package io.scalechain.blockchain.storage

import java.io.File

import io.scalechain.blockchain.proto.codec.{BitcoinProtocol, NetworkProtocol}
import io.scalechain.blockchain.proto.{BlockHeader, ProtocolMessage, Block}
import io.scalechain.blockchain.storage.RecordFile.RecordLocator
import io.scalechain.blockchain.{ErrorCode, BlockStorageException}
import org.slf4j.LoggerFactory

import scala.collection.mutable


/** Block file name extractor
  * http://www.tutorialspoint.com/scala/scala_extractors.htm
  */
object BlockFileName {
  val PREFIX_LENGTH = 3
  val POSTFIX = ".dat"
  def apply(prefix : String, fileNumber : Int) = {
    assert(prefix.length == PREFIX_LENGTH)
    s"${prefix}${"%05d".format(fileNumber)}.dat"
  }
  def unapply(fileName : String) : Option[(String, Int)] = {
    if (fileName.endsWith(POSTFIX)) {
      val prefix = fileName.substring(0, PREFIX_LENGTH)
      val fileNumberPart =
        fileName.substring(
          PREFIX_LENGTH, // start offset - inclusive
          fileName.length - POSTFIX.length) // end offset - exclusive
      try {
        Some(prefix, fileNumberPart.toInt)
      } catch {
        case e : NumberFormatException => {
          None
        }
      }
    } else {
      None
    }
  }
}

object RecordStorage {
  case class FileRecordLocator(fileIndex : Int, recordLocator : RecordLocator)
}

/**
  * Created by kangmo on 2/15/16.
  */
class RecordStorage[T <: ProtocolMessage ](directoryPath : File, filePrefix : String, maxFileSize : Long)(implicit protocol : NetworkProtocol ) {
  val logger = LoggerFactory.getLogger(classOf[RecordStorage[ProtocolMessage]])

  import RecordStorage._
  val files = mutable.IndexedSeq[ RecordFile[T] ]()

  if (directoryPath.exists()) {
    // For each file in the path
    for ( file <- directoryPath.listFiles.sortBy(_.getName() ) ) {
      file.getName() match {
        case BlockFileName(prefix, fileNumber) => {
          if (prefix == filePrefix) {
            if (files.length == fileNumber) {
              files(files.length) = newFile(file)
            } else {
              logger.error(s"Invalid Block File Number. Expected : ${files.length}, Actual : ${fileNumber}")
              throw new BlockStorageException(ErrorCode.InvalidFileNumber)
            }
          }
        }
      }
    }
  } else {
    throw new BlockStorageException(ErrorCode.BlockFilePathNotExists)
  }

  def lastFile = files(lastFileIndex)

  /** Because we are appending records, flushing the last file is enough.
    * We also flush the last file when a new file is added, so flushing the current last file is enough.
    */
  def flush = lastFile.flush

  def lastFileIndex = files.length-1

  def newFile(blockFile : File) : RecordFile[T] = new RecordFile[T](blockFile, maxFileSize, "block" )

  def newFile() : RecordFile[T] = {
    val fileNumber = lastFileIndex + 1
    val blockFile = new File(BlockFileName(filePrefix, fileNumber))
    newFile(blockFile)
  }

  /** Add a new file. Also flush the last file, so that we do not lose any file contents when the system crashes.
    */
  def addNewFile() = {
    lastFile.flush
    files(files.length) = newFile()
  }

  def appendRecord(record : T): FileRecordLocator = {
    try {
      val recordLocator = lastFile.appendRecord(record)
      FileRecordLocator( lastFileIndex, recordLocator )
    } catch {
      case e : BlockStorageException => {
        if (e.code == ErrorCode.OutOfFileSpace) {
          addNewFile
          val recordLocator = lastFile.appendRecord(record)
          FileRecordLocator( lastFileIndex, recordLocator )
        } else {
          throw e
        }
      }
    }
  }

  def readRecord(locator : FileRecordLocator) : T = {
    if (locator.fileIndex < 0 || locator.fileIndex >= files.length) {
      throw new BlockStorageException(ErrorCode.InvalidFileNumber)
    }

    val file = files(locator.fileIndex)

    file.readRecord(locator.recordLocator)
  }
}

object HeaderRecordStorage {
  val FILE_PREFIX = "hdr"
  val MAX_FILE_SIZE = 1024 * 1024 * 10
}

class HeaderRecordStorage(directoryPath : File) extends
  RecordStorage[BlockHeader](directoryPath, HeaderRecordStorage.FILE_PREFIX, HeaderRecordStorage.MAX_FILE_SIZE)(new BitcoinProtocol())


object BlockRecordStorage {
  val FILE_PREFIX = "blk"
  val MAX_FILE_SIZE = 1024 * 1024 * 100
}

class BlockRecordStorage(directoryPath : File) extends
  RecordStorage[Block](directoryPath, BlockRecordStorage.FILE_PREFIX, BlockRecordStorage.MAX_FILE_SIZE)(new BitcoinProtocol())