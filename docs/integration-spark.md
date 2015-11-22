# Spark Integration
ScaleChain supports Spark integration to load blockchain data for analysis or machine learning. 

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