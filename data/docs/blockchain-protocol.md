#Introduction

Implement Bitcoin blockchain protocol according to the protocol specification in the following link. 

https://en.bitcoin.it/wiki/Protocol_documentation

#Protocol parser
TODO : Write

#Protocol serializer
TODO : Write

#Akka
Send and receive messages using akka actors. Read and write messages from/to a socket.
```
http://doc.akka.io/docs/akka/snapshot/scala/io-tcp.html
```
Communicate with a remote actor.
```
http://doc.akka.io/docs/akka/snapshot/scala/remoting.html
```

#Code Examples
## Akka-camel
http://doc.akka.io/docs/akka/snapshot/scala/camel.html

## Camel-Netty 
http://camel.apache.org/netty.html

## Tutorial : Akka,Camel,Netty
https://vijayannadi.wordpress.com/tutorials/131-2/

## Netty encoder
https://github.com/netty/netty/blob/4.1/codec/src/main/java/io/netty/handler/codec/string/StringEncoder.java

#Documents
## Akka Remote
http://doc.akka.io/docs/akka/snapshot/java/remoting.html

#Bitcoin
## main.cpp
Process messages received from each peer node.
```
bool static ProcessMessage(CNode* pfrom, string strCommand, CDataStream& vRecv, int64_t nTimeReceived)
```
## net.cpp
Get peer address from DNS seeds.
```
void ThreadDNSAddressSeed()
```

Open connections to peers.
```
void ThreadOpenConnections()
void ThreadOpenAddedConnections()
bool OpenNetworkConnection(const CAddress& addrConnect, CSemaphoreGrant *grantOutbound, const char *pszDest, bool fOneShot)
```

Receive messages from each peer node, call process them, send reponse messages.
```
void ThreadMessageHandler()
```
