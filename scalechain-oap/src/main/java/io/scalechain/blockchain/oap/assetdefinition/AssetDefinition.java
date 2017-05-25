package io.scalechain.blockchain.oap.assetdefinition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.scalechain.blockchain.oap.exception.OapException;
import io.scalechain.blockchain.oap.wallet.AssetId;
import io.scalechain.crypto.HashFunctions;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shannon on 16. 12. 28.
 */
public class AssetDefinition {
  public static String ASSET_IDS = "asset_ids";
  public static String NAME = "name";
  public static String NAME_SHORT = "name_short";
  private JsonObject jsonObject;

  public AssetDefinition(JsonObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  public List<String> getAssetIds() {
    JsonArray array = jsonObject.get(ASSET_IDS).getAsJsonArray();
    List<String> list = new ArrayList<String>();
    for (JsonElement e : array) {
      list.add(e.getAsString());
    }
    return list;
  }

  public boolean isValid() {
    if (jsonObject == null) return false;
    if (!jsonObject.has(ASSET_IDS)) return false;
    if (!jsonObject.has(NAME)) return false;
    if (jsonObject.get(NAME).getAsString().length() == 0) return false;
    if (!jsonObject.has(NAME_SHORT)) return false;
    if (jsonObject.get(NAME_SHORT).getAsString().length() == 0) return false;
    JsonElement ids = jsonObject.get(ASSET_IDS);
    if (!ids.isJsonArray()) return false;
    if (ids.getAsJsonArray().size() == 0) return false;
    for (JsonElement e : ids.getAsJsonArray()) {
      try {
        // CHECK Asset Id is valid
        AssetId.from(e.getAsString());
      } catch (OapException ex) {
        return false;
      }
    }
    return true;
  }

  public JsonObject toJson() {
    return jsonObject;
  }

  public String toString() {
    return jsonObject.toString();
  }

  public byte[] hash() {
    byte[] bytes;
    try {
      bytes = toString().getBytes("UTF-8");
    } catch (UnsupportedEncodingException e) {
      bytes = toString().getBytes();
    }
    return HashFunctions.hash160(bytes).getValue().getArray();
  }

  public static AssetDefinition from(String value) throws OapException {
    // CONVERT MAP TO JSONOBJECT.
    JsonObject jsonObject = new JsonParser().parse(value).getAsJsonObject();
    // CHECK asset_ids exists.
    if (jsonObject.has(ASSET_IDS)) {
      if (!jsonObject.get(ASSET_IDS).isJsonArray()) throw new OapException(OapException.INVLAID_DEFINITION, "asset_ids should be an array");
      if (jsonObject.get(ASSET_IDS).getAsJsonArray().size() == 0) throw new OapException(OapException.INVLAID_DEFINITION, "asset_ids should contain asset_id");
      for (JsonElement e : jsonObject.get(ASSET_IDS).getAsJsonArray()) {
        try {
          // CHECK Asset Id is valid
          AssetId.from(e.getAsString());
        } catch (Exception ex) {
          throw new OapException(OapException.INVLAID_DEFINITION, "Invalid asset_id:" + e.getAsString());
        }
      }
    } else {
      throw new OapException(OapException.INVLAID_DEFINITION, "Metadata should contain asset_ids");
    }
    if (!jsonObject.has(NAME)) throw new OapException(OapException.INVLAID_DEFINITION, "Metadata has no \"name\" field");
    if (jsonObject.get(NAME).getAsString().length() == 0) throw new OapException(OapException.INVLAID_DEFINITION, "Metadata has empty \"name\" field");
    if (!jsonObject.has(NAME_SHORT)) throw new OapException(OapException.INVLAID_DEFINITION, "Metadata has no \"name_short\" field");
    if (jsonObject.get(NAME_SHORT).getAsString().length() == 0)
      throw new OapException(OapException.INVLAID_DEFINITION, "Metadata has empty \"name_short\" field");
    return new AssetDefinition(jsonObject);
  }

    public static AssetDefinition from(String assetId, String value) throws OapException {
    // CONVERT MAP TO JSONOBJECT.
    JsonObject jsonObject = new JsonParser().parse(value).getAsJsonObject();
    // CHECK asset_ids exists.
    if (jsonObject.has(ASSET_IDS)) {
      if (!jsonObject.get(ASSET_IDS).isJsonArray()) throw new OapException(OapException.INVLAID_DEFINITION, "asset_ids should be an array");
      boolean exists = false;
      for (JsonElement e : jsonObject.get(ASSET_IDS).getAsJsonArray()) {
        if (assetId.equals(e.getAsString())) exists = true;
      }
      if (!exists) throw new OapException(OapException.INVLAID_DEFINITION, "asset_ids should not contain asset_id");
    } else {
      JsonArray assetIds = new JsonArray();
      assetIds.add(assetId);
      jsonObject.add(ASSET_IDS, assetIds);
    }
    if (!jsonObject.has(NAME)) throw new OapException(OapException.INVLAID_DEFINITION, "Metadata has no \"name\" field");
    if (jsonObject.get(NAME).getAsString().length() == 0) throw new OapException(OapException.INVLAID_DEFINITION, "Metadata has empty \"name\" field");
    if (!jsonObject.has(NAME_SHORT)) throw new OapException(OapException.INVLAID_DEFINITION, "Metadata has no \"name_short\" field");
    if (jsonObject.get(NAME_SHORT).getAsString().length() == 0)
      throw new OapException(OapException.INVLAID_DEFINITION, "Metadata has empty \"name_short\" field");
    return new AssetDefinition(jsonObject);
  }
}
