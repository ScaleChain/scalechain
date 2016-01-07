import sbt._
import Keys._

object ScaleChainBuild extends Build {
	lazy val root =
		Project(
			id = "scalechain-all",
			base = file(".")).
			aggregate(util, crypto, block, proto, codec_block, codec_proto, script, transaction, storage, net, main, cli)

	lazy val util =
		Project(
			id = "scalechain-util",
			base = file("scalechain-util"))

	lazy val crypto =
		Project(
			id = "scalechain-crypto",
			base = file("scalechain-crypto"))
		.dependsOn(util)

	lazy val block =
		Project(
			id = "scalechain-block",
			base = file("scalechain-block"))
		.dependsOn(util)

	lazy val proto =
		Project(
			id = "scalechain-proto",
			base = file("scalechain-proto"))
		.dependsOn(util)

	lazy val codec_block =
		Project(
			id = "scalechain-codec-block",
			base = file("scalechain-codec-block"))
		.dependsOn(util, block)

	lazy val codec_proto =
		Project(
			id = "scalechain-codec-proto",
			base = file("scalechain-codec-proto"))
		.dependsOn(util, proto)

	lazy val script =
		Project(
			id = "scalechain-script",
			base = file("scalechain-script"))
		.dependsOn(util, crypto, codec_block)

  lazy val transaction =
		Project(
			id = "scalechain-transaction",
			base = file("scalechain-transaction"))
		.dependsOn(util, script, storage)

	lazy val storage =
		Project(
			id = "scalechain-storage",
			base = file("scalechain-storage"))
		.dependsOn(util, block)

	lazy val net =
		Project(
			id = "scalechain-net",
			base = file("scalechain-net"))
		.dependsOn(util)

	lazy val main =
		Project(
			id = "scalechain-main",
			base = file("scalechain-main"))
		.dependsOn(util)

	lazy val cli =
		Project(
			id = "scalechain-cli",
			base = file("scalechain-cli"))
	  .dependsOn(util, transaction, net)
}

