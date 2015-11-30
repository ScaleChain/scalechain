# Spark Integration
ScaleChain supports analyzing block data files on Apache Spark.

# Step by step guide
The following step by step guide shows setting a Docker container that runs Spark on MacOS and running transaction analysis on Spark using ScaleChain.

## Start a Docker container with Spark
Run boot2docker, and execute following commands to setup a Docker container with Spark. 
```
cd dockerfiles
./spark.sh build
./spark.sh shell
```

You will see a bash shell on the Docker container that has Spark installed.

## Upload block files
First, convert blkNNNNN.dat files to a text file with hex format instead of binary format.
You can use DumpChain for it. The text file will be loaded onto a Spark RDD later.
```
java io.scalechain.blockchain.DumpChain <path to blkNNNNN.dat files> dump-block-index-data > block-index-data.txt
```
After running the above command, open the block-index-data.txt file and remove first 4 lines and last 2 lines which are written by sbt.

And then, upload block dump files to hdfs so that Spark can read them.
```
cd dockerfiles
# The block-index-data.txt was created by running DumpChain.
./cp.sh spark block-index-data.txt
```
Now, you will have /test directory in the docker container. In the test directory, you have the block-index-data.txt file.
We need to copy the dump file to hdfs so that it can be read by Spark.
```
# Run in the docker container.
hadoop fs -mkdir /user
hadoop fs -put /user/block-index-data.txt hdfs://test/block-index-data.txt
```

## Write a Spark app using ScaleChain
You can write a Spark app to parse transaction data in each block during the Spark RDD transformation.
You can even run Bitcoin scripts in each transaction while the transformation runs.

An example Spark app is on [SparkLoader](src/main/scala/io/scalechain/blockchain/SparkLoader.scala).

## Package the Spark app, and upload to a Spark cluster.
```
sbt compile package

# copy jar to Spark docker container
tar -c target/scala-2.10/scalechain_2.10-1.0.jar | docker exec -i spark /bin/tar -C /test -x
```

## Run the Spark app on your Spark cluster.
```
# run the spark app, SparkLoader.
docker exec -i spark /usr/local/spark/bin/spark-submit --class "io.scalechain.blockchain.SparkLoader" --master local[4] /test/target/scala-2.10/scalechain_2.10-1.0.jar
```