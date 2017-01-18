package io.scalechain.blockchain.oap;

import io.scalechain.blockchain.oap.blockchain.IBlockchainInterface;
import io.scalechain.blockchain.oap.blockchain.IWalletInterface;
import io.scalechain.blockchain.oap.blockchain.OapBlockchain;
import io.scalechain.blockchain.oap.blockchain.OapWallet;
import io.scalechain.blockchain.oap.coloring.ColoringEngine;
import io.scalechain.blockchain.oap.exception.OapException;

import java.io.File;

/**
 * Created by shannon on 16. 11. 16.
 */
public class OpenAssetsProtocolImpl extends OpenAssetsProtocol {
  OapWallet      wallet;
  OapBlockchain  chain;
  OapStorage     storage;
  ColoringEngine coloringEngine;

  @Override
  public OapWallet     wallet() {
    return wallet;
  }
  @Override
  public OapBlockchain chain() {
    return chain;
  }
  @Override
  public OapStorage    storage() { return storage; }
  @Override
  public ColoringEngine coloringEngine() {
    return coloringEngine;
  }

  protected OpenAssetsProtocolImpl(IBlockchainInterface chainView, IWalletInterface wallet, File storagePath) throws OapException {
    this.wallet         = OapWallet.create(wallet);
    this.chain          = OapBlockchain.create(chainView);
    this.storage        = OapStorage.create(storagePath);
    this.coloringEngine = ColoringEngine.create();
  }
}
