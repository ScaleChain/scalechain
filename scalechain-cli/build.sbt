//import Version._
libraryDependencies ++= {
  val akkaV       = "2.4.2"

  Seq(
    "com.github.scopt"  %% "scopt" % "3.3.0",
    "com.typesafe.akka" %% "akka-actor"                           % akkaV,
    "com.typesafe.akka" %% "akka-stream"                          % akkaV,
    //"com.typesafe.akka" %% "akka-http-core-experimental"          % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental"               % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaV,
    "com.typesafe.akka" %% "akka-testkit"                         % akkaV % "test"
  )
}

resolvers += Resolver.sonatypeRepo("public")