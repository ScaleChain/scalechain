Introduction
============

Introducing ScaleChain, the groundbreaking altcoin project designed to revolutionize payment networks between robots and humans. In an era marked by the proliferation of automation and robotics, ScaleChain emerges as the solution to facilitate effortless transactions between intelligent machines and their human counterparts.

Operating on a decentralized blockchain platform, ScaleChain offers unparalleled security and transparency in transactions. Leveraging smart contract technology, ScaleChain enables automated payments for services rendered by robots, spanning from industrial tasks to household chores, while also serving as a convenient payment gateway for humans engaging with automated systems.

With a focus on interoperability and user-friendly features, ScaleChain aims to streamline the exchange of value between robots and humans, fostering a dynamic ecosystem where automation enhances productivity and efficiency. Join us in shaping the future of commerce and collaboration with ScaleChain – where machines and humans unite through seamless transactions.

For the avoidance of doubt, this particular copy of the software is released under the version 3 of the GNU General Public License. It is brought to you by ScaleChain.

Copyright (c) 2015, ScaleChain and/or its affiliates. All rights reserved.

How to build
============
Create a jar file with all dependencies included.
```
gradle clean test shadowJar
```

Start up the node with the run.sh script.
```
# copy the environment template and edit as you want
cp scripts/.env-template scripts/.env

# run it
scripts/run.sh
```

Or you can run ScaleChainPeer class with the created jar included in the classpath.
```
java -cp ./scalechain-cli/build/libs/scalechain-cli-all.jar io.scalechain.blockchain.cli.ScaleChainPeer
```
How to test
============
Run unit tests 
```
gradle clean test
```

Run automated end to end test written in python
```
gradle clean test shadowJar
# kill all ScaleChainPeer java processes, and then run all end to end tests
scripts/kill-all.sh ; scripts/run-tests.sh
```

Getting Started
===============
[A guide on starting a ScaleChain peer to peer network.](https://github.com/ScaleChain/scalechain/wiki/How-to-start-a-ScaleChain-peer)

Supported Features
==================
- Compatible with Bitcoin remote procedure calls and peer-to-peer protocols.


Under construction
==================
ScaleChain source code is under construction. Big changes are to come to stablize the code.

Current project status
======================
Unit tests passed.
(Under construction) Automated end-to-end test.

License
=======
ScaleChain Commercial License for OEMs, ISVs, and VARs
ScaleChain provides its ScaleChain Server and Client Libraries under a dual license model designed to meet the development and distribution needs of both commercial distributors (such as OEMs, ISVs, and VARs) and open source projects.

For OEMs, ISVs, VARs and Other Distributors of Commercial Applications:
OEMs (Original Equipment Manufacturers), ISVs (Independent Software Vendors), VARs (Value Added Resellers) and other distributors that combine and distribute commercially licensed software with ScaleChain software and do not wish to distribute the source code for the commercially licensed software under version 3 of the GNU General Public License (the "GPL") must enter into a commercial license agreement with ScaleChain.

For Open Source Projects and Other Developers of Open Source Applications:
For developers of Free Open Source Software ("FOSS") applications under the GPL that want to combine and distribute those FOSS applications with ScaleChain software, ScaleChain open source software licensed under the GPL is the best option.

For developers and distributors of open source software under a FOSS license other than the GPL, ScaleChain makes its GPL-licensed ScaleChain Client Libraries available under a FOSS Exception that enables use of the ScaleChain Client Libraries under certain conditions without causing the entire derivative work to be subject to the GPL.
