NAME=$1
./stop.sh $NAME
docker rm -f $NAME
docker rmi -f $NAME
