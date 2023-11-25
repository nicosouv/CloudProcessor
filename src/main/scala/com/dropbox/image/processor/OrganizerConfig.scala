package com.dropbox.image.processor

import scala.util.Try

import com.typesafe.config.Config

import org.apache.beam.sdk.options.{ PipelineOptionsFactory, ValueProvider }
import org.apache.beam.sdk.options.Validation.Required

import com.spotify.scio.options.ScioOptions

case class OrganizerConfig(
    client: String,
    accessToken: String,
    sourceFolder: String,
    destinationFolder: String) {}

object OrganizerConfig {

  def apply(config: Config, cmdlineArgs: Array[String]): Try[OrganizerConfig] =
    for {
      client <- Try(config.getString("dropbox.client"))
      accessToken <- Try(config.getString("dropbox.accessToken"))
      sourceFolder <- Try(config.getString("dropbox.source"))
      destinationFolder <- Try(config.getString("dropbox.destination"))
    } yield {
      PipelineOptionsFactory.register(classOf[FolderPipelineOptions])

      Try {
        PipelineOptionsFactory
          .fromArgs(cmdlineArgs: _*)
          .withValidation
          .as(classOf[FolderPipelineOptions])
      }.map(options =>
        OrganizerConfig(
          options.getClient.get,
          options.getAccessToken.get,
          options.getSourceFolder.get,
          options.getDestinationFolder.get
        )
      ).getOrElse(
        OrganizerConfig(
          client,
          accessToken,
          sourceFolder,
          destinationFolder
        )
      )

    }
}

trait FolderPipelineOptions extends ScioOptions {

  @Required
  def getClient: ValueProvider[String]
  def setClient(value: ValueProvider[String]): Unit

  @Required
  def getAccessToken: ValueProvider[String]
  def setAccessToken(value: ValueProvider[String]): Unit

  @Required
  def getSourceFolder: ValueProvider[String]
  def setSourceFolder(value: ValueProvider[String]): Unit

  @Required
  def getDestinationFolder: ValueProvider[String]
  def setDestinationFolder(value: ValueProvider[String]): Unit
}
