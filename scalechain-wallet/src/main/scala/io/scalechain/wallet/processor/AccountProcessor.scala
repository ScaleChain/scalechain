package io.scalechain.wallet.processor

import java.io.File

import akka.actor.{Actor}
import io.scalechain.blockchain.storage.{DiskAccountStorage}
import io.scalechain.wallet.{Account}
import org.slf4j.LoggerFactory

/**
  * Created by mijeong on 2016. 3. 24..
  */
object AccountProcessor {

  case class GetNewAddress(account : String)
  case class GetNewAddressResult(addressOption : Option[String])

  def getAddressPurposeInt(purpose : String) : Int = {
    purpose match {
      case "received" => 1
      case _ => 2
    }
  }
}

class AccountProcessor() extends Actor {
  private val logger = LoggerFactory.getLogger(classOf[AccountProcessor])

  import AccountProcessor._

  def receive : Receive = {

    case GetNewAddress(account) => {

      val accountStorage = new DiskAccountStorage(new File("./target/accountdata/"), account)
      val coinAddress = Account(account).newAddress

      sender ! GetNewAddressResult(
        accountStorage.putNewAddress(
          account, coinAddress.address, AccountProcessor.getAddressPurposeInt(coinAddress.purpose), coinAddress.publicKey, coinAddress.privateKey
        )
      )
      accountStorage.close()
      println("processed GetNewAddress")
    }
  }

}
