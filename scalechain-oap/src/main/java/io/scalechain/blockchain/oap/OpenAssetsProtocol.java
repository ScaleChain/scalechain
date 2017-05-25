package io.scalechain.blockchain.oap;

import io.scalechain.blockchain.oap.assetdefinition.AssetDefinition;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer;
import io.scalechain.blockchain.oap.blockchain.IBlockchainInterface;
import io.scalechain.blockchain.oap.blockchain.IWalletInterface;
import io.scalechain.blockchain.oap.blockchain.OapBlockchain;
import io.scalechain.blockchain.oap.blockchain.OapWallet;
import io.scalechain.blockchain.oap.coloring.ColoringEngine;
import io.scalechain.blockchain.oap.command.AssetTransferTo;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.wallet.AssetAddress;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.oap.wallet.OapTransactionDescriptor;
import io.scalechain.blockchain.proto.Transaction;
import io.scalechain.blockchain.transaction.CoinAddress;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

import kotlin.Pair;

/**
 * Hold all Open Assets Protocol related Object instance.
 * Acts as a portal of Open Assets Protocol APIs.
 *
 * Created by shannon on 16. 11. 30.
 */
public abstract class OpenAssetsProtocol implements  IOapConstants {
  protected static OpenAssetsProtocol instance = null;

  public static OpenAssetsProtocol create(IBlockchainInterface blockchainView, IWalletInterface walletInterface, File oapStoragePath) throws OapException {
    instance = new OpenAssetsProtocolImpl(blockchainView, walletInterface, oapStoragePath);
    return instance;
  }

  public static OpenAssetsProtocol get() {
    return instance;
  }

  public abstract OapWallet wallet();
  public abstract OapBlockchain chain();
  public abstract OapStorage storage();
  public abstract ColoringEngine coloringEngine();

  /**
   * creates an Asset Issuance Transaction, signs and pushes it to the blockchain.
   * <p>
   * "issuerAddress" and "changeAddress" are Coin Addresses in Base58 check format.
   * "toAddress" is Asset Address. Issued Assets are tranfered to toAddress.
   * "pointer" is an Asset Defitiion Pointer.
   * "privateKeys" is an array of Private Keys needed to sign transaction.
   * Coin Changes are transfered to "changeAddress"
   * <p>
   *
   * @param issuerAddress
   * @param toAddress
   * @param assetId
   * @param quantity
   * @param pointer
   * @param changeAddress
   * @param fees
   * @return
   * @throws OapException
   */
  public Transaction createIssuanceTransaction(
    CoinAddress issuerAddress,
    AssetAddress toAddress,
    AssetId assetId,
    int quantity,
    AssetDefinitionPointer pointer,
    CoinAddress changeAddress,
    long fees
  ) throws OapException {
   return IssueAssetHandler.get().createIssuanceTransaction(issuerAddress, toAddress, assetId, quantity, pointer, changeAddress, fees);
  }

  /**
   * creates an Asset Transfer transaction, signs and pushes it to the blockchain.
   *
   * "fromAddress" and "changeAddress" are Asset Addresses.
   * All Asset Changes and Coin Changes are transfered to changeAddress.
   * <p>
   * "to_address" of AssetTransferTo is an Asset Adddress. If not, an OapException is thrown.
   * <p>
   *
   * @param fromAddress
   * @param transfers
   * @param changeAddress
   * @param fees
   * @return
   * @throws OapException
   */
  public Transaction createTransferTransaction(
    AssetAddress fromAddress,
    List<AssetTransferTo> transfers,
    AssetAddress changeAddress, long fees
  ) throws OapException {
    return TransferAssetHandler.get().createTransferTransaction(fromAddress, transfers, changeAddress, fees);
  }

  /**
   * calculate the Balance of an Account.
   *
   * "includeAssets" flag controlls whether the balance of each asset included.
   *
   * @param accountOption
   * @param minConf
   * @param includeWatchOnly
   * @param assetIds
   * @return
   * @throws OapException
   */
  public List<AssetBalanceDesc> getAssetBalance(
    String accountOption,
    long minConf,
    boolean includeWatchOnly,
    List<String> assetIds
  ) throws OapException {
    return WalletHandler.get().getAssetBalance(accountOption, minConf, includeWatchOnly, assetIds);
  }

  public BigDecimal getBalance(
    String accountOption,
    long minConf,
    boolean includeWatchOnly
  ) throws OapException {
    return WalletHandler.get().getBalance(accountOption, minConf, includeWatchOnly);
  }


  /**
   * lists recent transaction of an Account.
   *
   * @param account
   * @param count
   * @param skip
   * @param includeWatchOnly
   * @return
   * @throws OapException
   */
  public List<OapTransactionDescriptor> listTransactions(
    String account,
    int count,
    long skip,
    boolean includeWatchOnly
  ) throws OapException {
    return WalletHandler.get().listTransactionsWithAsset(account, count, skip, includeWatchOnly);
  }

  /**
   * returns addresses of an Account
   *
   * @param acountOption
   * @param includeWatchOnly
   * @return
   * @throws OapException
   */
  public List<CoinAddress> getAddressesByAccount(String acountOption, boolean includeWatchOnly) throws OapException {
    return WalletHandler.get().getAddressesByAccount(acountOption, includeWatchOnly);
  }

  //
  // Methods for commands handling Asset Definition Management API
  //
  /**
   * returns the Asset Defition File of given hash or Asset ID.
   *
   * If no Asset Definition for given hash or AssetID OapException is thrown
   *
   * @param hashOrAssetId
   * @return
   * @throws OapException
   */
  public AssetDefinition getAssetDefinition(String hashOrAssetId) throws OapException {
    return AssetDefinitionHandler.get().getAssetDefinition(hashOrAssetId);
  };

  /**
   * create an Asset Definition File for an Asset Id
   * "metadata" can be arbitray json objects with name and name_short feilds. "asset_ids" is set to [ asset_id ].
   *
   * returan value is a json String that contains Asset ID, hex encoded hash of the Asset Definition file and Asset Definition File
   *
   * @param assetId
   * @param metadata
   * @return
   * @throws OapException
   */
  public Pair<AssetDefinitionPointer, AssetDefinition> createAssetDefinition(AssetId assetId, String metadata) throws OapException {
    return AssetDefinitionHandler.get().createAssetDefinition(assetId, metadata);
  }
}
