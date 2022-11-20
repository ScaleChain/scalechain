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
 *
 * The followings are bitcoin-cli command line arguments. Make arguments compatible to bitcoin-cli
 *
 */
object CommandExecutor {
    val parser = DefaultParser()
    val options = Options()

    init {
        options.addOption(Option.builder("version")
            .longOpt("version")
            .hasArg()
            .desc("Print scalechian-cli command line tool version.")
            .build())

        options.addOption(Option.builder("homedir")
            .longOpt("homedir")
            .hasArg()
            .desc("Specify data directory where data files such as block files are stored")
            .build())

        options.addOption(Option.builder("rpcconnect")
            .longOpt("rpcconnect")
            .hasArg()
            .desc("The host address of the node that runs ScaleChain Json-RPC service")
            .build())

        // TODO : Need to have constants for ports instead of hard-coding them
        options.addOption(Option.builder("rpcport")
            .longOpt("rpcport")
            .hasArg()
            .desc("The port number of the ScaleChain Json-RPC service. Default mainnet port: 8080, testnet port: 18080")
            .build())

        options.addOption(Option.builder("rpcuser")
            .longOpt("rpcuser")
            .hasArg()
            .desc("The user name for Json-RPC authentication")
            .build())

        options.addOption(Option.builder("rpcpassword")
            .longOpt("rpcpassword")
            .hasArg()
            .desc("The user password for Json-RPC authentication")
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
        print("program arguments: ")
        for (a in args) {
            print(" $a")
        }
        println()

        val line = parser.parse(options, args)

        // Set home directory first so that scalechain.conf can be loaded without any problem.
        GlobalEnvironemnt.ScaleChainHome = line.getOptionValue("homedir", "./")

        val rpcParams = RpcParameters(
            host = line.getOptionValue("rpcconnect", "localhost"),
            port = CommandArgumentConverter.toInt( "rpcport", line.getOptionValue("port", null ) ) ?: Config.get().getInt("scalechain.api.port"),
            user = line.getOptionValue("rpcuser", Config.get().getString("scalechain.api.user")),
            password = line.getOptionValue("rpcpassword", Config.get().getString("scalechain.api.password"))
        )

        val command = Commands.all.filter{ line.hasOption(it.descriptor.command) }.map{ it.descriptor.command }.firstOrNull()
        if (command == null) {
            println("You need to specify a command. Commands are : ${Commands.all.map{ it.descriptor.command }.joinToString(", ") }")
        } else {

            val params = Parameters(
                rpcParameters = rpcParams,
                network = line.getOptionValue("network", "testnet"),
                command = command,
                args = line.getOptionValues(command) ?: arrayOf<String>()
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



