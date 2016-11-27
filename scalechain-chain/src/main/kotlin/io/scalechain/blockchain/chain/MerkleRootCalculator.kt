package io.scalechain.blockchain.chain

import io.scalechain.blockchain.proto.Transaction
import io.scalechain.blockchain.proto.Hash
import io.scalechain.blockchain.script.hash
import io.scalechain.crypto.HashFunctions

/**
  * Calculates Merkle root hash from transactions.
  */
object MerkleRootCalculator {
  /** Concatenate two hash values and calculate double SHA256 on it.
    *
    * @param hash1 The first hash value
    * @param hash2 The second hash value
    * @return The hash value calcuated from the concatenated hash values.
    */
  private fun mergeHash(hash1 : Hash, hash2 : Hash) : Hash {
    val concatenated = hash1.value + hash2.value

    return Hash( HashFunctions.hash256(concatenated).value )
  }
  /** Calculate the merkle root hash. The number of input hash values are always even.
    *
    * @param hashes The list of hashes for calculating the merkle root hash.
    * @return
    */
  private fun mergeHashes(hashes : List<Hash>) : List<Hash> {
    // The number of hashes should be even.
    // TODO : Optimize, i%2==0 could be expensive.
    assert( hashes.size % 2 == 0)

    // Note : We may duplicate the last element, so prepare space for one more element in the array buffer.
    val mergedHashes = arrayListOf<Hash>() //(hashes.length/2 + 1)

    for (i in 0 until hashes.size) {
      // TODO : Optimize, i%2==0 could be expensive.
      if (i % 2 == 0) {
        mergedHashes += mergeHash( hashes[i], hashes[i+1] )
      }
    }

    return calculateMerkleRoot(mergedHashes)
  }


  /** Duplicate the last item of the number of hash values is odd, and call calculateAlwaysEven.
    *
    * @param hashes The list of hashes for calculating the merkle root hash.
    * @return
    */
  private fun calculateMerkleRoot(hashes : MutableList<Hash>) : List<Hash> {
    assert (hashes.size > 0)
    if (hashes.size == 1) { // The base condition. If the number of hashes is one, we are done.
      return hashes
    } else {
      // TODO : Optimize, i%2==0 could be expensive.
      if (hashes.size % 2 == 1) { // If the number of hashes is odd, duplicate the last one to make it even.
        hashes += hashes.last()
      }
      return mergeHashes(hashes)
    }
  }

  /** Calculate merkle root hash from a list of transactions.
    *
    * Recursive invocation steps:
    * calculate
    *   -> calculateMerkleRoot
    *      -> mergeHashes
    *         -> calculateMerkleRoot
    *            -> mergeHashes
    *               ... when it reaches the base condition, the invocation finishes ...
    *
    * @param transactions The list of transactions for calculating the merkle root hash.
    * @return The calculated merkle root hash.
    */
  fun calculate(transactions : List<Transaction>) : Hash {
    // Step 1 : Calculate transaction hashes for each transaction.
    // Note : We may duplicate the last element, so prepare space for one more element in the array buffer.
    val transactionHashes = arrayListOf<Hash>()//(transactions.length + 1)
    transactions.forEach { transaction -> transactionHashes.add( transaction.hash() ) }

    // Step 2 : Duplicate the last hash item if the number of hashes is odd, and calculate the merkle root hash.
    val merkleRootHashes : List<Hash> = calculateMerkleRoot( transactionHashes )

    assert( merkleRootHashes.size == 1 )
    return merkleRootHashes[0]
  }
}
