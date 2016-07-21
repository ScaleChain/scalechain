//////////////////////////////////////////////////////////////////////////
// scalechain-proto-codec/build.sbt
//////////////////////////////////////////////////////////////////////////

import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

PB.protobufSettings

//PB.singleLineToString in PB.protobufConfig := true

PB.runProtoc in PB.protobufConfig := (args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray))

PB.flatPackage in PB.protobufConfig := true

//PB.javaConversions in PB.protobufConfig := true

libraryDependencies ++= Seq(
  "org.scodec" %% "scodec-core" % "1.10.2",
  "com.trueaccord.scalapb" %% "scalapb-runtime" % "0.4.20" % PB.protobufConfig
)