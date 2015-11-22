name := """scalachain"""

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.13",
  "org.slf4j" % "slf4j-simple" % "1.7.13",
  "org.apache.spark" %% "spark-core" % "1.5.2",
  "com.madgag.spongycastle" % "core" % "1.53.0.0",
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test")
