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
Create the docker compose file. Need to copy hex volume names from the above output to the below volumes section.
```
version: '2'
services:
  cassandra-01:
    image: cassandra
    container_name: cassandra-01
    environment:
      CASSANDRA_BROADCAST_ADDRESS: "cassandra-01"
    ports:
    - 7000
    - "9042:9042"
    restart: always
  cassandra-02:
    image: cassandra
    container_name: cassandra-02
    environment:
      CASSANDRA_BROADCAST_ADDRESS: "cassandra-02"
      CASSANDRA_SEEDS: "cassandra-01"
    ports:
    - 7000
    - "9042:9042"
    depends_on:
      - cassandra-01
    restart: always
  cassandra-03:
    image: cassandra
    container_name: cassandra-03
    environment:
      CASSANDRA_BROADCAST_ADDRESS: "cassandra-03"
      CASSANDRA_SEEDS: "cassandra-01"
    ports:
    - 7000
    - "9042:9042"
    depends_on:
      - cassandra-02
    restart: always
        
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
docker run -it --rm --net=swarm-net cassandra sh -c 'exec cqlsh cassandra-01'
```

Check node status
```
docker run -it --rm --net=swarm-net cassandra sh -c 'exec nodetool -h cassandra-01 status'
```

Check if a port is open.
```
exec 6<>/dev/tcp/cassandra-1/9042
```

### Restart machines
```
docker-machine regenerate-certs swarm-master swarm-worker-01 swarm-worker-02 swarm-worker-03
docker-machine restart swarm-master swarm-worker-01 swarm-worker-02 swarm-worker-03
```

### Stop/Remove machines
```
docker-machine regenerate-certs swarm-master swarm-worker-01 swarm-worker-02 swarm-worker-03
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