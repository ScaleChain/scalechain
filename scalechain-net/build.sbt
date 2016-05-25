//////////////////////////////////////////////////////////////////////////
// scalechain-net/build.sbt
//////////////////////////////////////////////////////////////////////////
val akkaV       = "2.4.6"
libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor"                           % akkaV,
    "com.typesafe.akka" %% "akka-stream"                          % akkaV,
    //"com.typesafe.akka" %% "akka-http-core-experimental"          % akkaV,
    "com.typesafe.akka" %% "akka-http-experimental"               % akkaV,
    "com.typesafe.akka" %% "akka-http-spray-json-experimental"    % akkaV,
    "com.typesafe.akka" %% "akka-testkit"                         % akkaV % "test"
)  