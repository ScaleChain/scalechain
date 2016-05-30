package io.scalechain.util

import io.scalechain.blockchain.{UnsupportedFeature, ErrorCode, HttpRequestException}

object HttpRequester {
  def post(uri : String, postData : String, user : String, password : String) : String = {
    // TODO : Need to implement HTTP client to connect to our API service.
    throw new UnsupportedFeature(ErrorCode.UnsupportedFeature)
  }
}

