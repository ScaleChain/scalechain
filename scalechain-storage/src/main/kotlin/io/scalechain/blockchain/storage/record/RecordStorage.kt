package io.scalechain.blockchain.storage.record

import java.io.File

import com.typesafe.scalalogging.Logger
import io.scalechain.blockchain.proto.codec.MessagePartCodec
import io.scalechain.blockchain.proto.{FileRecordLocator, ProtocolMessage}
import io.scalechain.blockchain.{BlockStorageException, ErrorCode}
import org.slf4j.LoggerFactory

import scala.collection.mutable

/** Maintains a list of record files.
  *
  * Why?
  *   A record file has a maximum size. If a file reaches the maximum size, we need to add a new record file.
  *   Record storage keeps track of multiple record files, enables us to search a record by a record locator,
  *   which hash the file index of the multiple record files.
  */
class RecordStorage(directoryPath : File, filePrefix : String, maxFileSize : Long) {
  private val logger = Logger( LoggerFactory.getLogger(classOf<RecordStorage>) )

  val files = mutable.ArrayBuffer.empty<RecordFile>

  if (directoryPath.exists()) {
    // For each file in the path
    val fileList = directoryPath.listFiles
    if (fileList.isEmpty) {
      // Do nothing. no file exists
    } else {
      for ( file <- fileList.sortBy(_.getName() ) ) {
        file.getName() match {
          case BlockFileName(prefix, fileNumber) => {
            if (prefix == filePrefix) {
              if (files.length == fileNumber) {
                files += newFile(file)
              } else {
                logger.error(s"Invalid Block File Number. Expected : ${files.length}, Actual : ${fileNumber}")
                throw BlockStorageException(ErrorCode.InvalidFileNumber)
              }
            }
          }
          case _ => {
            // Ignore files that are not matching the block file name format.
          }
        }
      }
    }
  } else {
    throw BlockStorageException(ErrorCode.BlockFilePathNotExists)
  }

  /** If there is no file at all, add a file.
   */
  if (files.isEmpty)
    files += newFile()

  protected<storage> fun lastFile = files(lastFileIndex)

  /** Because we are appending records, flushing the last file is enough.
    * We also flush the last file when a file is added, so flushing the current last file is enough.
    */
  protected<storage> fun flush = lastFile.flush

  protected<storage> fun lastFileIndex = files.length-1

  protected<storage> fun newFile(blockFile : File) : RecordFile = RecordFile(blockFile, maxFileSize )

  protected<storage> fun newFile() : RecordFile {
    val fileNumber = lastFileIndex + 1
    val blockFile = File( directoryPath.getAbsolutePath + File.separatorChar + BlockFileName(filePrefix, fileNumber))
    newFile(blockFile)
  }

  /** Add a file. Also flush the last file, so that we do not lose any file contents when the system crashes.
    */
  protected<storage> fun addNewFile() {
    lastFile.flush
    files += newFile()
  }

  // TODO : Make FileRecordLocator to have a type parameter, T so that we can have a compile error when an incorrect codec is used for a record locator.

  fun appendRecord<T <: ProtocolMessage >(record : T)(implicit codec : MessagePartCodec<T>): FileRecordLocator {
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

  fun readRecord<T <: ProtocolMessage >(locator : FileRecordLocator)(implicit codec : MessagePartCodec<T>) : T {
    if (locator.fileIndex < 0 || locator.fileIndex >= files.length) {
      throw BlockStorageException(ErrorCode.InvalidFileNumber)
    }

    val file = files(locator.fileIndex)

    file.readRecord(locator.recordLocator)
  }

  fun close() {
    // Flush the last file first not to lose any data.
    lastFile.flush()

    for ( file <- files) {
      file.flush()
      file.close()
    }
  }
}
