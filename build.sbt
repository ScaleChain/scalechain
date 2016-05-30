name := """scalechain"""

version in ThisBuild := "0.4"

scalaVersion in ThisBuild := "2.11.8"

libraryDependencies in ThisBuild ++= Seq(
    "io.netty" % "netty-all" % "4.0.36.Final",
    "io.spray" % "spray-json_2.11" % "1.3.2",
    "org.slf4j" % "slf4j-api" % "1.7.13",
    "org.scalatest" %% "scalatest" % "2.2.6",
    "org.scalacheck"%% "scalacheck" % "1.12.5",
// latest :    
//    "org.scalacheck"%% "scalacheck" % "1.13.1",
    // For unit tests, we need apache commons-io to remove a directory that has data files created while testing.
    "commons-io" % "commons-io" % "2.5")

fork in test := true
traceLevel in run := 0