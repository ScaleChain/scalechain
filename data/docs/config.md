# ScaleChain configuration
ScaleChain supports setting up a fully customizable blockchain infrastructure.

Like assembling lego blocks, you can compose your blockchain by choosing plugins you want to use.
For example, you may want to compose a private blockchain with Ethereum script support.
In this case, you can use 'private-bitcoin' blockchain plugin and 'ethereum' script plugin.

ScaleChain uses HOCON(Human-Optimized Config Object Notation) for configuration files.

https://github.com/typesafehub/config/blob/master/HOCON.md

# Configuration files
ScaleChain has two configuration files. They are plugins.conf and configurations.conf.

## plugins.conf
plugins.conf lists all plugins for each plugin types.
For example, 'storage' plugin category defines two plugins, 'shared' and 'sharded'. The 'shared' storage
 keeps all transactions from day one on each P2P node. The 'sharded' storage plugin distributes transactions
 across P2P nodes based on the transaction hash.

Each plugin requires a field, 'class', which has the name of the Scala(or Java) class that
implements the plugin. In case you need a custom plugin instead of one provided by ScaleChain, you can
extend a plugin base class for the specific category. For example, you can implement your own wallet specification by extending io.scalechain.blockchain.wallet.WalletPlugin class and overriding methods in it.

The 'scalechain.' prefix on a class name denotes that the classe is provided by ScaleChain.
The actual package where the class exists may be different from class to class.
For example, the actual package name of scalechain.BitcoinWallet is io.scalechain.blockchain.wallet.
The 'scalechain.' prefix helps us to write a simple configuration file with a shorter package name.
```
scalechain {
    plugins {
        rpc-protocol {
            bitcoin {
                class = "scalechain.BitcoinRPC"
            }
        }
        p2p-protocol {
            bitcoin {
                class = "scalechain.BitcoinP2P"
            }
        }
        wallet {
            bitcoin {
                class = "scalechain.BitcoinWallet"
            }
        }
        blockchain {
            public-bitcoin {
                class = "scalechain.BitcoinBlockchain"
            }
            private-bitcoin {
                class = "scalechain.PrivateBitcoinBlockchain"
            }
        }
        serialization {
            bitcoin {
                class = "scalechain.BitcoinSerialization"
            }
        }
        script {
            bitcoin {
                class = "scalechain.BitcoinScript"
            }
            bitcoin-turing-complete {
                class = "scalechain.BitcoinTuringCompleteScript"
            }
            ethereum {
                class = "scalechain.EthereumScript"
            }
        }
        storage {
            shared {
                class = "scalechain.SharedStorage"
            }
            sharded {
                class = "scalechain.ShardedStorage"
            }
        }
    }
}
```

## configurations.conf

configurations.conf lists all possible configurations. Each configuration has a specific plugin for each plugin category. For example, 'private-bitcoin' configuration defines 'storage' plugin category to use 'sharded' storage
to distribute data across p2p nodes in the private network, whereas 'public-bitcoin-testnet' configuration defines
'storage' plugin category to use 'shared' so that all p2p nodes share the same set of data.

```
scalechain {
    configurations {
        private-bitcoin {
            rpc-protocol = "bitcoin"
            p2p-protocol = "bitcoin"
            wallet {
                plugin="bitcoin"
                params {
                    address-version = 0x00
                }
            }
            blockchain = "private-bitcoin"
            protocol = "bitcoin"
            serialization = "bitcoin"
            script = "bitcoin-turing-complete"
            storage = "sharded"
            params {
                rpc-port = 8080
                protocol-port = 8081
            }
        }
        public-bitcoin-testnet {
            rpc-protocol = "bitcoin"
            p2p-protocol = "bitcoin"
            wallet {
                plugin="bitcoin"
                params {
                    address-version = 0x6f
                }
            }
            blockchain = "public-bitcoin"
            protocol = "bitcoin"
            serialization {
                plugin = "bitcoin"
                params {
                    magic = 0x0B110907
                }
            }
            script = "bitcoin"
            storage = "shared"
            params {
                rpc-port = 18332
                protocol-port = 18333
            }
        }
    }
}
```

# Node startup
To start a ScaleChain node, you can run the ScaleChain main class by specifying a configuration name defined in configurations.conf.
For example, the following command starts a ScaleChain node with 'private-bitcoin' configuration.

```
java io.scalechain.blockchain.ScaleChain private-bitcoin
```
