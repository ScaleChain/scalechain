//////////////////////////////////////////////////////////////////////////
// scalechain-chain/build.sbt
//////////////////////////////////////////////////////////////////////////

//import Version._
//libraryDependencies ..

// We are resetting singletons such as BlockProcessor for each test with BlockProcessor.create(chain).
// Because the singleton is valid for a specific transaction, we can't run tests in parallel.
parallelExecution in Test := false