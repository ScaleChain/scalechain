# Introduction
ScaleChain project has sub-modules with layered architecture. 
Each module exists at a layer and a module can use another module at a layer below it.

# Layers
The followings are list of sub-modules from the top layer to the bottom layer.
```
+-------------------------+----------------------+
|          Cli            |          Main        |
+-------------------------+----------------------+
|                   Net                          |
+------------------------------------------------+
|                   Storage                      |
+------------------------------------------------+
|                   Script                       |
+----------------+----------------+--------------+
|   Codec|Block  |   Codec|Proto  |              |
+---------------------------------+    Crypto    |
|      Block     |      Proto     |              |
+----------------+----------------+--------------+
|                   Util Layer                   |
+------------------------------------------------+
```
## Cli
A jar package containing all CLI(command line interface) utilities such as DumpChain.

## Main
A jar package containing the ScaleChain main class to run P2P nodes.

## Net
The P2P networking layer.

## Storage
The storage layer. Knows how to store blocks and transactions. It knows how to search blocks by hash or transactions by hash.

## Script
The script layer. Implements all bitcoin script commands.

## Codec-Proto
The codec layer of protocols. Knows how to read/write case classes for P2P networking to/from disk or network.

## Codec-Block
The codec layer of blocks. Knows how to read/write transaction/block data to/from disk or network.

## Proto
Defines all case classes for communicating with another P2P node.

## Block
Defines all case classes about blocks and transactions.

## Util
The utility layer. Implements utility functions that can be used by other modules.
