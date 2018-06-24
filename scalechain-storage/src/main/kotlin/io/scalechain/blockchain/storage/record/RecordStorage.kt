package io.scalechain.blockchain.storage.record

import java.io.File

import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.FileRecordLocator
import io.scalechain.blockchain.BlockStorageException
import io.scalechain.blockchain.ErrorCode

import org.slf4j.LoggerFactory

/** Maintains a list of record files.
  *
  * Why?
  *   A record file has a maximum size. If a file reaches the maximum size, we need to add a new record file.
  *   Record storage keeps track of multiple record files, enables us to search a record by a record locator,
  *   which hash the file index of the multiple record files.
  */
open class RecordStorage(private val directoryPath : File, private val filePrefix : String, private val maxFileSize : Long) {
  private val logger = LoggerFactory.getLogger(RecordStorage::class.java)

  val files = arrayListOf<RecordFile>()

  init {
    if (directoryPath.exists()) {
      // For each file in the path
      val fileList = directoryPath.listFiles()
      if (fileList.isEmpty()) {
        // Do nothing. no file exists
      } else {
        fileList.sortBy{ it.getName() }
        for ( file in fileList ) {
          val decodedFileName = BlockFileName.from(file.getName())
          if (decodedFileName == null) {
            // Ignore files that are not matching the block file name format.
          } else {
            if (decodedFileName.prefix == filePrefix) {
              if (files.size == decodedFileName.fileNumber) {
                files.add( newFile(file) )
              } else {
                val fileNames = fileList.toList().joinToString(",")

                logger.error("Invalid Block File Number. Expected : ${files.size}, Actual : ${decodedFileName.fileNumber}, File Names: $fileNames")
                throw BlockStorageException(ErrorCode.InvalidFileNumber)
              }
            }
          }
        }
      }
    } else {
      throw BlockStorageException(ErrorCode.BlockFilePathNotExists)
    }

    /** If there is no file at all, add a file.
     */
    if (files.isEmpty())
      files.add( newFile() )
  }

  fun lastFile() = files[lastFileIndex()]

  /** Because we are appending records, flushing the last file is enough.
    * We also flush the last file when a file is added, so flushing the current last file is enough.
    */
  fun flush() = lastFile().flush()

  fun lastFileIndex() = files.size-1

  fun newFile(blockFile : File) : RecordFile = RecordFile(blockFile, maxFileSize )

  fun newFile() : RecordFile {
    val fileNumber = lastFileIndex() + 1
    val blockFile = File( directoryPath.getAbsolutePath() + File.separatorChar + BlockFileName(filePrefix, fileNumber))
    return newFile(blockFile)
  }

  /** Add a file. Also flush the last file, so that we do not lose any file contents when the system crashes.
    */
  fun addNewFile() {
    lastFile().flush()
    files.add( newFile() )
  }

  // TODO : Make FileRecordLocator to have a type parameter, T so that we can have a compile error when an incorrect codec is used for a record locator.

  fun<T> appendRecord(codec : Codec<T>, record : T) : FileRecordLocator {
    try {
      val recordLocator = lastFile().appendRecord(codec, record)
      return FileRecordLocator( lastFileIndex(), recordLocator )
    } catch( e : BlockStorageException ) {
      if (e.code == ErrorCode.OutOfFileSpace) {
        addNewFile()
        val recordLocator = lastFile().appendRecord(codec, record)
        return FileRecordLocator( lastFileIndex(), recordLocator )
      } else {
        throw e
      }
    }
  }

  fun<T> readRecord(codec : Codec<T>, locator : FileRecordLocator) : T {
    if (locator.fileIndex < 0 || locator.fileIndex >= files.size) {
      throw BlockStorageException(ErrorCode.InvalidFileNumber)
    }

    val file = files[locator.fileIndex]

    return file.readRecord(codec, locator.recordLocator)
  }

  fun close() {
    // Flush the last file first not to lose any data.
    lastFile().flush()

    for ( file in files) {
      file.flush()
      file.close()
    }
  }
}
