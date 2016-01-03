import sbt._
import Keys._

object ScaleChainBuild extends Build {
	lazy val root =
		Project(
			id = "scalechain-all",
			base = file(".")).
			aggregate(base_module, codec_block, codec_proto, script, storage, net, main, util)

	lazy val base_module =
		Project(
			id = "scalechain-base",
			base = file("base"))

	lazy val codec_block =
		Project(
			id = "scalechain-codec-block",
			base = file("codec-block"))

	lazy val codec_proto =
		Project(
			id = "scalechain-codec-proto",
			base = file("codec-proto"))

	lazy val script =
		Project(
			id = "scalechain-script",
			base = file("script"))

	lazy val storage =
		Project(
			id = "scalechain-storage",
			base = file("storage"))

	lazy val net =
		Project(
			id = "scalechain-net",
			base = file("net"))

	lazy val main =
		Project(
			id = "scalechain-main",
			base = file("main"))

	lazy val util =
		Project(
			id = "scalechain-util",
			base = file("util"))

}

