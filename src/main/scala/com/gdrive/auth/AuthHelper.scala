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
import com.google.api.services.drive.model.File

import java.io.InputStreamReader
import java.nio.file.{ Files, Paths }
import java.util
import java.util.Collections
import scala.annotation.unused
import scala.jdk.CollectionConverters.CollectionHasAsScala

object AuthHelper {
  private val JSON_FACTORY = GsonFactory.getDefaultInstance

  @unused
  private val SCOPES = Collections.singletonList(DriveScopes.DRIVE_METADATA)

  private var client: Drive = null

  def getClient(credentialsPath: String): Drive = {
    val absolutePath = Paths.get(credentialsPath).toAbsolutePath.toString
    val in = Files.newInputStream(Paths.get(absolutePath))

    val clientSecrets =
      GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in))

    val flow = new GoogleAuthorizationCodeFlow.Builder(
      GoogleNetHttpTransport.newTrustedTransport,
      JSON_FACTORY,
      clientSecrets,
      DriveScopes.all()
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
    client = new Drive.Builder(
      GoogleNetHttpTransport.newTrustedTransport,
      JSON_FACTORY,
      credential
    ).setApplicationName("CloudProcessor").build

    client
  }

  def getOrCreateFolder(rootFolder: String, name: String): String = {
    val gdriveFolderList: List[File] = client
      .files()
      .list()
      .setSpaces("drive")
      .setFields("nextPageToken, files(id, name, parents)")
      .setQ(
        s"name='$name' and '$rootFolder' in parents and trashed=false and mimeType='application/vnd.google-apps.folder'"
      )
      .execute()
      .getFiles
      .asScala
      .toList

    val gdriveFolder = gdriveFolderList.headOption

    gdriveFolder match {
      case Some(folder) =>
        println(s"Getting folder $name: ${folder.getId}")
        folder.getId
      case _ =>
        val id = createFolder(rootFolder, name)
        println(s"Creating folder $name: $id")
        id
    }
  }

  private def createFolder(rootFolder: String, name: String): String = {
    val fileMetadata = new File
    fileMetadata.setName(name)
    fileMetadata.setParents(util.Arrays.asList(rootFolder))
    fileMetadata.setMimeType("application/vnd.google-apps.folder")

    val file = client.files.create(fileMetadata).setFields("id").execute
    println(s"Folder ID: ${file.getId}")
    file.getId
  }
}
