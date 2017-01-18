package io.scalechain.blockchain.net

import io.scalechain.blockchain.ErrorCode
import io.scalechain.blockchain.GeneralException
import io.scalechain.blockchain.RpcException
import io.scalechain.blockchain.chain.Blockchain
import io.scalechain.blockchain.chain.TransactionBuilder
import io.scalechain.blockchain.chain.processor.TransactionProcessor
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinition
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer
import io.scalechain.blockchain.oap.command.AssetTransferTo
import io.scalechain.blockchain.oap.exception.OapException
import io.scalechain.blockchain.oap.wallet.AssetAddress
import io.scalechain.blockchain.oap.wallet.AssetId
import io.scalechain.blockchain.oap.*
import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.storage.index.KeyValueDatabase
import io.scalechain.blockchain.transaction.*
import io.scalechain.util.ByteArray
import io.scalechain.util.HexUtil
import io.scalechain.wallet.UnspentCoinDescriptor
import io.scalechain.wallet.Wallet

/** List of responses for submitblock RPC.
 */
enum class SubmitBlockResult {
  DUPLICATE, DUPLICATE_INVALID, INCONCLUSIVE, REJECTED
}


class RpcSubSystem(private val db : KeyValueDatabase, private val chain : Blockchain, private val peerCommunicator: PeerCommunicator) {

  /** Get the hash of a block specified by the block height on the best blockchain.
    *
    * Used by : getblockhash RPC.
    *
    * @param blockHeight The height of the block.
    * @return The hash of the block header.
    */
  fun getBlockHash(blockHeight : Long) : Hash {
    return chain.getBlockHash(db, blockHeight)
  }

  /** Get a block searching by the header hash.
    *
    * Used by : getblock RPC.
    *
    * @param blockHash The header hash of the block to search.
    * @return The searched block.
    */
  fun getBlock(blockHash: Hash): Pair<BlockInfo, Block>? {
    return chain.getBlock(db, blockHash)
  }

  /** Get the header hash of the most recent block on the best block chain.
    *
    * Used by : getbestblockhash RPC.
    *
    * @return The header hash of the most recent block.
    */
  fun getBestBlockHash(): Hash? {
    return chain.getBestBlockHash(db)
  }


  /**
    * Return the block height of the best block.
    *
    * Used by getrawtransaction RPC to get the confirmation of the block which has a transaction.
    *
    * @return The height of the best block.
    */
  fun getBestBlockHeight() : Long {
    return chain.getBestBlockHeight()
  }

  /**
    * Get the block info of the block which has the given transaction.
    *
    * @param txHash The hash of the transaction to get the block info of the block which has the transaction.
    * @return Some(block info) if the transaction is included in a block; None otherwise.
    */
  fun getTransactionBlockInfo(txHash : Hash) : BlockInfo? {
    return chain.getTransactionBlockInfo(db, txHash)
  }

  /** Get a transaction searching by the transaction hash.
    *
    * Used by : gettransaction RPC.
    *
    * @param txHash The header hash of the transaction to search.
    * @return The searched block.
    */
  fun getTransaction(txHash : Hash): Transaction? {
    return chain.getTransaction(db, txHash)
  }

  /** Accepts a block, verifies it is a valid addition to the block chain, and broadcasts it to the network.
    *
    * Used by : submitblock RPC.
    *
    * @param block The block we are going to submit.
    * @param parameters The JsObject we got from the second parameter of submitblock RPC. A common parameter is a workid string.
    * @return Some(SubmitBlockResult) if any error happend; None otherwise.
    */
  fun submitBlock(block : Block, parameters : JsonObject) : SubmitBlockResult? {
    // TODO : BUGBUG : parameters is not used.
    val blockHash = block.header.hash()
    if (chain.hasBlock(db, blockHash)) {
      return SubmitBlockResult.DUPLICATE
    } else {
      BlockPropagator.propagate(blockHash, block)

      return null
    }
  }

  /** Validates a transaction and broadcasts it to the peer-to-peer network.
    *
    * Used by : sendrawtransaction RPC.
    *
    * @param transaction The serialized transaction.
    * @param allowHighFees Whether to allow the transaction to pay a high transaction fee.
    * @return
    */
  fun sendRawTransaction(transaction : Transaction, allowHighFees : Boolean) {
    // Do not process the send raw transaction RPC during initial block download.
    if ( Node.get().isInitialBlockDownload() ) {
      throw RpcException( ErrorCode.Companion.BusyWithInitialBlockDownload, "Unable to send raw transactions while the initial block download is in progress.")
    } else {
      TransactionProcessor.putTransaction(Blockchain.get().db, transaction.hash(), transaction)

      peerCommunicator.propagateTransaction(transaction)
    }
  }

  /** Get the list of information on each peer.
    *
    * Used by : getpeerinfo RPC.
    *
    * @return The list of peer information.
    */
  fun getPeerInfos() : List<PeerInfo> {
    return peerCommunicator.getPeerInfos()
  }

  fun verifyTransaction( transaction : Transaction) : Unit {
    val db : KeyValueDatabase = Blockchain.get().db

    TransactionVerifier(db, transaction).verify(Blockchain.get())
  }


  //
  // Methods for SendMany API
  //

  /**
   * Returns "non watch-only" addresses of account.
   *
   * @param account
   * @return
   */
  protected[net] def nonWatchOnlyAddressesOf(account : String) : Option[List[CoinAddress]] = {
    val addressesOption = Option(
      Wallet.get.store.getOutputOwnerships(Option(account))(Blockchain.get.db).filter(o => {
        !Wallet.get.store.getPrivateKeys(Option(o))(Blockchain.get.db).isEmpty
      }).map((o: OutputOwnership) => {
      if (o.isInstanceOf[CoinAddress]) o.asInstanceOf[CoinAddress] else CoinAddress.from(o.stringKey())
    }: CoinAddress)
    )
    if (addressesOption.isDefined) {
      if (addressesOption.get.size == 0) {
        if (account.length > 0) throw new OapException(OapException.INVALID_ARGUMENT, "No addresses exist for account " + account);
      }
    }
    addressesOption
  }

  protected[net] def calculateInputsAndChange(addressesOption: Option[List[CoinAddress]], outputs : List[Tuple2[CoinAddress, CoinAmount]]) : Tuple2[List[UnspentCoinDescriptor], CoinAmount] = {
    val zero = BigDecimal(0);
    if (outputs.size == 0) {
      throw new OapException(OapException.INVALID_ARGUMENT, "No outputs.");
    }
    val sumOfOutputAmount: BigDecimal = outputs.foldLeft[BigDecimal](BigDecimal(0))((sum, output) => sum + output._2.value)
    var inputAmountRequired = sumOfOutputAmount + IOapConstants.DEFAULT_FEES_IN_BITCOIN
    val unspents = Wallet.get.listUnspent(Blockchain.get, IOapConstants.DEFAULT_MIN_CONFIRMATIONS, IOapConstants.DEFAULT_MAX_CONFIRMATIONS, addressesOption)(Blockchain.get.db).filter(
      p => {
      // remove MARKER OUTPUT(0) AND ASSETS(600)
      if ((inputAmountRequired <= zero) || (p.amount == BigDecimal(IOapConstants.DUST_IN_BITCOIN))) false
      else {
        inputAmountRequired = inputAmountRequired - p.amount
        true
      }
    }
    )
    inputAmountRequired = - inputAmountRequired;
    Tuple2(unspents, CoinAmount.apply(inputAmountRequired))
  }

  protected[net] def buildSendManyTransaction(
  inputsAndNegativeChagne : Tuple2[List[UnspentCoinDescriptor], CoinAmount],
  outputs : List[Tuple2[CoinAddress, CoinAmount]],
  account: String
  ) : Transaction = {
    val builder = TransactionBuilder.newBuilder()
    inputsAndNegativeChagne._1.foreach(u => builder.addInput(Blockchain.get, OutPoint(u.txid, u.vout))(Blockchain.get.db))
    outputs.foreach(o => builder.addOutput(o._2, o._1))
    if (inputsAndNegativeChagne._2.value > 0) // ADD CHANGE, CHANGE GOES TO THE RECEIVING ADDRESS OF THE ACCOUNT.
      builder.addOutput(inputsAndNegativeChagne._2, Wallet.get.getReceivingAddress(account)(Blockchain.get.db))
    builder.build()
  }

  def sendMany(account : String,outputs : List[Tuple2[String, BigDecimal]], comment: String, subtractFees: List[String]) : Hash = {
    if (outputs.size == 0) {
      throw new OapException(OapException.INVALID_ARGUMENT, "No outputs.");
    }
    // TODO WE SOULD CHECK OUTPUT
    val transfers : List[Tuple2[CoinAddress, CoinAmount]] = outputs.map(
    tuple => (
    try {
      CoinAddress.from(tuple._1)
    } catch {
      case e : Throwable => throw new OapException(OapException.INVALID_ARGUMENT, "Cannot convert address " + tuple._1, e);
    },
    if (tuple._2 < 0)
      throw new OapException(OapException.INVALID_ARGUMENT, "Negative amount " + tuple._2)
    else
    CoinAmount.apply(tuple._2)
    )
    );
    val addressesOption = nonWatchOnlyAddressesOf(account);
    val inputsAndChange = calculateInputsAndChange(addressesOption, transfers)

    if (inputsAndChange._2.value < 0) { // NOT ENOUGH COIN

      throw new OapException(OapException.NOT_ENOUGH_COIN, "Account " + account + " has not enought coins");
    }

    val rawTx = buildSendManyTransaction(inputsAndChange, transfers, account)

    val signedTx = Wallet.get.signTransaction(rawTx, Blockchain.get, List(), None, SigHash.withName("ALL"))(Blockchain.get.db)
    if (!signedTx.complete) { // NOT COMPLETE, THROW EXCEPTION.
      throw new OapException(OapException.CANNOT_SIGN_TRANSACTION, "Cannot sign all inputs");
    } else {
      verifyTransaction(signedTx.transaction)
      sendRawTransaction(signedTx.transaction, true)
      // RETURN HASH as Hex String
      signedTx.transaction.hash
    }
  }

  protected[net] def getAssetDefinitionPointer(assetId: AssetId, hashOption: Option[String]) : Option[AssetDefinitionPointer] = {
    val hashPointerOption =
      if (hashOption.isDefined && hashOption.get.length() > 0)
        Option(AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, HexUtil.bytes(hashOption.get)))
      else
        None
    val idPointerOption: Option[AssetDefinitionPointer] = OpenAssetsProtocol.get.storage.getAssetDefinitionPointer(assetId.base58())

    if (hashPointerOption.isDefined) {
      if (!hashPointerOption.get.equals(idPointerOption.get)) {
        throw new OapException(OapException.DEFINITION_POINTER_ERROR, "Asset Definition Pointer does not match with Asset Id")
      }
      // IF NO ASSET DEFINITION IS FOUND, THROW AN EXCEPTION
      val stringOption = OpenAssetsProtocol.get().storage.getAssetDefinition(hashPointerOption.get)
      if (stringOption.isEmpty)
        throw new OapException(OapException.DEFINITION_POINTER_ERROR, "Invalid Asset Definition Pointer")
      // IF ASSET DEFINITION FILE DOESN'T HAVE asset_ids CONTAINING assetId THROW AN EXCEPTION
      val definition = AssetDefinition.from(stringOption.get);
      if (!definition.getAssetIds.contains(assetId.base58()))
        throw new OapException(OapException.DEFINITION_POINTER_ERROR, "Asset Definition is not match with given Hash")
      hashPointerOption
    } else {
      idPointerOption
    }
  }

  protected[net] def createAssetDefinition(assetId : AssetId, metadataOption : Option[JsObject]) : Option[AssetDefinitionPointer] = {
    // CREATE NEW ASSET DEFITION
    if (metadataOption.isEmpty) throw new OapException(OapException.INVALID_ARGUMENT,"No Asset Definition found")
    Option(AssetDefinitionHandler.get().createAssetDefinition(assetId, metadataOption.get.toString()).getFirst)
  }

  def issueAsset(issuerAddress: String,toAddress: String, quantity: Int, hashOption: Option[String], privateKeys: Option[List[PrivateKey]], changeAddress: String, fees: Long, metadataOption: Option[JsObject]): IssueAssetResult = {
    if (fees < IOapConstants.MIN_FEES_IN_SATOSHI) throw new OapException(OapException.FEES_TOO_SMALL, "Fees are to small")
    if (quantity < 0) throw new OapException(OapException.INVALID_QUANTITY, "Invalid quantity: " + quantity)

    val issuer: CoinAddress = CoinAddress.from(issuerAddress)
    val to: AssetAddress = AssetAddress.from(toAddress)
    val assetId: AssetId = AssetId.from(issuer)
    val change: CoinAddress = CoinAddress.from(issuerAddress)

    val pointerOption = getAssetDefinitionPointer(assetId, hashOption)

    // CREATE ASSET DEFINITION
    var pointerToBeRollbacked : Option[AssetDefinitionPointer] = None
    if (pointerOption.isEmpty) {
      pointerToBeRollbacked = createAssetDefinition(assetId, metadataOption)
    }

    val pointer = if (pointerOption.isDefined) pointerOption.get else pointerToBeRollbacked.get
    try
    //   If an exception is thrown, newly created asset defition should be rollbacked.
      val rawTx = OpenAssetsProtocol.get().createIssuanceTransaction(issuer, to, assetId, quantity, pointer, change, fees)
      val signedTx = Wallet.get.signTransaction(rawTx, Blockchain.get, List(), privateKeys, SigHash.ALL)(Blockchain.get.db)
      if (!signedTx.complete) {
        throw new OapException(OapException.CANNOT_SIGN_TRANSACTION, "Cannot sign all inputs");
      }

      verifyTransaction(signedTx.transaction)
      sendRawTransaction(signedTx.transaction, true)
      // TODO : We can propagate newly created oap transaction here.
      // TODO : We can also propagate new asset definition file and new asset definition pointer here.

      // EVERYTHING DONE, CLEAR pointerTOBeRollbacked
      pointerToBeRollbacked = None
      IssueAssetResult(signedTx.transaction.hash, HexUtil.hex(pointer.getValue), assetId.base58())
      finally {
        if (pointerToBeRollbacked.isDefined) {
          // pointerToBeRollbacked IS NOT CLEAR, DELETE the pointer and defition
          OapStorage.get().delAssetDefintion(pointerToBeRollbacked.get)
          OapStorage.get().delAssetDefinitionPointer(assetId.base58())
        }
      }
    }

    /**
     *
     * @param fromAddress
     * @param tos
     * @param privateKeys
     * @param changeAddress
     * @param fees
     * @return
     */
    def transferAsset(fromAddress: String, tos: List[AssetTransferTo], privateKeys: Option[List[PrivateKey]], changeAddress: String, fees: Long) : Hash = {
      import scala.collection.JavaConverters._
      val rawTx: Transaction = OpenAssetsProtocol.get().createTransferTransaction(
        AssetAddress.from(fromAddress), tos.asJava, AssetAddress.from(changeAddress), fees
      )

      val signedTx = Wallet.get.signTransaction(rawTx, Blockchain.get, List.empty, privateKeys, SigHash.ALL)(Blockchain.get.db)

      if (!signedTx.complete) {
        throw new GeneralException(ErrorCode.NoMoreKeys);
      }

      verifyTransaction(signedTx.transaction)
      sendRawTransaction(signedTx.transaction, true)
      // TODO : We can propagte newly created oap transaction here.

      if (!signedTx.complete) {
        throw new OapException(OapException.CANNOT_SIGN_TRANSACTION, "Cannot sign all inputs");
      }
      rawTx.hash
    }
  }

  case class IssueAssetResult(txid : Hash, metadata_hash : String, asset_id : String);

  companion object {
    private var theRpcSubSystem : RpcSubSystem? = null

    fun create(chain : Blockchain, peerCommunicator: PeerCommunicator) : RpcSubSystem {
      theRpcSubSystem = RpcSubSystem(chain.db, chain, peerCommunicator)
      return theRpcSubSystem!!
    }

    fun get() : RpcSubSystem {
      assert(theRpcSubSystem != null)
      return theRpcSubSystem!!
    }
  }
}


