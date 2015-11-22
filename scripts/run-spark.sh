pushd .

cd ..
# build jar
sbt compile package

# copy jar to Spark docker container
tar -c target/scala-2.11/scalachain_2.11-1.0.jar | docker exec -i spark /bin/tar -C /test -x

# run the spark app, SparkLoader.
docker exec -i spark /usr/local/spark/bin/spark-submit --class "io.scalechain.blockchain.SparkLoader" --master local[4] /test/target/scala-2.11/scalachain_2.11-1.0.jar

popd

