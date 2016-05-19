libraryDependencies ++= Seq()

// We need to run the WalletStoreSpec in serial
parallelExecution in Test := false

