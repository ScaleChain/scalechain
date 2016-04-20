# Design 

## Routing messages based on the message type
Within a connection, we will process messages one by one instead of routing messages to different workers for each message type.
For processing handling messages based on the message type, we will use 'collect', 
which processes messages based on the given partial function.

## Ability not to send any response
We should be able to decided whether to send any response to the TCP connection.
For example, upon the receival of Verack message, we should not send any response back.
We can achieve this by using 'collect', which converts a message to either Some(message) for None.
On the next stage, we can filter out all None values.

## Source with mutable sequence
We need to be able to send messages to an existing connection(stream). 

### Option 1 : Source.fromIterator(ConcurrentLinkedQueue)

Simply, this does not work. Source.fromIterator creates an immutable iterator based on the mutable iterator.
It views the elements by the time the mutable iterator was passed to the fromIterator method.
```
We will use ConcurrentLinkedQueue to create a source w/ Source.fromIterator.
https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ConcurrentLinkedQueue.html

We need a concurrent queue, as the queue should be accessible by multiple threads.
```

### Option 2 : Source.queue

Source.queue materializes SourceQueue, but not sure if it is thread-safe.
Because it is not sure, we won't choose this option.

### Option 3 : Implement SourceGraph that materializes ConcurrentLinkedQueue

We will create a ConcurrentQueueSource, which create a Source[T, ConcurrentQueueSource[T]].
Multiple threads can pass items to the materialized queue.


## Ability to send messages from a running stream to any connection opened by any peer.

### (To all) blocks/transactions propagation.
We need to be able to propagate new (valid) blocks or transactions to all peers. 

### (To some) block requests 
We need to be able to requests block data to some of peers for IBD(Initial block download).

### (To any) block header request
We need to be able to request block headers to a peer. We can start with the first connected peer,
but we need to choose a peer if the current peer for requesting block headers was disconnected.

## Concurrent access to the set of peers.
As following threads can access the set of peers, the access of information for each peer, 
or the request to send messages to any/some/all peers should be thread-safe.

1. getpeerinfo RPC can get the information of each peer.
2. Data(ex>blocks/transactions) propagation can happen from a TCP connection to any/some/all peers.
The TCP connection is materialized as an actor, which can run in a different thread than the getpeerinfo RPC.

## Concurrent access to the block/transaction database.
As following threads can access the block/transaction database, the access to the database should be thread-safe.

1. RPC calls such as getblock, getrawtransaction access the block/transaction database.
2. Upon the receival of messages such as block or transaction, the materialized actors of a TCP connection
should be able to put blocks or transactions on the block/transaction database.

## No actors, but all streams.
We won't use actors, as they require manual back pressure management. 
We will use Akka Streams which will be meterialized to actors in the end.

## Using Materialized values as source of another stream.
Upon the receival of a message from a peer, we should be able to send it to any, some, all peers.
We will use the 'source with mutable sequence' of each peer that sends messages to each connected peer.

## Disconnection detection
We need to be able to detect the TCP disconnection using the Akka Streams. 
Once it happens for a peer, all subscribers should be notified. 
For example the IBD process should continue even though the TCP connection closes with a peer.

# Examples

The followings are akka streams examples.

### Word count
https://github.com/pkinsky/akka-streams-example/blob/master/src/main/scala/WordCount.scala
