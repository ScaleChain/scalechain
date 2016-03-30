package io.scalechain.wallet.service

import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import io.scalechain.wallet.processor.AccountProcessor
import io.scalechain.wallet.processor.AccountProcessor.{GetAccountAddressResult, GetAccountResult, GetNewAddressResult}

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.duration._

/**
  * Created by mijeong on 2016. 3. 24..
  */
class AccountDatabaseService() {
  implicit val timeout = Timeout(60 seconds)

  implicit val system = ActorSystem("ScaleChainWallet", ConfigFactory.load("server"))
  val accountProcessor = system.actorOf(Props[AccountProcessor])

  def getNewAddress(account : String) : Option[String] = {
    val resultFuture = (accountProcessor ? AccountProcessor.GetNewAddress(account)).mapTo[GetNewAddressResult]
    Await.result(resultFuture, Duration.Inf).addressOption
  }

  def getAccount(address : String) : Option[String] = {
    val resultFutre = (accountProcessor ? AccountProcessor.GetAccount(address)).mapTo[GetAccountResult]
    Await.result(resultFutre, Duration.Inf).accountOption
  }

  def getAccountAddress(account : String) : String = {
    val resultFutre = (accountProcessor ? AccountProcessor.GetAccountAddress(account)).mapTo[GetAccountAddressResult]
    Await.result(resultFutre, Duration.Inf).address
  }

}
