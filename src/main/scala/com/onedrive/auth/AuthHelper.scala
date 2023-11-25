package com.onedrive.auth

import scala.jdk.CollectionConverters._

import com.azure.identity.ClientSecretCredentialBuilder
import com.microsoft.graph.authentication.{
  BaseAuthenticationProvider,
  TokenCredentialAuthProvider
}
import com.microsoft.graph.requests.GraphServiceClient

object AuthHelper {

  private def getAuthProvider(
      clientId: String,
      clientSecretId: String,
      tenantId: String
    ): BaseAuthenticationProvider = {
    val deviceCodeCredential = new ClientSecretCredentialBuilder()
      .clientId(clientId)
      .tenantId(tenantId)
      .clientSecret(clientSecretId)
      .build()
    println(s"DEVICE CODE CREDENTIAL: ${deviceCodeCredential}")

    new TokenCredentialAuthProvider(
      List(".default").asJava,
      deviceCodeCredential
    )
  }

  def getGraphServiceClient(
      clientId: String,
      clientSecretId: String,
      tenantId: String
    ) = {
    val authProvider = getAuthProvider(clientId, clientSecretId, tenantId)

    GraphServiceClient
      .builder()
      .authenticationProvider(authProvider)
      .buildClient()
  }
}
