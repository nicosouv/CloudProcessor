package com.onedrive.image.processor

import com.onedrive.AppContext
import com.spotify.scio.ScioContext
import com.typesafe.config.ConfigFactory
import org.slf4j.{ Logger, LoggerFactory }

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
      _ = logger.info(s"Onedrive Image processor job configuration loaded: ${jobCfg}")

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

      val items = Seq("toto", "titi")

      logger.info(s"hey ${config.clientId}")
      logger.info(s"hey ${cmdlineArgs.length}")

      sc.parallelize(items)
        .map(item => logger.info(s"hey $item"))
        .countByValue

      sc.run()
      ()
    }
  }
}
