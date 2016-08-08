package io.scalechain.util

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.net.{URL, URLConnection, HttpURLConnection}

import io.scalechain.blockchain.{UnsupportedFeature, ErrorCode, HttpRequestException}

object HttpRequester {
  def inputStreamAsString(is : InputStream) : String = {
    val sb: StringBuilder = new StringBuilder();
    val br: BufferedReader = new BufferedReader(new InputStreamReader(is));

    var read: String = null

    do {
      read = br.readLine()
      if (read != null) {
        //System.out.println(read);
        sb.append(read)
      }
    } while (read != null)

    br.close()
    return sb.toString()
  }

  /**
    * code copied from :
    * http://www.journaldev.com/7148/java-httpurlconnection-example-java-http-request-get-post
    * @param uri
    * @param postData
    * @param user
    * @param password
    * @return
    */
  def post(uri : String, postData : String, user : String, password : String) : String = {
    val url : URL = new URL(uri);
    val con : URLConnection = url.openConnection();
    val http : HttpURLConnection = con.asInstanceOf[HttpURLConnection];
    http.setRequestMethod("POST") // PUT is another valid option
    http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    val bytesToSend = postData.getBytes

    http.setDoOutput(true)
    val os = http.getOutputStream
    os.write(bytesToSend)
    os.flush
    os.close

    val responseCode = http.getResponseCode();
    val response = inputStreamAsString( http.getInputStream() )
    if (responseCode == HttpURLConnection.HTTP_OK) {
//      println(s"output : ${response}")
      response
    } else {
      throw new HttpRequestException(ErrorCode.HttpRequestFailure, responseCode, response)
    }
  }
}

