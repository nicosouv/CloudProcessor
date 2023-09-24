package com.onedrive.auth

import com.microsoft.graph.authentication.{
  BaseAuthenticationProvider,
  TokenCredentialAuthProvider
}
import com.microsoft.graph.requests.GraphServiceClient
import com.azure.identity.ClientSecretCredentialBuilder

import scala.jdk.CollectionConverters._

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
