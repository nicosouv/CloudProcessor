package com.onedrive

import com.spotify.scio.ScioContext
import org.slf4j.{ Logger, LoggerFactory }
import com.typesafe.config.Config
import org.apache.beam.sdk.options.PipelineOptions

import scala.util.Try

final class AppContext(
    val name: String,
    val pipelineOptions: PipelineOptions) {
  val logger: Logger = LoggerFactory.getLogger(name)

  override def toString: String =
    s"{name: $name}"
}

object AppContext {
  val logger: Logger = LoggerFactory.getLogger(getClass)

  def apply(
      config: Config,
      cmdlineArgs: Array[String],
      jobName: String
    ): Try[AppContext] =
    for {
      appName <- Try(config getString "appName")
    } yield {
      val (options, _) = ScioContext
        .parseArguments[PipelineOptions](cmdlineArgs, withValidation = true)

      val rand = new scala.util.Random

      val name = s"$appName-$jobName-${rand.nextInt()}"

      options.setJobName(name)

      new AppContext(
        name = name,
        pipelineOptions = options
      )
    }
}
