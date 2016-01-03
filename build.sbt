name := """scalechain"""

version in ThisBuild := "0.1"

scalaVersion in ThisBuild := "2.10.6"

libraryDependencies in ThisBuild ++= Seq(
  "org.slf4j" % "slf4j-api" % "1.7.13",
  "org.slf4j" % "slf4j-simple" % "1.7.13",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test" )

