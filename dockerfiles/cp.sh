NAME=$1
FILE=$2

# Create the test folder.
docker exec -i $NAME /bin/mkdir /test

# Copy the jar file.
tar -c $FILE | docker exec -i $NAME /bin/tar -C /test -x

