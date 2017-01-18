package io.scalechain.blockchain.oap.exception;

/**
 * Created by shannon on 16. 12. 8.
 */
public class OapException extends Exception {
  public static final int ERROR_CODE_BASE = -9000;


  public static final int INTERNAL_ERROR = ERROR_CODE_BASE - 1;    //     throw new OapException("Asset Id is undefined");
  public static final int INVALID_ARGUMENT = ERROR_CODE_BASE - 2;    //     throw new OapException("Asset Id is undefined");
  public static final int NOT_ENOUGH_COIN = ERROR_CODE_BASE - 3;

  public static final int NO_ASSET = ERROR_CODE_BASE - 4;    // "Address has no asset "
  public static final int NOT_ENOUGH_ASSET = ERROR_CODE_BASE - 5;    // "Not enoough asset " + assetId.base58() + " for transfer"
  public static final int FEES_TOO_SMALL = ERROR_CODE_BASE - 6;
  ;   // "Fees are too small"
  public static final int INVALID_QUANTITY = ERROR_CODE_BASE - 7;
  ;   // "Invalid quantity: " + quantity

  public static final int DEFINITION_EXISTS = ERROR_CODE_BASE - 8;    // OapException(OapException.DEFINITION_ERROR, "Asset Definition for " + assetId + " already exists");
  public static final int DEFINITIO_NOT_EXIST = ERROR_CODE_BASE - 9;    // OapException("Asset Definition for " + hashOrAssetId + " does not exist");
  public static final int INVLAID_DEFINITION = ERROR_CODE_BASE - 9; //OapException(OapException.INVLAID_DEFINITION, "asset_ids should be an array");
  public static final int DEFINITION_POINTER_ERROR = ERROR_CODE_BASE - 9; //OapException(OapException.DEFINITION_POINTER_ERROR, "Invalid hash value");
  public static final int INVALID_ADDRESS = ERROR_CODE_BASE - 10;    // OapException(OapException.INVALID_ADDRESS, "Unupported version byte " + coinAddressVersion);
  public static final int INVALID_PRIVATE_KEY = ERROR_CODE_BASE -11;
  public static final int CANNOT_SIGN_TRANSACTION = ERROR_CODE_BASE -13;
  public static final int COLORING_ERROR = ERROR_CODE_BASE - 14;    // OapException(OapException.COLORING_ERROR, "Not OAP Issuance Tx.");
  // OapException(OapException.COLORING_ERROR, "Invalid TX ID: " + outPoint.transactionHash().toHex());
  // OapException(OapException.COLORING_ERROR, "Not a LEB128 encoded data");
  public static final int INVALID_ASSET_ID = ERROR_CODE_BASE - 15;    // OapException(OapException.INVALID_ASSET_ID, "Invalid AssetId: " + base58, e);

  int errorCode;

  public OapException(int errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  public OapException(int errorCode, String message, Throwable t) {
    super(message, t);
    this.errorCode = errorCode;

  }

  public int getErrorCode() {
    return errorCode;
  }
}
