//import Version._
libraryDependencies ++= {
  val akkaV       = "2.4.1"
  val akkaStreamV = "2.0.2"

  Seq(
    "com.typesafe" % "config" % "1.3.0",
    "com.typesafe.akka" %% "akka-actor"                           % akkaV,
    "com.typesafe.akka" %% "akka-stream-experimental"             % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-core-experimental"          % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-experimental"               % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaStreamV,
    "com.typesafe.akka" %% "akka-http-testkit-experimental"       % akkaStreamV,
    "com.google.guava" % "guava" % "19.0"
  )
}
