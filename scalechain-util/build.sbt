//////////////////////////////////////////////////////////////////////////
// scalechain-util/build.sbt
//////////////////////////////////////////////////////////////////////////

//import Version._
libraryDependencies ++= {
  Seq(
    "com.typesafe" % "config" % "1.3.1",
    // for LRUMap in Peer.
    //"org.apache.commons" % "commons-collections4" % "4.1",
    "com.google.guava" % "guava" % "20.0"
  )
}
