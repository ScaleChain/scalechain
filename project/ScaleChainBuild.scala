import sbt._
import Keys._

object ScaleChainBuild extends Build {
	lazy val root =
		Project(
			id = "scalechain-all",
			base = file(".")).
			aggregate(util, crypto, proto, proto_codec, script, transaction, storage, net, api_domain, api, wallet, cli)

	lazy val util =
		Project(
			id = "scalechain-util",
			base = file("scalechain-util"))

	lazy val crypto =
		Project(
			id = "scalechain-crypto",
			base = file("scalechain-crypto"))
		.dependsOn(util)

	lazy val proto =
		Project(
			id = "scalechain-proto",
			base = file("scalechain-proto"))
		.dependsOn(util)

	lazy val proto_codec =
		Project(
			id = "scalechain-proto-codec",
			base = file("scalechain-proto-codec"))
		.dependsOn(util, proto, crypto)

	lazy val script =
		Project(
			id = "scalechain-script",
			base = file("scalechain-script"))
		.dependsOn(util, crypto, proto_codec)

  lazy val transaction =
		Project(
			id = "scalechain-transaction",
			base = file("scalechain-transaction"))
		.dependsOn(util, script, storage)

	lazy val storage =
		Project(
			id = "scalechain-storage",
			base = file("scalechain-storage"))
		.dependsOn(util, proto, proto_codec, script)

	lazy val net =
		Project(
			id = "scalechain-net",
			base = file("scalechain-net"))
		.dependsOn(util, proto, proto_codec, script, storage)

	lazy val api_domain =
		Project(
			id = "scalechain-api-domain",
			base = file("scalechain-api-domain"))
				.dependsOn(util)

	lazy val api =
		Project(
			id = "scalechain-api",
			base = file("scalechain-api"))
				.dependsOn(util, net, api_domain, transaction, wallet)

	lazy val wallet =
		Project(
			id = "scalechain-wallet",
			base = file("scalechain-wallet"))
		.dependsOn(util, storage, transaction)

	lazy val cli =
		Project(
			id = "scalechain-cli",
			base = file("scalechain-cli"))
	  .dependsOn(util, api, net, transaction, api_domain)
}

