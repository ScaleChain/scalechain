NAME=$1
PORT=$2
docker run --name $NAME -d -p $PORT:$PORT $NAME
