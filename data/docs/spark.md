# Spark Integration
ScaleChain supports Spark integration to load blockchain data for analysis or machine learning. 

For example, you can upload all block files (Ex> blk00001.dat) written by Bitcoin reference implementation,
and calculate the total amount of Bitcoins sent to outputs in each block. 


# How to store blockchain data
## data files
Block data is stored on disk in a format compatible to Bitcoin reference implementation.
It is possible to read block data in blkNNNNN.dat files written by bitcoin reference implementation as well.
For example, block headers and block data(mostly transaction data) will be stored on disk.

## Spark Pair RDD
Spark provides key, value storage with Pair RDDs. 
ScaleChain provides block/transaction search layer on top of Pair RDD.
For example, we will create a Pair RDD whose key is block hash, and whose data is the file name and offset of the block in the file.
By using this Pair RDD, we will be able to search a specific block data by block hash in O(log N) by leveraging indexes on the key.

Spark quick start:

http://spark.apache.org/docs/latest/quick-start.html

Details on Pair RDD:

https://www.safaribooksonline.com/library/view/learning-spark/9781449359034/ch04.html

## Tachyon
After Spark integration, Tachyon will be used for the data storage layer of Spark Pair RDD.
We will consider using StorageLevel.OFF_HEAP to store Spark RDD data onto Tachyon.
We will also see if we can use tiered storage on Tachyon with two level storage layers, 
(1) memory, and (2) disk to store data larger than the size of physical memory onto Tachyon.

An introduction to Tachyon:
 
http://tachyon-project.org/documentation/Getting-Started.html

Details on Tachyon Tiered Storage.

http://tachyon-project.org/documentation/Tiered-Storage-on-Tachyon.html


# Coding Plan 
1. Setup Spark with docker
2. Test spark integration from ScaleChain
3. Change Spark configuration to use Tachyon for dat storage layer
4. Change Tachyon configuration to use Tiered storage to store dataset larger than the physical memory.

# Running the spark loader
You can run the spark loader, which loads blockchain data into spark.

0. Run 'DumpChain <path to blkNNNNN.dat files> build-block-index-data > your/path/block-index-data.txt' to create block-index-data.txt to load to Spark.
1. Open the block-index-data.txt file and remove first 4 lines and last 2 lines.
2. Build jar ; sbt package
3. Start docker container that has Spark ; cd dockerfiles; ./spark.sh build; ./spark.sh shell
4. Copy jar ; cd scripts; ./cp-jar.sh
5. Copy block-index-data.txt; cd dockerfiles; ./cp.sh spark your/path/block-index-data.txt 
5. (on the shell we got from step 2) run following commands.

Copy the block index file block-index-data.txt on hdfs 
```
cd /test
hadoop fs -mkdir /test
hadoop fs -put block-index-data.txt /test
```

Run the SparkLoader Spark app; 
```
cd /test
spark-submit \
  --class "io.scalechain.cli.SparkLoader" \
  --master local[4] \
  target/scala-2.10/scalechain_2.10-1.0.jar
```

# Note

Because Spark 1.5.2 is shipped with Scala 2.10, ScaleChain uses Scala 2.10.x.
In case you need to use Scala 2.11, you can build Spark with Scala version 2.11, as Spark was using Scala 2.10.

from: http://spark.apache.org/docs/latest/building-spark.html#building-for-scala-211
```
./dev/change-scala-version.sh 2.11
mvn -Pyarn -Phadoop-2.4 -Dscala-2.11 -DskipTests clean package
```