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
The netty component in Camel is a socket communication component, based on the Netty project.
Netty is a NIO client server framework which enables quick and easy development of network applications such as protocol servers and clients.
Netty greatly simplifies and streamlines network programming such as TCP and UDP socket server.

http://camel.apache.org/netty.html

## Tutorial : Akka,Camel,Netty
https://vijayannadi.wordpress.com/tutorials/131-2/

## Netty StringEncoder
https://github.com/netty/netty/blob/4.1/codec/src/main/java/io/netty/handler/codec/string/StringEncoder.java

## Netty StringDecoder
https://github.com/netty/netty/blob/4.1/codec/src/main/java/io/netty/handler/codec/string/StringDecoder.java


#Documents
## Akka Remote
http://doc.akka.io/docs/akka/snapshot/java/remoting.html

## Camel-Spring
http://camel.apache.org/spring.html

## Camel Fluent Builders
Camel provides fluent builders for creating routing and mediation rules using a type-safe IDE friendly way which provides smart completion and is refactoring safe.
http://camel.apache.org/fluent-builders.html

## Developer guide.
https://bitcoin.org/en/developer-guide#p2p-network

## Developer reference.
https://bitcoin.org/en/developer-reference#p2p-network


#Bitcoin
## main.cpp
Process messages received from each peer node.
```
bool static ProcessMessage(CNode* pfrom, string strCommand, CDataStream& vRecv, int64_t nTimeReceived)
```

## net.h
class CNetMessage ; Has P2P protocol message. A good place to dump all messages?


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

Receive a message to create CNetMessage.
```
bool CNode::ReceiveMsgBytes(const char *pch, unsigned int nBytes)
```

Send a message to peers.
```
void SocketSendData(CNode *pnode)
{
    std::deque<CSerializeData>::iterator it = pnode->vSendMsg.begin();

    while (it != pnode->vSendMsg.end()) {
        const CSerializeData &data = *it;
```

Receive a message. 
```
bool CNode::ReceiveMsgBytes(const char *pch, unsigned int nBytes)
{
    ...
        if (msg.complete()) {

```