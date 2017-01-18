//////////////////////////////////////////////////////////////////////////
// scalechain-oap/build.sbt
//////////////////////////////////////////////////////////////////////////

libraryDependencies ++= Seq(
  "com.google.code.gson" % "gson" % "2.8.0",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.11" % "test->default"
)

crossPaths := false
parallelExecution in Test := false
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))
//fork in Test := true

// Test resources
resourceDirectory in Test := baseDirectory.value / "src/test/resources"

