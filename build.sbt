name := """scalechain"""

version in ThisBuild := "0.7"

scalaVersion in ThisBuild := "2.11.8"

libraryDependencies in ThisBuild ++= Seq(
    "io.netty" % "netty-all" % "4.1.1.Final",
    "io.spray" % "spray-json_2.11" % "1.3.2",
    "ch.qos.logback" %  "logback-classic" % "1.1.7",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.4.0",
    "org.scalatest" %% "scalatest" % "2.2.6",
    "org.scalacheck"%% "scalacheck" % "1.12.5",
// latest :    
//    "org.scalacheck"%% "scalacheck" % "1.13.1",
    // For unit tests, we need apache commons-io to remove a directory that has data files created while testing.
    "commons-io" % "commons-io" % "2.5",
    "org.eclipse.collections" % "eclipse-collections" % "7.1.0",
    "org.eclipse.collections" % "eclipse-collections-api" % "7.1.0" )

fork in test := true
traceLevel in run := 0
logLevel in Test := Level.Warn
