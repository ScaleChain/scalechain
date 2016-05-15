name := """scalechain"""

version in ThisBuild := "0.3"

scalaVersion in ThisBuild := "2.11.7"

libraryDependencies in ThisBuild ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.13",
  "org.slf4j" % "slf4j-simple" % "1.7.13",
  "org.scalatest" %% "scalatest" % "2.2.6" % "test",
  "org.scalacheck"%% "scalacheck" % "1.12.5" % "test")

fork in test := true
