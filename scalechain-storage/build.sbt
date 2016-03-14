libraryDependencies ++= Seq(
  "org.rocksdb" % "rocksdbjni" % "4.1.0",
  // For unit tests, we need apache commons-io to remove a directory that has data files created while testing.
  "org.apache.commons" % "commons-io" % "1.3.2"
)