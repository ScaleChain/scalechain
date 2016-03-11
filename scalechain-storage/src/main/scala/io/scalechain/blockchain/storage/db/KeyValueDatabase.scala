package io.scalechain.blockchain.storage.db

import io.scalechain.blockchain.proto.ProtocolMessage

/**
  * Created by kangmo on 3/11/16.
  */
trait KeyValueDatabase {
  def get(key : Array[Byte] ) : Option[Array[Byte]]
  def put(key : Array[Byte], value : Array[Byte] ) : Unit
  def del(key : Array[Byte]) : Unit
  def close() : Unit
}

