package com.onedrive.auth

import com.microsoft.aad.msal4j.{ConfidentialClientApplication, ClientCredentialFactory}
import scala.collection.JavaConverters._

class AuthHelper(tenantId: String, clientId: String, clientSecret: String) {
  private val authority = s"https://login.microsoftonline.com/$tenantId"

  private val app = ConfidentialClientApplication
    .builder(clientId, ClientCredentialFactory.createFromSecret(clientSecret))
    .authority(authority)
    .build()

  def getAccessToken(scopes: Seq[String]): String = {
    val parameters = Map("scope" -> scopes.mkString(" ")).asJava
    val result = app.acquireToken(parameters).join()
    result.accessToken()
  }
}

