package io.scalechain.blockchain.storage.record

import java.io.File

/**
  * Created by mijeong on 2016. 3. 22..
  */

object AccountRecordStorage {
  val FILE_PREFIX = "wallet-"
  val MAX_FILE_SIZE = 1024 * 1024
}

class AccountRecordStorage(directoryPath : File, account: String)
  extends RecordStorage(directoryPath, AccountRecordStorage.FILE_PREFIX + account, AccountRecordStorage.MAX_FILE_SIZE)
