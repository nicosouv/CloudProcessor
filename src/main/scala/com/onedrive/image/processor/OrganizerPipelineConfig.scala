package com.onedrive.image.processor

import com.spotify.scio.options.ScioOptions
import com.typesafe.config.Config
import org.apache.beam.sdk.options.Validation.Required
import org.apache.beam.sdk.options.{ PipelineOptionsFactory, ValueProvider }

import scala.util.Try

case class OrganizerPipelineConfig(
    clientId: String,
    clientSecret: String,
    sourceFolder: String,
    destinationFolder: String,
    tenantId: String) {}

object OrganizerPipelineConfig {

  def apply(config: Config, cmdlineArgs: Array[String]): Try[OrganizerPipelineConfig] =
    for {
      clientId <- Try(config.getString("onedrive.clientId"))
      clientSecret <- Try(config.getString("onedrive.clientSecret"))
      tenantId <- Try(config.getString("onedrive.tenantId"))
      sourceFolder <- Try(config.getString("onedrive.sourceFolder"))
      destinationFolder <- Try(config.getString("onedrive.destinationFolder"))
    } yield {
      PipelineOptionsFactory.register(classOf[FolderPipelineOptions])

      Try {
        PipelineOptionsFactory
          .fromArgs(cmdlineArgs: _*)
          .withValidation
          .as(classOf[FolderPipelineOptions])
      }.map(options =>
        OrganizerPipelineConfig(
          options.getClientId.get,
          options.getClientSecret.get,
          options.getSourceFolder.get,
          options.getDestinationFolder.get,
          options.getTenantId.get
        )
      ).getOrElse(
        OrganizerPipelineConfig(
          clientId,
          clientSecret,
          sourceFolder,
          destinationFolder,
          tenantId
        )
      )

    }
}

trait FolderPipelineOptions extends ScioOptions {

  @Required
  def getClientId: ValueProvider[String]
  def setClientId(value: ValueProvider[String]): Unit

  @Required
  def getClientSecret: ValueProvider[String]
  def setClientSecret(value: ValueProvider[String]): Unit

  @Required
  def getSourceFolder: ValueProvider[String]
  def setSourceFolder(value: ValueProvider[String]): Unit

  @Required
  def getDestinationFolder: ValueProvider[String]
  def setDestinationFolder(value: ValueProvider[String]): Unit

  @Required
  def getTenantId: ValueProvider[String]
  def setTenantId(value: ValueProvider[String]): Unit
}
