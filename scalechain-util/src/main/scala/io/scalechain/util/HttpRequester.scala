package io.scalechain.util

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.ByteString
import akka.http.scaladsl.Http

import akka.http.scaladsl.model._

import HttpMethods._
import io.scalechain.blockchain.{ErrorCode, HttpRequestException}

import scala.concurrent.Await

// customize every detail of HTTP request
import HttpProtocols._
import MediaTypes._


import scala.concurrent.{Await, Future}
import akka.http.scaladsl.model.StatusCodes._

import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object HttpRequester {
  def post(uri : String, postData : String, user : String, password : String) = {

    // BUGBUG : Need to pass actor system as an implicit parameter.
    implicit val actorSystem = ActorSystem()
    implicit val materializer = ActorMaterializer()

    val authorization = headers.Authorization(BasicHttpCredentials(user, password))
    val entity = HttpEntity(`application/json`, ByteString(postData))

    val request = HttpRequest(
      POST,
      uri = uri,
      entity = entity,
      headers = List(authorization),
      protocol = `HTTP/1.1`)

    val responseFuture : Future[HttpResponse] = Http().singleRequest(request);

    val response = Await.result(responseFuture, Duration.Inf )

    val responseTextFuture = response.status match {
      case OK => Unmarshal(response.entity).to[String].map( responseText => responseText )
      case _ => Unmarshal(response.entity).to[String].map { responseText =>
        throw new HttpRequestException(ErrorCode.HttpRequestFailure, response.status.intValue, responseText)
      }
    }

    val responseText = Await.result(responseTextFuture, Duration.Inf )

    responseText
  }
}

