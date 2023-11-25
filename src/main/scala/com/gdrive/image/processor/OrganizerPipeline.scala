package com.gdrive.image.processor

import org.slf4j.{ Logger, LoggerFactory }

import scala.util.{ Failure, Success, Try }

import scala.jdk.CollectionConverters.CollectionHasAsScala

import com.typesafe.config.ConfigFactory

import com.spotify.scio.ScioContext

import com.AppContext
import com.gdrive.auth.AuthHelper
import com.google.api.services.drive.model.{ File, FileList }

object OrganizerPipeline {
  private val loggerName = getClass.getName

  @transient
  private lazy val logger: Logger =
    LoggerFactory.getLogger(loggerName)

  private val JOB_NAME = getClass.getSimpleName.stripSuffix("$")

  def main(cmdlineArgs: Array[String]): Unit = {
    logger.info("Running gDrive Image Processor job ...")
    logger.info(s"${cmdlineArgs.length}")

    (for {
      config <- Try(ConfigFactory.load())

      jobCfg <- OrganizerConfig(config, cmdlineArgs)
      _ = logger.info(
        s"gDrive processor job configuration loaded: ${jobCfg}"
      )

      context <- AppContext(config, cmdlineArgs, JOB_NAME)
      _ = logger.info(s"Application context loaded: $context")

      _ <- apply(context, jobCfg, cmdlineArgs)
    } yield {
      ()
    }) match {
      case Success(_) =>
        logger.info("Pipeline job successfully executed")

      case Failure(cause) =>
        logger.error("Fails to execute Pipeline job", cause)
    }
  }

  def apply(
      context: AppContext,
      config: OrganizerConfig,
      cmdlineArgs: Array[String]
    ): Try[Unit] = {

    Try {
      val options = context.pipelineOptions
      val sc = ScioContext(options)

      val sourceFolder = config.inputFolder
      val destinationFolder = config.outputFolder
      val client = AuthHelper.getClient(config.credentialsPath)

      println(destinationFolder)
      println(cmdlineArgs)

      // List files in the specified folder
      logger.info("Getting files in input folder")
      val fileList: FileList =
        client
          .files()
          .list()
          .setFields("nextPageToken, files(id,name,parents)")
          .setQ(s"'$sourceFolder' in parents")
          .execute()

      val files: List[File] =
        Option(fileList.getFiles.asScala.toList).getOrElse(List())

      // Convert the list of files to an SCollection
      logger.info(s"Processing ${files.length} files")
      val fileSCollection =
        sc.parallelize(files)
          .map(processFile =>
            s"FILE: ${processFile.getName}: ${processFile.getId}"
          )

      // Materialize the result
      fileSCollection.materialize

      sc.run()
      ()
    }
  }
}
