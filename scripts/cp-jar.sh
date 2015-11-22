# Create the test folder.
docker exec -i spark /bin/mkdir /test

# Copy the jar file.
tar -c ../target/scala-2.11/scalachain_2.11-1.0.jar | docker exec -i spark /bin/tar -C /test -x

