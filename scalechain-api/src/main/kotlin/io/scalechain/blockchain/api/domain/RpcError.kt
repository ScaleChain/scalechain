package io.scalechain.blockchain.api.domain

// TODO : Make sure the format matches the one used by Bitcoin.
data class RpcError(val code : Int, val message : String, val data : String) {

  companion object {
    data class ErrorDescription( val code : Int, val messagePrefix : String)

    // Standard JSON-RPC 2.0 errors.
    val RPC_INVALID_REQUEST              = ErrorDescription(-32600, "RPC Invalid Request" )
    val RPC_METHOD_NOT_FOUND             = ErrorDescription(-32601, "RPC Method Not Found")
    val RPC_INVALID_PARAMS               = ErrorDescription(-32602, "RPC Invalid Params"  )
    val RPC_INTERNAL_ERROR               = ErrorDescription(-32603, "RPC Internal Error"  )
    // fundrawtransaction

    val RPC_PARSE_ERROR                  = ErrorDescription(-30700, "RPC Parse Error"     )
    // HTTPReq_JSONRPC : "Parse error"
    // HTTPReq_JSONRPC : "Top-level object parse error"
    // HTTPReq_JSONRPC : (catch exception)

    // General application defined errors.
    val RPC_MISC_ERROR                   = ErrorDescription( -1, "RPC Misc Error"                  ) // General exception handling
    // setaccount : "setaccount can only be used with own address"

    // TODO : Bitcoin core(rpcwallet.cpp) has a flag called okSafeMode for each command. Do we also need one?
    val RPC_FORBIDDEN_BY_SAFE_MODE       = ErrorDescription( -2, "RPC Forbidden By Safe Mode"      ) // Unable to run a command that is not allowed in safe mode when the server is in safe mode.
    val RPC_TYPE_ERROR                   = ErrorDescription( -3, "RPC Type Error."                 ) // Unexpected type was passed as parameter.
    // dumpprivkey : "Address does not refer to a key"
    // sendtoaddress : "Invalid amount for send"
    // signmessage : "Invalid address"
    // signmessage : "Address does not refer to key"
    // movecmd : "Invalid amount for send"
    // sendfrom : "Invalid amount for send"

    val RPC_INVALID_ADDRESS_OR_KEY       = ErrorDescription( -5, "RPC Invalid Addres Or Key"       ) // Invalid address or key.
    // importprivkey : "Invalid private key encoding"
    // importprivkey : "Private key outside allowed range"
    // importaddress : "Cannot use the p2sh flag with an address - use a script instead"
    // importaddress :  "Invalid Bitcoin address or script"
    // importpubkey : "Pubkey must be a hex string"
    // importpubkey : "Pubkey is not a valid public key"
    // dumpprivkey : "Invalid Bitcoin address"
    // setaccount : "Invalid Bitcoin address"
    // getaccount : "Invalid Bitcoin address"
    // sendtoaddress : "Invalid Bitcoin address"
    // signmessage : "Sign failed"
    // getreceivedbyaddress : "Invalid Bitcoin address"
    // sendfrom : "Invalid Bitcoin address"
    // getransaction : "Invalid or non-wallet transaction id"
    // abandontransaction : "Invalid or non-wallet transaction id"
    // abandontransaction : "Transaction not eligible for abandonment"
    // listunspent : "Invalid Bitcoin address"

    val RPC_OUT_OF_MEMORY                = ErrorDescription( -7, "RPC Out Of Memory"               ) // Ran out of memory during operation.
    val RPC_INVALID_PARAMETER            = ErrorDescription( -8, "RPC Invalid Parameter"           ) // Invalid, missing or duplicate parameter.
    // importwallet : "Cannot open wallet dump file"
    // dumpwallet : "Cannot open wallet dump file"
    // SendMoney : "Invalid amount"
    // listtransactions : "Negative count"
    // listtransactions : "Negative from"
    // listsinceblock : "Invalid parameter"
    // keypoolrefill : "Invalid parameter, expected valid size."
    // lockunspent : "Invalid parameter, expected object"
    // lockunspent : "Invalid parameter, expected hex txid"
    // lockunspent : "Invalid parameter, vout must be positive"
    // listunspent : "Invalid parameter, duplicated address : "
    // fundrawtransaction : "TX must have at least one output"

    val RPC_DATABASE_ERROR               = ErrorDescription(-20, "RPC Database Error"              ) // Database error.
    // movecmd : "database error"

    val RPC_DESERIALIZATION_ERROR        = ErrorDescription(-22, "RPC Deserialization Error"       ) // Error parsing or validating structure in raw format.
    // fundrawtransaction : "TX decode failed"

    val RPC_VERIFY_ERROR                 = ErrorDescription(-25, "RPC Verify Error"                ) // General error during transaction or block submission.
    val RPC_VERIFY_REJECTED              = ErrorDescription(-26, "RPC Verify Rejected"             ) // Transaction or block was rejected by network rules.
    val RPC_VERIFY_ALREADY_IN_CHAIN      = ErrorDescription(-27, "RPC Verify Already In Chain"     ) // Transaction already in chain
    val RPC_IN_WARMUP                    = ErrorDescription(-28, "RPC In Warmup"                   ) // Client still warming up.

    // P2P client errors.
    val RPC_CLIENT_NOT_CONNECTED         = ErrorDescription( -9, "RPC Client Not Connected"        ) // ScaleChain is not connected.
    val RPC_CLIENT_IN_INITIAL_DOWNLOAD   = ErrorDescription(-10, "RPC Client In Initial Download"  ) // Still downloading initial blocks
    val RPC_CLIENT_NODE_ALREADY_ADDED    = ErrorDescription(-23, "RPC Client Node Already Added"   ) // Node is already added.
    val RPC_CLIENT_NODE_NOT_ADDED        = ErrorDescription(-24, "RPC Client Node Not Added"       ) // Node has not been added before.
    val RPC_CLIENT_NODE_NOT_CONNECTED    = ErrorDescription(-29, "RPC Client Node Not Connected"   ) // Node to disconnect not found in connected nodes.
    val RPC_CLIENT_INVALID_IP_OR_SUBNET  = ErrorDescription(-30, "RPC Client Invalid IP or Subnet" ) // Invalid IP/Subnet

    // Wallet errors
    val RPC_WALLET_ERROR                 = ErrorDescription( -4, "RPC Wallet Error"                ) // Unspecified problem with wallet ( key not found, etc.)
    // importprivkey : "Rescan is disabled in pruned mode"
    // importprivkey : "Error adding key to wallet"
    // importScript : "The wallet already contains the private key for this address or script"
    // importScript : "Error adding address to wallet"
    // importScript : "Error adding p2sh redeemScript to wallet"
    // importaddress : "Rescan is disabled in pruned mode"
    // importpubkey : "Rescan is disabled in pruned mode"
    // importwallet : "Importing wallets is disabled in pruned mode"
    // importwallet : "Error adding some keys to wallet"
    // dumpprivkey : "Private key for address " + strAddress + " is not known"
    // SendMoney : variable
    // SendMoney : "Error: The transaction was rejected! This might happen if some of the coins in your wallet were already spent, such as if you used a copy of wallet.dat and coins were spent in the copy but not marked as spent here."
    // signmessage : "Private key not available"
    // sendmany : "Transaction commit failed"
    // backupwallet : "Error: Wallet backup failed!"
    // keypoolrefill : "Error refreshing keypool."

    val RPC_WALLET_INSUFFICIENT_FUNDS    = ErrorDescription( -6, "RPC Wallet Insufficient Funds"   ) // Not enough funds in wallet or account.
    // SendMoney : "Insufficient funds"
    // sendfrom : "Account has insufficient funds"
    // sendmany : "Account has insufficient funds"
    // sendmany : variable

    val RPC_WALLET_INVALID_ACCOUNT_NAME  = ErrorDescription(-11, "RPC Wallet Invalid Account Name" ) // Invalid account name
    // AccountFromValue : "Invalid account name"

    val RPC_WALLET_KEYPOOL_RAN_OUT       = ErrorDescription(-12, "RPC Wallet Keypool Ran Out"      ) // Keypool ran out, call keypoolrefill first.
    // getnewaddress : "Error: Keypool ran out, please call keypoolrefill first"
    // getaccountaddress : "Error: Keypool ran out, please call keypoolrefill first"
    // getrawchangeaddress : "Error: Keypool ran out, please call keypoolrefill first"

    val RPC_WALLET_UNLOCK_NEEDED         = ErrorDescription(-13, "RPC Wallet Unlock Needed"        ) // Enter the wallet passphrase with walletpassphrase first.
    // EnsureWalletIsUnlocked : "Error: Please enter the wallet passphrase with walletpassphrase first."

    val RPC_WALLET_PASSPHRASE_INCORRECT  = ErrorDescription(-14, "RPC Wallet Passphrase Incorrect" ) // The wallet passphrase entered was incorrect.
    // walletpassphrase : "Error: The wallet passphrase entered was incorrect."
    // walletpassphrasechange : "Error: The wallet passphrase entered was incorrect."
    val RPC_WALLET_WRONG_ENC_STATE       = ErrorDescription(-15, "RPC Wallet Wrong Enc State"      ) // Command given in wrong wallet encryption state( encrypting an encrypted wallet, etc.)
    // walletpassphrase : "Error: running with an unencrypted wallet, but walletpassphrase was called."
    // walletpassphrasechange : "Error: running with an unencrypted wallet, but walletpassphrasechange was called."
    // walletlock : "Error: running with an unencrypted wallet, but walletlock was called."
    // encryptwallet : "Error: running with an encrypted wallet, but encryptwallet was called."

    val RPC_WALLET_ENCRYPTION_FAILED     = ErrorDescription(-16, "RPC Wallet Encryption Failed"    ) // Failed to encrypt the wallet.
    // encryptwallet : "Error: Failed to encrypt the wallet."

    val RPC_WALLET_ALREADTY_UNLOCKED     = ErrorDescription(-17, "RPC Wallet Already Unlocked"     ) // Wallet is already unlocked.
  }
}

