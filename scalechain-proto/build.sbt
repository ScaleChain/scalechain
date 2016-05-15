val akkaV       = "2.4.2"

libraryDependencies ++= Seq(
  "com.google.guava" % "guava" % "19.0",
  "com.typesafe.akka" %% "akka-http-experimental"               % akkaV,
  "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaV
)