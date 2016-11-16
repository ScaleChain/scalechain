package io.scalechain.util

import java.io.*
import java.net.*

class HttpRequestException(val httpCode : Int, val reponse : String ) : Exception(httpCode.toString() + " : " + reponse)

object HttpRequester {
    @JvmStatic
    fun inputStreamAsString(istream : InputStream) : String {
        val sb: StringBuilder = StringBuilder();
        val br: BufferedReader = BufferedReader(InputStreamReader(istream));

        var read: String? = null

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
    @JvmStatic
    fun post(uri : String, postData : String, user : String, password : String) : String {
        val url : URL = URL(uri);
        val con : URLConnection = url.openConnection();
        val http : HttpURLConnection = con as HttpURLConnection;
        http.setRequestMethod("POST") // PUT is another valid option
        http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        val bytesToSend = postData.toByteArray()

        http.setDoOutput(true)
        val os = http.outputStream
        os.write(bytesToSend)
        os.flush()
        os.close()

        val responseCode = http.getResponseCode();
        val response = inputStreamAsString( http.getInputStream() )
        if (responseCode == HttpURLConnection.HTTP_OK) {
            return response
        } else {
            throw HttpRequestException(responseCode, response)
        }
    }
}

