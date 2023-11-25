package com.gdrive.auth

import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.{
  GoogleAuthorizationCodeFlow,
  GoogleClientSecrets
}
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.{ Drive, DriveScopes }

import java.io.InputStreamReader
import java.nio.file.{ Files, Paths }
import java.util.Collections
import scala.annotation.unused

object AuthHelper {
  private val JSON_FACTORY = GsonFactory.getDefaultInstance

  @unused
  private val SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA)

  def getClient(credentialsPath: String): Drive = {
    val absolutePath = Paths.get(credentialsPath).toAbsolutePath.toString
    val in = Files.newInputStream(Paths.get(absolutePath))

    val clientSecrets =
      GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))

    val flow = new GoogleAuthorizationCodeFlow.Builder(
      GoogleNetHttpTransport.newTrustedTransport,
      JSON_FACTORY,
      clientSecrets,
      Collections.singletonList(DriveScopes.DRIVE)
    ).setDataStoreFactory(
      new com.google.api.client.util.store.FileDataStoreFactory(
        new java.io.File("tokens")
      )
    ).setAccessType("online")
      .build
    val credential =
      new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver)
        .authorize("user")

    // Build the Drive API client
    new Drive.Builder(
      GoogleNetHttpTransport.newTrustedTransport,
      JSON_FACTORY,
      credential
    ).setApplicationName("CloudProcessor").build
  }
}
