package com.dropbox.auth

import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2

object AuthHelper {

  def getClient(accessToken: String): DbxClientV2 = {
    val config = DbxRequestConfig.newBuilder("CloudProcessor").build

    new DbxClientV2(config, accessToken)
  }
}
