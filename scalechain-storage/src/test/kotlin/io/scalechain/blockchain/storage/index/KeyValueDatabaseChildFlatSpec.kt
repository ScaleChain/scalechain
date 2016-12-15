package io.scalechain.blockchain.storage.index

import io.kotlintest.matchers.Matchers
import io.kotlintest.specs.FlatSpec
import io.scalechain.blockchain.storage.Storage
import io.scalechain.test.BeforeAfterEach
import io.scalechain.test.ChildFlatSpec
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * Created by kangmo on 15/12/2016.
 */

interface KeyValueDatabaseHolder {
  var db : KeyValueDatabase
}