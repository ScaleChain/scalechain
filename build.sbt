name := """scalechain"""

version in ThisBuild := "0.3"

scalaVersion in ThisBuild := "2.11.7"

libraryDependencies in ThisBuild ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.13",
//  "org.slf4j" % "slf4j-log4j12" % "1.7.21",
//  "log4j" % "log4j" % "1.2.17",
  "org.scalatest" %% "scalatest" % "2.2.6",
  "org.scalacheck"%% "scalacheck" % "1.12.5")

fork in test := true
traceLevel in run := 0