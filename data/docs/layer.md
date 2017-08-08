# Introduction
ScaleChain project has sub-modules with layered architecture. 
Each module exists at a layer and a module can use another module at a layer below it.

# Layers
The followings are list of sub-modules from the top layer to the bottom layer.
```
+------------------------------------------------+
|                   Cli                          |
+------------------------------------------------+
|                   API                          |
+------------------------------------------------+
|                   Net                          |
+------------------------------------------------+
|                   Wallet                       |
+------------------------------------------------+
|                   Chain                        |
+------------------------------------------------+
|                   Transaction                  |
+------------------------------------------------+
|                   Storage                      |
+------------------------------------------------+
|                   Script                       |
+------------------------------------------------+
|                   Proto|Codec                  |
+------------------------------------------------+
|                   Proto                        |
+------------------------------------------------+
|                   Crypto                       |
+------------------------------------------------+
|      Util Layer        |       Consensus       |
+------------------------------------------------+
```
## Cli
A jar package containing all CLI(command line interface) utilities such as DumpChain.

## API
The Json-RPC API layer. 100% Compatible with Bitcoin.

## Net
The P2P networking layer. 100% Compatible with Bitcoin. 
To make the protocol compatible, we use Akka Streams and implement codecs on Codec|Proto layer to convert on-the-wire format to case classes and vice versa.

## Wallet
The wallet layer. Contains all features related to a wallet, creating transactions, signing transactions,
creating accounts, creating addresses an account etc.

## Chain
The chain layer is responsible for maintaining the best block chain. 
It knows how to update the best block, move transactions from/to blocks and the mempool upon block reorganization.

## Transaction
The ScaleChain transaction layer. Implements signing a raw transaction, verifying a signed transaction.

## Storage
The storage layer. Knows how to store blocks and transactions. It knows how to search blocks by hash or transactions by hash.

## Script
The script layer. Implements all bitcoin script commands.

## Proto-Codec
The codec layer of protocols. Knows how to read/write case classes for P2P networking to/from disk or network.
Also, has the codecs of blocks. Knows how to read/write transaction/block data to/from disk or network.

## Proto
Defines all case classes for communicating with another P2P node.

## Block
Defines all case classes about blocks and transactions.

## Crypto 
The crypto layer. Implements ECC for signing and verifying transactions.
Also implements various hash functions.

## Consensus
The consensus layer. Implements consensus alogrithms that satisfies Byzantine Fault Tolerance.

## Util
The utility layer. Implements utility functions that can be used by other modules.

# Communication Channels
## RPC(Remote Procedure Call)s
RPCs are compatible with Bitcoin. ScaleChain supports a subset of RPCs supported by Bitcoin.

## Peer-to-Peer Communication
Serialized format of each message for Peer-to-Peer communication is compatible with Bitcoin messages. 

# The whole picture
The ScaleChain open source project repository keeps the source code of the community edition of ScaleChain. 
The followings are not open sourced.

1. ScaleChain enterprise edition, which has advanced features such as node recovery.
2. ScaleChain REST API, which provides asset management concept on top of both ScaleChain and Bitcoin. 
3. Scalechain Management Console, which provides a web-based administration tool for monitoring ScaleChain daemons and managing addressess, accounts, transactions, and assets.

## ScaleChain Daemon
ScaleChain daemon runs in each node of a private(or consortium) block chain. It provides RPCs as interfaces for communicating with outside of the blockchain network.
It also provides peer-to-peer communication channels for the 'intranet' of the blockchain network. Each node in the network communicates with each other using the peer-to-peer communication channel.
Each daemon in each node manages its own wallet. Each node as different set of addresses and accounts, which are groups of addresses. 
Each wallet in each node keeps additional information on transactions related to the addresses in the wallet.
ScaleChain transaction model is same to Bitcoin. A transaction spends outputs of previous transactions. These spent outputs are specified by inputs of a transaction. Each output has a locking script, which should be unlocked by inputs that are pointing to the output.
ScaleChain daemon provides addresses, which owns coin, which can be transferred by the address owner by providing corresponding digital signature on the transaction that creates new outputs spending inputs. 

ScaleChain daemon does not provide the asset concept. It provides coin, which can be transferred from an address to another.

## ScaleChain REST API
ScaleChain REST API provides interfaces for business logic. The business logic written using the ScaleChain REST API can choose one of two back-ends.

1. ScaleChain - For integrating with a private(or consortium) blockchain in corporations or clouds.
2. Bitcoin - For integrating with Bitcoin network.

Also, ScaleChain REST API provides asset concept on top of the virtual currencies supported by Bitcoin or ScaleChain. 
ScaleChain REST API implements Open Assets Protocol, which uses the 80 byte additional space for each transaction by leveraging OP_RETURN. 
It puts hash of the asset definition file in the metadata part of the 80 additional bytes in a transaction.

## Business Logic
ScaleChain supports various kinds of uses cases on top of the ScaleChain REST API.
The followings are ones supported, and provided as sample codes.

The business logic built on top of the ScaleChain REST API can choose either Bithcoin network or ScaleChain network as the back-end of the REST API.
No need to modify the business logic to choose a different blockchain network.

## ScaleChain Management Console
ScaleChain Management Console provides a web-based administration tool for monitoring ScaleChain daemons and managing addressess, accounts, transactions, and assets.
Take a look at the ScaleChain Management Console by signing up on scalechain.io. Currently we are approving employees or founders of companies in the financial sector or IoT sector.

# Consensus
## Problems with PoW
### Need block reorganization
With PoW, block reorganization is inevitable, because more than a node can mine a block at the same time, but only one of them should create the latest block on top of the current best blockchain.
Bitcoin uses chain work to decide which block to attach on top of the best blockchain when there are more than a candidate.
The block reorganization happens not only on the latest block, but can also happen on a fork that has greater chain work than the best blockchain.
This results in many transactions in the best blockchain to be invalidated, moved to the transaction pool, and wait for the new block to have them.

The block reorganization made the delay time for a transaction to put into a block uncertain. Financial applications require a transaction to be put into a durable storage in a predefined time window, but the block reorganization is making it hard to meet this requirement.

### An intruder with greater computing power can rewrite the blockchain
An intruder can use his(or hers) own computing power to rewrite the blockchain. It is because the blockchain decides to choose a chain that has greater chain work, which can be achieved by having greater computing power.

## Byzantine Fault Tolerance
We chose BFT smart to support BFT(Byzantine fault tolerance). Initially we supported PoW(Proof of Work) like Bitcoin, but it had the following problems.

### The white paper
http://www.di.fc.ul.pt/~bessani/publications/dsn14-bftsmart.pdf

