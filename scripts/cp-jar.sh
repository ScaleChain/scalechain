# Create the test folder.
docker exec -i spark /bin/mkdir /test

# Copy the jar file.
tar -c ../target/scala-2.10/scalechain_2.10-1.0.jar | docker exec -i spark /bin/tar -C /test -x

