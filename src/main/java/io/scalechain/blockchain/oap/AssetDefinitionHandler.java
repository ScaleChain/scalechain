package io.scalechain.blockchain.oap;

import io.scalechain.blockchain.oap.assetdefinition.AssetDefinition;
import io.scalechain.blockchain.oap.assetdefinition.AssetDefinitionPointer;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.util.Pair;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.util.HexUtil;
import scala.Option;

/**
 * Created by shannon on 17. 1. 3.
 */
public class AssetDefinitionHandler {
  private static AssetDefinitionHandler instance = new AssetDefinitionHandler();
  public static AssetDefinitionHandler get() {
    return instance;
  }

  //
  // API Methods
  //
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
    OapStorage storage = OpenAssetsProtocol.get().storage();

    AssetDefinition definition = AssetDefinition.from(assetId.base58(), metadata);
    AssetDefinitionPointer pointer = AssetDefinitionPointer.create(AssetDefinitionPointer.HASH_POINTER, definition.hash());
    if (storage.getAssetDefinition(pointer).isDefined()) {
      throw new OapException(OapException.DEFINITION_EXISTS, "Asset Definition for " + assetId + " already exists");
    }
    storage.putAssetDefinition(pointer, definition.toString());
    storage.putAssetDefinitionPointer(assetId.base58(), pointer);
    return new Pair<AssetDefinitionPointer, AssetDefinition>(pointer, definition);
  }

  /**
   * retreive AssetDefinition from storage.
   *
   * GetAssetDefinition API calls this method.
   *
   * @param hashOrAssetId
   * @return
   * @throws OapException
   */
  public AssetDefinition getAssetDefintion(String hashOrAssetId) throws OapException {
    OapStorage storage = OpenAssetsProtocol.get().storage();

    AssetDefinitionPointer pointer = null;
    AssetId assetId = null;
    try {
      assetId = AssetId.from(hashOrAssetId);
    } catch (OapException ex) {
    }
    if (assetId != null) {
      Option<AssetDefinitionPointer> pointerOption = storage.getAssetDefinitionPointer(hashOrAssetId);
      if (pointerOption.isDefined()) pointer = pointerOption.get();
    } else {
      pointer = AssetDefinitionPointer.from(HexUtil.bytes(hashOrAssetId));
    }
    if (pointer == null) {
      throw new OapException(OapException.DEFINITIO_NOT_EXIST, "Asset Definition for " + hashOrAssetId + " does not exist");
    }
    Option<String> assetDefinitionOption = storage.getAssetDefinition(pointer);
    if (assetDefinitionOption.isDefined()) {
      return AssetDefinition.from(assetDefinitionOption.get());
    } else {
      throw new OapException(OapException.DEFINITIO_NOT_EXIST, "Asset Definition File for " + hashOrAssetId + " does not exist");
    }
  }

  //
  // Internal Methods
  //
  public Option<AssetDefinition> getAssetDefintion(AssetDefinitionPointer pointer) throws OapException {
    OapStorage storage = OpenAssetsProtocol.get().storage();

    Option<String> assetDefinitionOption = storage.getAssetDefinition(pointer);
    if (assetDefinitionOption.isDefined()) {
      return Option.apply(AssetDefinition.from(assetDefinitionOption.get()));
    } else {
      return Option.empty();
    }
  }

  /**
   * returns Asset Definition Pointer for Asset Id
   *
   * @param assetId
   * @return
   * @throws OapException
   */
  public AssetDefinitionPointer getAssetDefinitionPointer(AssetId assetId) throws OapException {
    OapStorage storage = OpenAssetsProtocol.get().storage();

    // GET pointer for assetId
    Option<AssetDefinitionPointer> pointerOption = storage.getAssetDefinitionPointer(assetId.base58());
    if (pointerOption.isDefined()) {
      return pointerOption.get();
    }
    return null;
  }
}
