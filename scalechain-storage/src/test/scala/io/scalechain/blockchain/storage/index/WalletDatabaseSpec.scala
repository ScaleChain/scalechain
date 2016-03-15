package io.scalechain.blockchain.storage.index

import io.scalechain.blockchain.proto.Wallet
import io.scalechain.blockchain.proto.codec.{WalletCodec, CodecTestUtil}
import io.scalechain.blockchain.storage.Storage
import io.scalechain.io.HexFileLoader
import org.scalatest._
import scodec.bits.BitVector

/**
  * Created by mijeong on 2016. 3. 15..
  */
class WalletDatabaseSpec extends FlatSpec with BeforeAndAfterEach with ShouldMatchers with CodecTestUtil {
  this: Suite =>

  Storage.initialize()

  val rawWalletBytes = HexFileLoader.loadWallet("data/unittest/codec/wallet-size289000.hex")
  val rawWalletBits = BitVector.view(rawWalletBytes)

  implicit val codec = WalletCodec.codec
  val wallet : Wallet = decodeFully(rawWalletBits)
}
