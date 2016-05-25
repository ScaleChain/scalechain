//////////////////////////////////////////////////////////////////////////
// scalechain-proto-codec/build.sbt
//////////////////////////////////////////////////////////////////////////

libraryDependencies ++= Seq(
  "org.scodec" %% "scodec-core" % "1.9.0"
  // we hit this issue if we use 1.10.0 :
  //   java.lang.IncompatibleClassChangeError: Implementing class java.lang.IncompatibleClassChangeError: Implementing class
//  "org.scodec" %% "scodec-core" % "1.10.0"
)