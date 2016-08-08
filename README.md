Introduction
============

[A five-minute pitch for ScaleChain private blockchain.](https://www.youtube.com/watch?v=Nh-XuL8mb6I)

A customizable blockchain implementation for private blockchains.
Includes Bitcoin script parser and executor. Fully customizable, fully compatible with Bitcoin scripts.
Take a look at an example [configuration](data/docs/config.md).

For the avoidance of doubt, this particular copy of the software is released under the version 3 of the GNU General Public License. It is brought to you by ScaleChain.

Copyright (c) 2015, ScaleChain and/or its affiliates. All rights reserved.

Why ScaleChain?
===============
1. Easy to use, easy to integrate, easy to customize.
2. Shard data across P2P nodes(Private blockchain only). 

Getting Started
===============
[A guide on starting a ScaleChain peer to peer network.](https://github.com/ScaleChain/scalechain/wiki/How-to-start-a-ScaleChain-peer)

A pitch deck
============
The following deck contains how financial companies can benefit from using ScaleChain.
 
[Cost Reduction for the Financial Industry using Blockchain Technology] (https://drive.google.com/file/d/0B5FqZAXWn_zgSThTdTVBYlhuUjA/view)

Supported Features
==================
- All Bitcoin scripts are supported and tested with actual bitcoin P2PK, P2PKH and P2SH transactions.
- Spark Integration; [load bitcoin transaction/block data onto Spark](data/docs/spark-integration.md) for real-time analysis.
- P2P networking; Bitcoin test-net integration.

Customization
=============
An example of adding a logarithm operation : 
```
// OP_LOG : Pop top two items, x and y to calculate logarithm of x with base y.
case class OpLog() extends Arithmetic {
　def opCode() = OpCode(0xb0)
　def execute(env : ScriptEnvironment) = binaryIntOperation( env, Math.log(_) / Math.log(_) )
}
```

Use Cases
=========
Private blockchains require customized script operations based on the Bitcoin script. ScaleChain provides customizable Bitcoin script executor, which is fully compatible with the recent version of Bitcoin script.

License
=======
ScaleChain Commercial License for OEMs, ISVs, and VARs
ScaleChain provides its ScaleChain Server and Client Libraries under a dual license model designed to meet the development and distribution needs of both commercial distributors (such as OEMs, ISVs, and VARs) and open source projects.

For OEMs, ISVs, VARs and Other Distributors of Commercial Applications:
OEMs (Original Equipment Manufacturers), ISVs (Independent Software Vendors), VARs (Value Added Resellers) and other distributors that combine and distribute commercially licensed software with ScaleChain software and do not wish to distribute the source code for the commercially licensed software under version 3 of the GNU General Public License (the "GPL") must enter into a commercial license agreement with ScaleChain.

For Open Source Projects and Other Developers of Open Source Applications:
For developers of Free Open Source Software ("FOSS") applications under the GPL that want to combine and distribute those FOSS applications with ScaleChain software, ScaleChain open source software licensed under the GPL is the best option.

For developers and distributors of open source software under a FOSS license other than the GPL, ScaleChain makes its GPL-licensed ScaleChain Client Libraries available under a FOSS Exception that enables use of the ScaleChain Client Libraries under certain conditions without causing the entire derivative work to be subject to the GPL.
