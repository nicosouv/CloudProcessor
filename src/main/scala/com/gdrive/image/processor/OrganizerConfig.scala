package com.gdrive.image.processor

import scala.util.Try

import com.typesafe.config.Config

import org.apache.beam.sdk.options.{ PipelineOptionsFactory, ValueProvider }
import org.apache.beam.sdk.options.Validation.Required

import com.spotify.scio.options.ScioOptions

case class OrganizerConfig(
    credentialsPath: String,
    inputFolder: String,
    outputFolder: String) {}

object OrganizerConfig {

  def apply(config: Config, cmdlineArgs: Array[String]): Try[OrganizerConfig] =
    for {
      credentialsPath <- Try(config.getString("gdrive.credentialsPath"))
      inputFolder <- Try(config.getString("gdrive.inputFolder"))
      outputFolder <- Try(config.getString("gdrive.outputFolder"))
    } yield {
      PipelineOptionsFactory.register(classOf[FolderPipelineOptions])

      Try {
        PipelineOptionsFactory
          .fromArgs(cmdlineArgs: _*)
          .withValidation
          .as(classOf[FolderPipelineOptions])
      }.map(options =>
        OrganizerConfig(
          options.getCredentialsPath.get,
          options.getInputFolder.get,
          options.getOutputFolder.get
        )
      ).getOrElse(
        OrganizerConfig(
          credentialsPath,
          inputFolder,
          outputFolder
        )
      )

    }
}

trait FolderPipelineOptions extends ScioOptions {

  @Required
  def getCredentialsPath: ValueProvider[String]
  def setCredentialsPath(value: ValueProvider[String]): Unit

  @Required
  def getInputFolder: ValueProvider[String]
  def setInputFolder(value: ValueProvider[String]): Unit

  @Required
  def getOutputFolder: ValueProvider[String]
  def setOutputFolder(value: ValueProvider[String]): Unit
}
