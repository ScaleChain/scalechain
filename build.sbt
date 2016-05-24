name := """scalechain"""

version in ThisBuild := "0.3"

scalaVersion in ThisBuild := "2.11.7"

val akkaV       = "2.4.2"
libraryDependencies in ThisBuild ++= Seq(
    // spark-core : SparkLoader.scala uses it.
    "org.apache.spark" %% "spark-core" % "1.5.2",
    "com.github.scopt"  %% "scopt" % "3.3.0",
    "com.typesafe.akka" %% "akka-actor"                           % akkaV,
    "com.typesafe.akka" %% "akka-stream"                          % akkaV,
    //"com.typesafe.akka" %% "akka-http-core-experimental"          % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental"               % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaV,
    "com.typesafe.akka" %% "akka-testkit"                         % akkaV % "test",  
    "org.slf4j" % "slf4j-api" % "1.7.13",
//  "org.slf4j" % "slf4j-log4j12" % "1.7.21",
//  "log4j" % "log4j" % "1.2.17",
    "org.scalatest" %% "scalatest" % "2.2.6",
    "org.scalacheck"%% "scalacheck" % "1.12.5")

fork in test := true
traceLevel in run := 0