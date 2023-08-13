package example

import org.slf4j.{ Logger, LoggerFactory }

import scala.util.{ Failure, Success, Try }

import com.typesafe.config.ConfigFactory

import com.spotify.scio.ScioContext

import com.onedrive.AppContext

object PipelineExample {

  private val loggerName = getClass.getName

  @transient
  private[example] lazy val logger: Logger =
    LoggerFactory.getLogger(loggerName)

  private val JOB_NAME = getClass.getSimpleName.stripSuffix("$")

  def main(cmdlineArgs: Array[String]): Unit = {

    logger.info("Running Pipeline Example job ...")

    (for {
      config <- Try(ConfigFactory.load())

      jobCfg <- PipelineExampleConfig(config, cmdlineArgs)
      _ = logger.info(s"Pipeline Example job configuration loaded: ${jobCfg}")

      context <- AppContext(config, cmdlineArgs, JOB_NAME)
      _ = logger.info(s"Application context loaded: $context")

      _ <- apply(context, jobCfg)
    } yield {
      ()
    }) match {
      case Success(_) =>
        logger.info("Pipeline Example job successfully executed")

      case Failure(cause) =>
        logger.error("Fails to execute Pipeline Example job", cause)
    }
  }

  def apply(context: AppContext, config: PipelineExampleConfig): Try[Unit] = {
    Try {
      val sc = ScioContext(context.pipelineOptions)

      sc.textFile(config.inputPath)
        .flatMap(_.split("[^a-zA-Z']+").filter(_.nonEmpty))
        .countByValue
        .map(kv => print(s"${kv._1.toUpperCase} = ${kv._2}"))

      sc.run()
      ()
    }
  }
}
