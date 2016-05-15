//import Version._
libraryDependencies ++= {
  val akkaV       = "2.4.2"

  Seq(
    "com.typesafe.akka" %% "akka-actor"                           % akkaV,
    "com.typesafe.akka" %% "akka-stream"                          % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental"               % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaV
//    "com.typesafe.akka" %% "akka-http-testkit-experimental"       % akkaV
  )
}
