package io.scalechain.blockchain.oap.sampledata;

import io.scalechain.blockchain.oap.assetdefinition.AssetDefinition;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.wallet.AssetAddress;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.blockchain.transaction.CoinAddress;
import io.scalechain.blockchain.transaction.ParsedPubKeyScript;
import io.scalechain.blockchain.transaction.PrivateKey;
import io.scalechain.blockchain.transaction.PublicKey;

/**
 * Created by shannon on 17. 1. 9.
 */

//
// Address and related data
//
public class AddressData {
  public PrivateKey privateKey;
  public PublicKey publicKey;
  public ParsedPubKeyScript pubKeyScript;
  public CoinAddress address;
  public AssetAddress assetAddress;
  public AssetId assetId;
  public AssetDefinitionPointer assetDefinitionPointer;
  public String assetDefinition;

  public AddressData(PrivateKey privateKey, PublicKey publicKey, ParsedPubKeyScript pubKeyScript, CoinAddress address, AssetAddress assetAddress, AssetId assetId) {
    this.privateKey = privateKey;
    this.publicKey = publicKey;
    this.pubKeyScript = pubKeyScript;
    this.address = address;
    this.assetAddress = assetAddress;
    this.assetId = assetId;
    try {
      this.assetDefinition = createAssetDefinition(assetId.base58());
      AssetDefinition definition = AssetDefinition.from(this.assetDefinition);
      this.assetDefinitionPointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    } catch (OapException e) {
    }
  }

  public static AddressData from(CoinAddress address) {
    try {
      return new AddressData(null, null, ParsedPubKeyScript.from(address.lockingScript()), address, AssetAddress.fromCoinAddress(address), AssetId.from(address));
    } catch(Exception e) {
      return new AddressData(null, null, ParsedPubKeyScript.from(address.lockingScript()), address, null, null);
    }
  }

  private String createAssetDefinition(String assetId) {
    String s =
      "{" +
        "  \"asset_ids\":[\"" + assetId + "\"]," +
        "  \"contact_url\":\"https://api.scalechain.io/assets/" + assetId + "\"," +
        "  \"name\":\"OAP Test Asset 0\"," +
        "  \"name_short\":\"TestAsset0\"," +
        "  \"issuer\":\"ScaleChain\"," +
        "  \"description\":\"Test Asset 0, for test purpose only.\"," +
        "  \"descripton_mime\":\"text/plain\"," +
        "  \"type\":\"other\"," +
        "  \"divisibility\":0," +
        "  \"link_to_website\":false," +
        "  \"icon_url\":\"https://api.scalechain.io/profile/icon/" + assetId + ".jpg\"," +
        "  \"image_url\":\"https://api.scalechain.io/profile/image/" + assetId + ".jpg\"," +
        "  \"version\":\"1.0\"" +
        "}";
    return s;
  }
}
