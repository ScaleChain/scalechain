# Introduction
This document summarizes steps for installing Cassandra and ScaleChain using Docker Swarm.

# Docker Swarm Cluster.
## Setup Colsul
```
docker-machine create -d virtualbox default
docker-machine start default
eval "$(docker-machine env default)"
docker run -d \
    -p "8500:8500" \
    -h "consul" \
    progrium/consul -server -bootstrap
```

## Create Swarm cluster
Create Swarm Master.
```
docker-machine create \
-d virtualbox \
--swarm --swarm-master \
--swarm-discovery="consul://$(docker-machine ip default):8500" \
--engine-opt="cluster-store=consul://$(docker-machine ip default):8500" \
--engine-opt="cluster-advertise=eth1:2376" \
swarm-master
```

Create Worker Nodes
```
docker-machine create -d virtualbox \
    --swarm \
    --swarm-discovery="consul://$(docker-machine ip default):8500" \
    --engine-opt="cluster-store=consul://$(docker-machine ip default):8500" \
    --engine-opt="cluster-advertise=eth1:2376" \
  swarm-worker-01
  
docker-machine create -d virtualbox \
    --swarm \
    --swarm-discovery="consul://$(docker-machine ip default):8500" \
    --engine-opt="cluster-store=consul://$(docker-machine ip default):8500" \
    --engine-opt="cluster-advertise=eth1:2376" \
  swarm-worker-02
    
docker-machine create -d virtualbox \
    --swarm \
    --swarm-discovery="consul://$(docker-machine ip default):8500" \
    --engine-opt="cluster-store=consul://$(docker-machine ip default):8500" \
    --engine-opt="cluster-advertise=eth1:2376" \
  swarm-worker-03    
```

Create Overlay Network
```
eval $(docker-machine env --swarm swarm-master)
docker network create --driver overlay --subnet=10.0.1.0/24 swarm-net
docker network ls
```

# Cassandra on Swarm
## Create three Cassandra instances on Swarm
Create Volumes 
```
docker volume create --name=testvol1
docker volume create --name=testvol2
docker volume create --name=testvol3

# docker volume create -d flocker --name=testvol1 -o size=10G
# docker volume create -d flocker --name=testvol2 -o size=10G
# docker volume create -d flocker --name=testvol3 -o size=10G
```

Check Volume Names
```
docker volume ls

## Output 
DRIVER              VOLUME NAME
local               swarm-master/3dbe43d994652c332f827c26fc82fc1d4f5e6cfdefca06792f9524b8e65b6f27
local               swarm-master/4f975fe10e5c452432894f2efeadba5f5e31d962af28514d79f57654a843a26d
local               swarm-master/testvol1
local               swarm-master/testvol2
local               swarm-master/testvol3
local               swarm-worker-01/e1b32812fca71d758e98ffd0bcc5af3c8651f4717a4c14437b276f3b651fcc2c
local               swarm-worker-01/testvol1
local               swarm-worker-01/testvol2
local               swarm-worker-01/testvol3
local               swarm-worker-02/978f60c1ad1ec001b1d8b9b581cc657b79b389a8f44c9f197d89a06dd324a738
local               swarm-worker-02/testvol1
local               swarm-worker-02/testvol2
local               swarm-worker-02/testvol3
local               swarm-worker-03/7e70da6aa776d542d873763d6bd18c753bcc99d79bce3d3983dac2c9898dac4d
local               swarm-worker-03/testvol1
local               swarm-worker-03/testvol2
local               swarm-worker-03/testvol3

```

Create the docker compose file. Need to copy hex volume names from the above output to the below volumes section.
```
version: '2'
services:
  cassandra-1:
    image: cassandra
    container_name: cassandra-1
    environment:
      CASSANDRA_BROADCAST_ADDRESS: "cassandra-1"
    ports:
    - 7000
    volumes:
    - "cassandra1:/var/lib/cassandra"
    restart: always
  cassandra-2:
    image: cassandra
    container_name: cassandra-2
    environment:
      CASSANDRA_BROADCAST_ADDRESS: "cassandra-2"
      CASSANDRA_SEEDS: "cassandra-1"
    ports:
    - 7000
    depends_on:
      - cassandra-1
    volumes:
    - "cassandra2:/var/lib/cassandra"
    restart: always
  cassandra-3:
    image: cassandra
    container_name: cassandra-3
    environment:
      CASSANDRA_BROADCAST_ADDRESS: "cassandra-3"
      CASSANDRA_SEEDS: "cassandra-1"
    ports:
    - 7000
    depends_on:
      - cassandra-2
    volumes:
    - "cassandra3:/var/lib/cassandra"
    restart: always
    
volumes:
  cassandra1:
    external:
        name: e1b32812fca71d758e98ffd0bcc5af3c8651f4717a4c14437b276f3b651fcc2c
  cassandra2:
    external:
        name: 978f60c1ad1ec001b1d8b9b581cc657b79b389a8f44c9f197d89a06dd324a738
  cassandra3:
    external:
        name: 7e70da6aa776d542d873763d6bd18c753bcc99d79bce3d3983dac2c9898dac4d
        
networks:
  default:
    external:
       name: swarm-net

```

Start Cassandra
```
docker-compose -f cassandra-multi.yml up -d
```

Connect to Cassandra
```
docker run -it --rm --net=swarm-net cassandra sh -c 'exec cqlsh cassandra-1'
```

Check node status
```
docker run -it --rm --net=swarm-net cassandra sh -c 'exec nodetool -h cassandra-1 status'
```

Check if a port is open.
```
exec 6<>/dev/tcp/cassandra-1/9042
```

### Stop/Remove machines
```
docker-machine stop swarm-master swarm-worker-01 swarm-worker-02 swarm-worker-03
docker-machine rm swarm-master swarm-worker-01 swarm-worker-02 swarm-worker-03
```

# Rerefernces
### Docker Swarm on a local sandbox.
https://docs.docker.com/swarm/install-w-machine/
## Get started with multi-host networking
https://docs.docker.com/engine/userguide/networking/get-started-overlay/
## Cassandra and Docker Lessons Learned
http://www.slideshare.net/planetcassandra/cassandra-and-docker-lessons-learnt
## Running Cassandra cluster
https://clusterhq.com/2016/03/09/fun-with-swarm-part1/
## Cassandra cluster on Docker Swarm and Overlay Networking using Docker Experimental 1.9
http://sirile.github.io/2015/09/30/cassandra-cluster-on-docker-swarm-and-overlay-networking-using-docker-experimental-1.9.html
## Running cassandra as an embedded service.
http://prettyprint.me/prettyprint.me/2010/02/14/running-cassandra-as-an-embedded-service/index.html