//////////////////////////////////////////////////////////////////////////
// scalechain-storage/build.sbt
//////////////////////////////////////////////////////////////////////////

libraryDependencies ++= Seq(
  "org.rocksdb" % "rocksdbjni" % "4.11.2",
  "org.fusesource.leveldbjni" % "leveldbjni-osx" % "1.8",
//  "org.fusesource.leveldbjni" % "leveldbjni-linux64" % "1.8",
  "org.fusesource" % "sigar" % "1.6.4"

//  "org.apache.cassandra" % "cassandra-all" % "3.9",
//  "com.datastax.cassandra" % "cassandra-driver-core" % "3.1.2",
//  "com.datastax.cassandra" % "cassandra-driver-extras" % "3.1.2",
//  "org.cassandraunit" % "cassandra-unit" % "3.0.0.1"
)

// We need to start/stop embedded cassandra within our unit tests.
// If we allow parallel execution, cassandra fails to start with an error saying "Unable to start two cassandra instances in a JVM"
parallelExecution in Test := false

