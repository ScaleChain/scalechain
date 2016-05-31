# Requirements
## Support a block per 0.5 second
We need to be able to add a block per 0.5 second.
Need to re-think about the in-memory structure of block indexes.

Bitcoind uses both in-memory index and on-disk index for keeping the chain of blocks.
It might not be a good idea to store the on-disk index in memory 
when on average two blocks is created every second.

Assuming that the size of memory required to keep a block in memory is 1K, 
If we can use 10GB memory, we can store blocks created for 60 days. 
(conservative estimation, actual required memory would be about 256 bytes)


```
# How many block indexes can we store in memory when we have 10GB memory?
1024 * 1024 * 1024 * 10 / 1024
=> 10485760

# How many blocks are created per day?
2 * 3600 * 24
=> 172800

# How many days can we keep created blocks in memory?
10485760 / 172800
=> 60

```

If we can use 3TB hard drive, we can store blocks created for 61 years.

```
# How many block indexes can we store on-disk when we have a 3TB hard drive?
1024 * 1024 * 1024 * 1024 * 3 / 1024
=> 3221225472

# How many blocks are created per day?
2 * 3600 * 24
=> 172800

# How many days can we keep created blocks in memory?
3221225472 / 172800
=> 18641 ( 51 years )
```

# Design Goals
## Use Minimum amount of memory.
Don't use memory if possible. Use on-disk key/value database such as RocksDB.

## Higher concurrency.
Depend on concurrency on database systems such as RocksDB.
Try not to use synchronized block in Scala.

# RocksDB capabilities
## Fast enough. No reason to use in-memory collections.
With key=10 bytes, value=800 bytes, RocksDB shows the following benchmark result.
( A high-end server machine, and NOT Java interface, but C++ interface)

- Random Read : 8 micros/op, 126K ops/sec
- Random Write : 56.295 micros/op, 17K ops/sec

Details :
https://github.com/facebook/rocksdb/wiki/Performance-Benchmarks


# On-disk raw data 
A raw data file is 100MB, and multiple raw data files can exist on disk. The raw data files keep the raw block data in on-the wire format, as well as transactions in it.
Transactions are also written on disk in the on-the-wire format.

## Concept : Record storage
A record storage is where records are stored. A record is an entity of data to store, such as a block or a transaction.
The record storage supports append only. No in-place updates. Whenever a new record is appended, 
the appended record has a unique position in the record storage. The unique position of a record is called a record locator. 
The record locator can be used to read a record from the record storage.
A record locator consists of three fields. 
1) file number - the file number of the raw data file where the record is stored. 
2) offset - the offset in the specific raw data file.

### Appending a block
After appending a block, all transactions in it are appended as part of the block.

### Reading a block
A record locator for the block can be used to read the block from the record storage.

### Reading a transaction
A record locator for a transaction can be used to read a specific transaction fromthe record storage.

# On-disk index data
The record storage with raw data files provides storage for records such as blocks and transactions, 
but finding a specific block or transaction requires a full scan on the data files.

To reduce the amount of data to scan, on-disk indexes can be used.
For the on-disk indexes we have two options, 1) RocksDB, 2) LevelDB.
RocksDB seems to perform better according to RocksDB site -;, so we use RocksDB for the on-disk index data.


## Block 
### Key
- Block hash (32 bytes)

### Value
- Record locator : to read the raw block data from the record storage.
- Chain work : The estimated number of total hash calculations from the genesis block up to the block.
- Block height : The height of the block from the genesis block. The genesis block has height 0. 
- Block header : The header of the block. Contains the hash of the previous block. Every block except the genesis block has the previous block hash.
- Next block hash : the next block of the block. Only blocks in the best blockchain have the next block hash set. Blocks in a fork have the next block hash as null.
- Transaction count : The number of transactions. ( Need investigation : When is this field used? )

## Transaction
### Key
- transaction hash (32 bytes)

### Value
- record locator : to read the raw transaction data from the record storage.
- spending transactions of each output : Array of (record locator for the spending transaction, input index of the transaction). Could be None if the output was not spent yet.

## Orphan blocks

## Orphan transactions


