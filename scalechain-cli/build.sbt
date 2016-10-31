//////////////////////////////////////////////////////////////////////////
// scalechain-cli/build.sbt
//////////////////////////////////////////////////////////////////////////

//import Version._
libraryDependencies ++= {
  Seq(
    // spark-core : SparkLoader.scala uses it.
    // "org.apache.spark" %% "spark-core" % "1.6.1",
    "com.github.scopt"  %% "scopt" % "3.5.0"
  )
}

resolvers += Resolver.sonatypeRepo("public")