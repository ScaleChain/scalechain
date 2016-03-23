package io.scalechain.blockchain.storage.index

import java.io.File
import java.nio.ByteBuffer

import com.datastax.driver.core.{Row, TableMetadata, KeyspaceMetadata, Cluster}
import org.cassandraunit.utils.EmbeddedCassandraServerHelper


object CassandraDatabase {
  val KEYSPACE_NAME = "scalechain"
  val TABLE_NAME = "kvtable"
}
class CassandraDatabase(path : File, tableName : String = CassandraDatabase.TABLE_NAME) extends KeyValueDatabase {
  import CassandraDatabase._

  EmbeddedCassandraServerHelper.startEmbeddedCassandra()

  val cluster = Cluster.builder().addContactPoint("127.0.0.1").withPort(9142).build()
  val session = cluster.connect()

  session.execute(s"CREATE KEYSPACE IF NOT EXISTS ${KEYSPACE_NAME} WITH REPLICATION ={ 'class' : 'SimpleStrategy', 'replication_factor' : 3 }")
  session.execute(s"USE ${KEYSPACE_NAME}")

  session.execute(s"CREATE TABLE IF NOT EXISTS ${tableName} (key blob primary key, value blob)")

  // FOR UNIT TESTS
  protected[index] def truncateTable() : Unit = {
    session.execute(s"TRUNCATE TABLE ${tableName}")
  }

  val preparedStmtGet = session.prepare(s"SELECT value FROM ${tableName} WHERE key = :key")
  val preparedStmtPut = session.prepare(s"INSERT INTO ${tableName} (key, value) VALUES( :key, :value)")
  val preparedStmtDel = session.prepare(s"DELETE FROM ${tableName} WHERE key = :key")

  def get(key : Array[Byte] ) : Option[Array[Byte]] = {
    val row = session.execute( preparedStmtGet.bind(ByteBuffer.wrap(key))).all()
    if ( row.isEmpty ) {
      None
    } else {
      Some(row.get(0).getBytes("value").array())
    }
  }

  def put(key : Array[Byte], value : Array[Byte] ) : Unit = {
    session.execute( preparedStmtPut.bind(ByteBuffer.wrap(key), ByteBuffer.wrap(value)))
  }

  def del(key : Array[Byte]) : Unit = {
    session.execute( preparedStmtDel.bind(ByteBuffer.wrap(key)))
  }

  def close() : Unit = {
    session.close
    cluster.close
  }
}
