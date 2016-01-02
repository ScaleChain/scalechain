NAME=$1
#docker exec -t -i $NAME /bin/bash
docker run --name $NAME -t -i $NAME /bin/bash 
