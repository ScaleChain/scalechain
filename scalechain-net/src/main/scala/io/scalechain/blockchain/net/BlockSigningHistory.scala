package io.scalechain.blockchain.net

import java.util.concurrent.TimeUnit
import com.google.common.cache.{Cache, CacheBuilder, CacheLoader, LoadingCache}
;

import io.scalechain.blockchain.proto.Hash

object BlockSigningHistory extends BlockSigningHistory(10, TimeUnit.MINUTES)

/**
  * Created by kangmo on 7/11/16.
  */
class BlockSigningHistory(duration: Long, unit: TimeUnit) {

  val cache : Cache[Hash, String] =
    CacheBuilder.newBuilder().expireAfterWrite(duration, unit).
      build[Hash,String]

  def signedOn(prevBlockHash : Hash) : Unit = {
    synchronized {
      cache.put(prevBlockHash, "1")
    }
  }

  def didSignOn(prevBlockHash : Hash) : Boolean = {
    synchronized {
      cache.getIfPresent(prevBlockHash) != null
    }
  }
}
