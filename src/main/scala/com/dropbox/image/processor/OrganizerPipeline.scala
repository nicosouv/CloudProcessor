package com.dropbox.image.processor

import org.slf4j.{ Logger, LoggerFactory }

import scala.util.{ Failure, Success, Try }

import scala.jdk.CollectionConverters.CollectionHasAsScala

import com.typesafe.config.ConfigFactory

import com.spotify.scio.ScioContext

import com.AppContext
import com.dropbox.auth.AuthHelper
import com.dropbox.core.v2.DbxClientV2

object OrganizerPipeline {
  private val loggerName = getClass.getName

  @transient
  private lazy val logger: Logger =
    LoggerFactory.getLogger(loggerName)

  private val JOB_NAME = getClass.getSimpleName.stripSuffix("$")

  def main(cmdlineArgs: Array[String]): Unit = {
    logger.info("Running Dropbox Image Processor job ...")
    logger.info(s"${cmdlineArgs.length}")

    (for {
      config <- Try(ConfigFactory.load())

      jobCfg <- OrganizerConfig(config, cmdlineArgs)
      _ = logger.info(
        s"Dropbox processor job configuration loaded: ${jobCfg}"
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

      val sourceFolder = config.sourceFolder
      val destinationFolder = config.destinationFolder
      val client: DbxClientV2 = AuthHelper.getClient(config.accessToken)
      val images =
        client.files().listFolder(sourceFolder).getEntries.asScala.toList
      println(cmdlineArgs)
      println(destinationFolder)

      val processed = sc.parallelize(images).map { image =>
        logger.info(s"${image.getName}: ${image.getPathDisplay}")
      }

      processed.materialize

      sc.run()
      ()
    }
  }
}
