//////////////////////////////////////////////////////////////////////////
// scalechain-cli/build.sbt
//////////////////////////////////////////////////////////////////////////

//import Version._
libraryDependencies ++= {
  Seq(
    // spark-core : SparkLoader.scala uses it.
    "org.apache.spark" %% "spark-core" % "1.5.2",
    "com.github.scopt"  %% "scopt" % "3.3.0"
  )
}

resolvers += Resolver.sonatypeRepo("public")