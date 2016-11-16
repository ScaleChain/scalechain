name := """scalechain"""

version in ThisBuild := "0.7"

scalaVersion in ThisBuild := "2.12.0"

kotlinLib("stdlib")

libraryDependencies in ThisBuild ++= Seq(
    "junit" % "junit" % "4.12" % "test" ,
    "org.jetbrains.kotlin" % "kotlin-stdlib" % "1.0.4",
    "org.jetbrains.kotlin" % "kotlin-test" % "1.0.4" % "test",
    "io.kotlintest" % "kotlintest" % "1.3.5" % "test",
    "io.netty" % "netty-all" % "4.1.6.Final",
    "io.spray" % "spray-json_2.12" % "1.3.2",
    "ch.qos.logback" %  "logback-classic" % "1.1.7",
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "org.scalatest" %% "scalatest" % "3.0.0",
//    "org.scalatest" %% "scalatest" % "2.2.6",
// latest :    
//    "org.scalacheck"%% "scalacheck" % "1.13.1",
    // For unit tests, we need apache commons-io to remove a directory that has data files created while testing.
    "commons-io" % "commons-io" % "2.5",
    "org.eclipse.collections" % "eclipse-collections" % "8.0.0",
    "org.eclipse.collections" % "eclipse-collections-api" % "8.0.0" )

fork in test := true
traceLevel in run := 0
logLevel in Test := Level.Warn
