# Introduction
ScaleChain project has sub-modules with layered architecture. 
Each module exists at a layer and a module can use another module at a layer below it.

# Layers
The followings are list of sub-modules from the top layer to the bottom layer.
```
+-------------------------+----------------------+
|          Cli            |          Main        |
+-------------------------+----------------------+
|                   API                          |
+------------------------------------------------+
|                   Wallet                       |
+------------------------------------------------+
|                   Net                          |
+------------------------------------------------+
|                   Transaction                  |
+------------------------------------------------+
|                   Chain                        |
+------------------------------------------------+
|                   Storage                      |
+------------------------------------------------+
|                   Script                       |
+----------------+----------------+--------------+
|           Proto|Codec           |              |
+---------------------------------+    Crypto    |
|              Proto              |              |
+----------------+----------------+--------------+
|                   Util Layer                   |
+------------------------------------------------+
```
## Cli
A jar package containing all CLI(command line interface) utilities such as DumpChain.


## API
The Json-RPC API layer. 100% Compatible with Bitcoin.

## Wallet
The wallet layer. Contains all features related to a wallet, creating transactions, signing transactions,
creating accounts, creating addresses an account etc.

## Net
The P2P networking layer. 100% Compatible with Bitcoin. 
To make the protocol compatible, we use Akka Streams and implement codecs on Codec|Proto layer to convert on-the-wire format to case classes and vice versa.

## Transaction
The ScaleChain transaction layer. Implements signing a raw transaction, verifying a signed transaction.
Also implements transaction memory pool.

## Chain
The chain layer is responsible for maintaining the best block chain. 
It knows how to update the best block, move transactions from/to blocks and the mempool upon block reorganization.

## Storage
The storage layer. Knows how to store blocks and transactions. It knows how to search blocks by hash or transactions by hash.
## Script
The script layer. Implements all bitcoin script commands.

## Codec-Proto
The codec layer of protocols. Knows how to read/write case classes for P2P networking to/from disk or network.

## Codec-Block
The codec layer of blocks. Knows how to read/write transaction/block data to/from disk or network.

## Crypto 
The crypto layer. Implements ECC for signing and verifying transactions.
Also implements various hash functions.

## Proto
Defines all case classes for communicating with another P2P node.

## Block
Defines all case classes about blocks and transactions.

## Util
The utility layer. Implements utility functions that can be used by other modules.
