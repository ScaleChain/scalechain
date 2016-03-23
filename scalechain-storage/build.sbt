libraryDependencies ++= Seq(
  "org.rocksdb" % "rocksdbjni" % "4.1.0",
  "org.apache.cassandra" % "cassandra-all" % "3.4",
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.0.0",
  "com.datastax.cassandra" % "cassandra-driver-extras" % "3.0.0",
//  "com.websudos" %% "phantom" % "1.22.0",
//  "com.websudos" %% "phantom-connectors" % "1.22.0",
//  "com.websudos" %% "phantom-dsl" % "1.22.0",
  "org.cassandraunit" % "cassandra-unit" % "3.0.0.1",
  // For unit tests, we need apache commons-io to remove a directory that has data files created while testing.
  "org.apache.commons" % "commons-io" % "1.3.2"
)

// We need to start/stop embedded cassandra within our unit tests.
// If we allow parallel execution, cassandra fails to start with an error saying "Unable to start two cassandra instances in a JVM"
parallelExecution in Test := false