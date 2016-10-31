# Configuration
## Consensus

### config/hosts.config

In case there are three nodes installed on three machines whose IP addresses are 10.10.10.1, 10.10.10.2, 10.10.10.3, and 10.10.10.4, specify unique server ids starting from 0 with the IPs.

The port 11000 is for the channel to keep block data in consensus by using BFT-smart algorithm.

```
#server id, address and port (the ids from 0 to n-1 are the service replicas)
0 10.10.10.1 11000
1 10.10.10.2 11000
2 10.10.10.3 11000
3 10.10.10.4 11000
```

### config/system.config

Need to modify the following configuration depending on the number of servers in the blockchain network.
system.bft should be true only if there are at least four servers.

Only one server in the blockchain network : 
```
#Number of servers in the group
system.servers.num = 1

#Maximum number of faulty replicas
system.servers.f = 0

...

#Replicas ID for the initial view, separated by a comma.
# The number of replicas in this parameter should be equal to that specified in 'system.servers.num'
system.initial.view = 0

...

#This sets if the system will function in Byzantine or crash-only mode. Set to "true" to support Byzantine faults
system.bft = false

```


Two servers in the blockchain network : 
```
#Number of servers in the group
system.servers.num = 2

#Maximum number of faulty replicas
system.servers.f = 0

...

#Replicas ID for the initial view, separated by a comma.
# The number of replicas in this parameter should be equal to that specified in 'system.servers.num'
system.initial.view = 0,1

...

#This sets if the system will function in Byzantine or crash-only mode. Set to "true" to support Byzantine faults
system.bft = false
```

Three servers in the blockchain network :    
```
#Number of servers in the group
system.servers.num = 3

#Maximum number of faulty replicas
system.servers.f = 0

...

#Replicas ID for the initial view, separated by a comma.
# The number of replicas in this parameter should be equal to that specified in 'system.servers.num'
system.initial.view = 0,1,2

...

#This sets if the system will function in Byzantine or crash-only mode. Set to "true" to support Byzantine faults
system.bft = false
```


Four servers in the blockchain network :    
```
#Number of servers in the group
system.servers.num = 4

#Maximum number of faulty replicas
system.servers.f = 1

...

#Replicas ID for the initial view, separated by a comma.
# The number of replicas in this parameter should be equal to that specified in 'system.servers.num'
system.initial.view = 0,1,2,3

...

#This sets if the system will function in Byzantine or crash-only mode. Set to "true" to support Byzantine faults
system.bft = true
```

## config/scalechain.conf

scalechain.conf specifies the api.port, which is the port used for RPC service, and p2p.port, which is used for the transaction/block channel between peers.
p2p.peers should list all peer IPs with the p2p.port of each peer.
```
scalechain {
 
  ...... 

  api {
    port = 8080
    user = "user"
    password = "pleasechangethispassword123@.@"
  }
  p2p {
    port = 7643
    peers = [
      { address:"10.10.10.1", port:"7643" }
      { address:"10.10.10.2", port:"7643" }
    ]
  }

  ... and more ...

}
```

scalechain.conf also specifies the block size in bytes. The default block size is 128KB.
```
  mining {
    max_block_size = 131072
```

## .env

The .env file is read by run-assembly.sh, which starts the scalechain daemon.

RPC_PORT_BASE and P2P_PORT_BASE should match the api.port and p2p.port in the scalechain.conf respectively.
CASSANDRA_IP_LAST_BASE is unused.

In JAVA_OPTIONS, you can specify JVM options such as the maximum memory used by JVM.

MINER_INITIAL_DELAY_MS is the time to sleep before the mining thread starts. You need to start all scalechain daemons within this time window. (Unit : milli-seconds )

MINER_HASH_DELAY_MS specifies the maximum interval of block creation in milisecond for each node. The block creation interval is calculated with the following formula. (Unit : milli-seconds )

( MINER_HASH_DELAY_MS / 2 ) / (the number of servers)

For example, with MINER_HASH_DELAY_MS 200000 and only one server, 200000 / 2 / 1 => 100000 ms => 100 second. The block is created every 100 second.


For example, with MINER_HASH_DELAY_MS 200000 and four servers, 200000 / 2 / 4 => 25000 ms => 25 second. The block is created every 25 second.

```
RPC_PORT_BASE=8080
P2P_PORT_BASE=7643
CASSANDRA_IP_LAST_BASE=101

#JAVA_OPTIONS="-verbosegc -Xmx2048m -Dio.netty.leakDetectionLevel=paranoid -XX:+AggressiveOpts"
#JAVA_OPTIONS="-server -d64 -XX:NewSize=4m -XX:+AggressiveOpts -XX:MaxDirectMemorySize=256M -Xmx2048m -Dio.netty.leakDetectionLevel=paranoid"
JAVA_OPTIONS="-server -d64 -XX:NewSize=4m -XX:+AggressiveOpts -Xmx4096m -Dio.netty.leakDetectionLevel=paranoid"
MINER_INITIAL_DELAY_MS=10000
MINER_HASH_DELAY_MS=200000
```

# Running ScaleChain
nohup ./run-assembly.sh

