//////////////////////////////////////////////////////////////////////////
// scalechain-net/build.sbt
//////////////////////////////////////////////////////////////////////////

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.2",
  "com.typesafe.akka" %% "akka-remote" % "2.4.2",
  "com.typesafe.akka" %% "akka-kernel" % "2.4.2",
//  "com.typesafe.akka" %% "akka-slf4j" % "2.4.2",
  "com.typesafe.akka" %% "akka-camel" % "2.4.2",
  "com.typesafe.akka" %% "akka-stream" % "2.4.2",
  "com.typesafe.akka" %% "akka-testkit" % "2.4.2" % "test",
  "com.typesafe.akka" %% "akka-stream-testkit" % "2.4.2" % "test",
  "com.typesafe" %% "ssl-config-akka" % "0.1.1"
)