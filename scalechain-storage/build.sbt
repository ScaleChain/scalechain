//////////////////////////////////////////////////////////////////////////
// scalechain-storage/build.sbt
//////////////////////////////////////////////////////////////////////////

libraryDependencies ++= Seq(
  "org.rocksdb" % "rocksdbjni" % "4.5.1",
  "org.fusesource.leveldbjni" % "leveldbjni-osx" % "1.8",
//  "org.fusesource.leveldbjni" % "leveldbjni-linux64" % "1.8",
  "org.apache.cassandra" % "cassandra-all" % "3.5",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.0.2",
  "com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.2",
  "org.cassandraunit" % "cassandra-unit" % "3.0.0.1"
)

// We need to start/stop embedded cassandra within our unit tests.
// If we allow parallel execution, cassandra fails to start with an error saying "Unable to start two cassandra instances in a JVM"
parallelExecution in Test := false

