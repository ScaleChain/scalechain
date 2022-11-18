package io.scalechain.blockchain.cli.command

import io.scalechain.blockchain.transaction.ChainEnvironment
import io.scalechain.util.Config
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.Options
import org.apache.commons.cli.Option
import kotlin.system.exitProcess
import io.scalechain.blockchain.cli.CommandArgumentConverter
import io.scalechain.blockchain.cli.ScaleChainPeer
import io.scalechain.util.GlobalEnvironemnt

data class RpcParameters(
    val host : String,
    val port : Int,
    val user : String,
    val password : String
)

data class Parameters(
    val rpcParameters: RpcParameters ,
    val network : String ,
    val command : String ,
    val args : Array<String> )

/**
 * Created by kangmo on 1/24/16.
 */
object CommandExecutor {
    val parser = DefaultParser()
    val options = Options()

    init {
        options.addOption(Option.builder("d")
            .longOpt("homeDirectory")
            .hasArg()
            .desc("The home directory of ScaleChain where scalechain.conf exists.")
            .build())

        options.addOption(Option.builder("h")
            .longOpt("host")
            .hasArg()
            .desc("host of the ScaleChain Json-RPC service")
            .build())

        options.addOption(Option.builder("p")
            .longOpt("port")
            .hasArg()
            .desc("port of the ScaleChain Json-RPC service")
            .build())

        options.addOption(Option.builder("u")
            .longOpt("user")
            .hasArg()
            .desc("The user name for RPC authentication")
            .build())

        options.addOption(Option.builder("w")
            .longOpt("password")
            .hasArg()
            .desc("The password for RPC authentication")
            .build())

        options.addOption(Option.builder("n")
            .longOpt("network")
            .hasArg()
            .desc("The network to use. currently 'testnet' is supported. Will support 'mainnet' as well as 'regtest' soon.")
            .build())

        Commands.all.forEach { command ->
            val desc = command.descriptor

            val optionBuilder = Option.builder()
                .longOpt(desc.command)
                .desc(desc.description)

            if (desc.argumentCount > 0)
                optionBuilder.numberOfArgs(desc.argumentCount)

            options.addOption(optionBuilder.build())
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        val line = parser.parse(options, args)

        // Set home directory first so that scalechain.conf can be loaded without any problem.
        GlobalEnvironemnt.ScaleChainHome = line.getOptionValue("homeDirectory", "./")

        val rpcParams = RpcParameters(
            host = line.getOptionValue("host", "localhost"),
            port = CommandArgumentConverter.toInt( "port", line.getOptionValue("port", null ) ) ?: Config.get().getInt("scalechain.api.port"),
            user = line.getOptionValue("user", Config.get().getString("scalechain.api.user")),
            password = line.getOptionValue("password", Config.get().getString("scalechain.api.password"))
        )

        val command = Commands.all.filter{ line.hasOption(it.descriptor.command) }.map{ it.descriptor.command }.firstOrNull()
        if (command == null) {
            println("You need to specify a command. Commands are : ${Commands.all.map{ it.descriptor.command }.joinToString(", ") }")
        } else {

            val params = Parameters(
                rpcParameters = rpcParams,
                network = line.getOptionValue("network", "testnet"),
                command = command,
                args = line.getOptionValues(command)
            )

            if (ChainEnvironment.create(params.network) == null) {
                println("Invalid p2p network : ${params.network}")
                exitProcess(-1)
            }

            val commandOption = Commands.commandMap.get(params.command)
            if (commandOption != null) { // If we have the command in the command map, execute it.
                commandOption.invoke(params.command, params.args, params.rpcParameters)
            } /*else {
                val response = RpcInvoker.invoke(params.command, params.args, params.rpcParameters.host, params.rpcParameters.port, params.rpcParameters.user, params.rpcParameters.password)
            }*/
        }
    }
}



