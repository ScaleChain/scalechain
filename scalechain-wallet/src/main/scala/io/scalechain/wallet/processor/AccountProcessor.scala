package io.scalechain.wallet.processor

import java.io.File

import akka.actor.{Actor}
import io.scalechain.blockchain.storage.{DiskAccountStorage}
import io.scalechain.wallet.{CoinAddress, Account}
import org.slf4j.LoggerFactory

/**
  * Created by mijeong on 2016. 3. 24..
  */
object AccountProcessor {

  case class GetNewAddress(account : String)
  case class GetNewAddressResult(addressOption : Option[String])
  case class GetAccount(address : String)
  case class GetAccountResult(accountOption : Option[String])

  def getAddressPurposeInt(purpose : String) : Int = {
    purpose match {
      case "unknown" => 1
      case "received" => 2
      case _ => 0
    }
  }
}

class AccountProcessor() extends Actor {
  private val logger = LoggerFactory.getLogger(classOf[AccountProcessor])

  import AccountProcessor._

  val accountStorage = new DiskAccountStorage(new File("./target/accountdata/"))
  accountStorage.open

  def receive : Receive = {

    case GetNewAddress(account) => {

      val coinAddress = Account(account).newAddress

      sender ! GetNewAddressResult(
        accountStorage.putNewAddress(
          account, coinAddress.address, AccountProcessor.getAddressPurposeInt(coinAddress.purpose), coinAddress.publicKey, coinAddress.privateKey
        )
      )
      println("processed GetNewAddress")
    }

    case GetAccount(address) => {

      val coinAddress = CoinAddress(
        address = address,
        purpose = "",
        publicKey = null,
        privateKey = null
      )

      if(coinAddress.isValid) {
        sender ! GetAccountResult(
          accountStorage.getAccount(
            address
          )
        )
      } else {
        sender ! GetAccountResult(
          Some("InvalidAddress")
        )
      }

      println("processed GetAccount")
    }
  }

}
