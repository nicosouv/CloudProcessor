package com.onedrive.image.processor

import com.spotify.scio.options.ScioOptions
import com.typesafe.config.Config
import org.apache.beam.sdk.options.Validation.Required
import org.apache.beam.sdk.options.{ PipelineOptionsFactory, ValueProvider }

import scala.util.Try

case class OrganizerPipelineConfig(
    clientId: String,
    clientSecret: String,
    tenantId: String,
    accessToken: String,
    folderId: String) {}

object OrganizerPipelineConfig {

  def apply(config: Config, cmdlineArgs: Array[String]): Try[OrganizerPipelineConfig] =
    for {
      clientId <- Try(config.getString("onedrive.clientId"))
      clientSecret <- Try(config.getString("onedrive.clientSecret"))
      tenantId <- Try(config.getString("onedrive.tenantId"))
      accessToken <- Try(config.getString("onedrive.accessToken"))
      folderId <- Try(config.getString("onedrive.folderId"))
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
          options.getTenantId.get,
          options.getAccessToken.get,
          options.getFolderId.get
        )
      ).getOrElse(
        OrganizerPipelineConfig(
          clientId,
          clientSecret,
          tenantId,
          accessToken,
          folderId
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
  def getTenantId: ValueProvider[String]
  def setTenantId(value: ValueProvider[String]): Unit

  @Required
  def getAccessToken: ValueProvider[String]
  def setAccessToken(value: ValueProvider[String]): Unit

  @Required
  def getFolderId: ValueProvider[String]
  def setFolderId(value: ValueProvider[String]): Unit
}
