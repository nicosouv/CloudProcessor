package com.gdrive.image.processor

import org.slf4j.{ Logger, LoggerFactory }

import scala.util.{ Failure, Success, Try }
import scala.jdk.CollectionConverters.CollectionHasAsScala
import com.typesafe.config.ConfigFactory
import com.spotify.scio.ScioContext
import com.AppContext
import com.gdrive.auth.AuthHelper
import com.google.api.services.drive.Drive
import com.google.api.services.drive.model.{ File, FileList }

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object OrganizerPipeline {
  private val loggerName = getClass.getName

  @transient
  private lazy val logger: Logger =
    LoggerFactory.getLogger(loggerName)

  private val JOB_NAME = getClass.getSimpleName.stripSuffix("$")

  private val dateTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")

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
      val getClient: () => Drive =
        () => AuthHelper.getClient(config.credentialsPath)

      println(cmdlineArgs)

      // List files in the specified folder
      logger.info("Getting files in input folder")
      val gdriveFileList: FileList =
        getClient()
          .files()
          .list()
          .setFields(
            "nextPageToken,files(id, name, imageMediaMetadata, parents)"
          )
          .setQ(s"'$sourceFolder' in parents")
          .execute()

      val fileList: List[File] =
        Option(gdriveFileList.getFiles.asScala.toList).getOrElse(List())

      // Convert the list of files to an SCollection
      logger.info(s"Processing ${fileList.length} files")
      val fileSCollection = sc
        .parallelize(fileList)
        .map { file =>
          val tryDate = Try {
            LocalDateTime.parse(
              file.getImageMediaMetadata.getTime,
              dateTimeFormatter
            )
          }

          val date: Option[LocalDateTime] = tryDate match {
            case Success(value) => Option(value)
            case Failure(exception) =>
              logger.error(s"Cannot get time for ${file.getName} ($exception)")
              None
          }

          file -> date
        }
        .collect {
          case (file, Some(date)) => (date.getYear, date.getMonthValue) -> file
        }
        .groupByKey
        .map {
          case ((year, month), iterableFiles) =>
            val yearParentId =
              AuthHelper.getOrCreateFolder(destinationFolder, year.toString)
            val monthParentId =
              AuthHelper.getOrCreateFolder(yearParentId, month.toString)

            iterableFiles.map { file =>
              logger.info(s"Moving file ${file.getName}: to $month")
              getClient()
                .files()
                .update(file.getId, null)
                .setAddParents(monthParentId)
                .setRemoveParents(file.getParents.asScala.toList.mkString(","))
                .setFields("id, parents")
                .execute()
            }
        }

      // Materialize the result
      fileSCollection.materialize

      sc.run()
      ()
    }
  }
}
