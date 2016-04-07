package io.scalechain.wallet.processor

import java.io.File

import akka.actor.{Actor}
import io.scalechain.blockchain.proto.walletparts.WalletTransactionDetail
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
  case class GetAccountAddress(account : String)
  case class GetAccountAddressResult(address : String)
  case class ListTransactions(account : String, count : Int, skip : Int, includeWatchOnly : Boolean)
  case class ListTransactionsResult(transactions : Option[List[WalletTransactionDetail]])

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

  // TODO : change target directory for wallet transaction
  val accountStorage = new DiskAccountStorage(new File("./target/accountdata/"), new File("./target/unittests-DiskTransactionStorageSpec/"))

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

    case GetAccountAddress(account) => {

      val coinAddress = Account(account).newAddress

      sender ! GetAccountAddressResult(
        accountStorage.getReceiveAddress(account, coinAddress.address, AccountProcessor.getAddressPurposeInt(coinAddress.purpose), coinAddress.publicKey, coinAddress.privateKey)
      )
      println("processed GetAccountAddress")
    }

    case ListTransactions(account, count, skip, includeWatchOnly) => {

      sender ! ListTransactionsResult(
        accountStorage.getTransactionList(account, count, skip, includeWatchOnly)
      )
      println("processed ListTransactions")
    }

  }



}
