package com.onedrive.image.processor

import com.onedrive.AppContext
import com.onedrive.auth.AuthHelper
import com.spotify.scio.ScioContext
import com.typesafe.config.ConfigFactory
import org.slf4j.{ Logger, LoggerFactory }

import scala.annotation.unused
import scala.jdk.CollectionConverters.CollectionHasAsScala
import scala.util.{ Failure, Success, Try }

object OrganizerPipeline {
  private val loggerName = getClass.getName

  @transient
  private lazy val logger: Logger =
    LoggerFactory.getLogger(loggerName)

  private val JOB_NAME = getClass.getSimpleName.stripSuffix("$")

  def main(cmdlineArgs: Array[String]): Unit = {
    logger.info("Running OneDrive Image Processor job ...")
    logger.info(s"${cmdlineArgs.length}")

    (for {
      config <- Try(ConfigFactory.load())

      jobCfg <- OrganizerPipelineConfig(config, cmdlineArgs)
      _ = logger.info(
        s"Onedrive Image processor job configuration loaded: ${jobCfg}"
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
      config: OrganizerPipelineConfig,
      cmdlineArgs: Array[String]
    ): Try[Unit] = {

    Try {
      val options = context.pipelineOptions
      val sc = ScioContext(options)

      val sourceFolder = config.sourceFolder
      val destinationFolder = config.destinationFolder

      val clientId = config.clientId
      val clientSecret = config.clientSecret
      val tenantId = config.tenantId
      val graphClient =
        AuthHelper.getGraphServiceClient(clientId, clientSecret, tenantId)

      logger.info(s"CmdLine Length: ${cmdlineArgs.length}")
      logger.info(s"From oneDriveFolder: $sourceFolder to destinationFolder: $destinationFolder")
      logger.info(s"Access Token: $graphClient")

      val images = graphClient
        .drive()
        .root()
        .itemWithPath(sourceFolder)
        .children
        .buildRequest()
        .get()
        .getCurrentPage
        .asScala

      sc.parallelize(images).map { image =>
        logger.info(s"Name: ${image.name}, Type: ${image.file.mimeType}")
      }

      sc.run()
      ()
    }
  }

  @unused
  def getExifData(imagePath: String): Map[String, String] = {
    logger.info(s"imagePath: $imagePath")
    Map("dateTaken" -> "2023-08-10")
  }

}
