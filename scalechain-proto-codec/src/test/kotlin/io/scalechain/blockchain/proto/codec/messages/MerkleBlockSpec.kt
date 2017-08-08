package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto.*
import io.scalechain.blockchain.proto.codec.*
import io.scalechain.util.HexUtil.bytes


/**
  *  <Bitcoin Core Packets Not Captured>
  *
  *  01000000 ........................... Block version: 1
  *  82bb869cf3a793432a66e826e05a6fc3
  *  7469f8efb7421dc88067010000000000 ... Hash of previous block's header
  *  7f16c5962e8bd963659c793ce370d95f
  *  093bc7e367117b3c30c1f8fdd0d97287 ... Merkle root
  *  76381b4d ........................... Time: 1293629558
  *  4c86041b ........................... nBits: 0x04864c * 256**(0x1b-3)
  *  554b8529 ........................... Nonce
  *
  *  07000000 ........................... Transaction count: 7
  *  04 ................................. Hash count: 4
  *
  *  3612262624047ee87660be1a707519a4
  *  43b1c1ce3d248cbfc6c15870f6c5daa2 ... Hash #1
  *  019f5b01d4195ecbc9398fbf3c3b1fa9
  *  bb3183301d7a1fb3bd174fcfa40a2b65 ... Hash #2
  *  41ed70551dd7e841883ab8f0b16bf041
  *  76b7d1480e4f0af9f3d4c3595768d068 ... Hash #3
  *  20d2a7bc994987302e5b1ac80fc425fe
  *  25f8b63169ea78e68fbaaefa59379bbf ... Hash #4
  *
  *  01 ................................. Flag bytes: 1
  *  1d ................................. Flags: 1 0 1 1 1 0 0 0
  *
  */
/*

@RunWith(KTestJUnitRunner::class)
class MerkleBlockSpec : PayloadTestSuite<MerkleBlock>  {

  val codec = MerkleBlockCodec.codec

  val payload = bytes(
    """
      01000000                        
      82bb869cf3a793432a66e826e05a6fc3
      7469f8efb7421dc88067010000000000
      7f16c5962e8bd963659c793ce370d95f
      093bc7e367117b3c30c1f8fdd0d97287
      76381b4d                        
      4c86041b                        
      554b8529                        
      
      07000000                        
      04                              
      
      3612262624047ee87660be1a707519a4
      43b1c1ce3d248cbfc6c15870f6c5daa2
      019f5b01d4195ecbc9398fbf3c3b1fa9
      bb3183301d7a1fb3bd174fcfa40a2b65
      41ed70551dd7e841883ab8f0b16bf041
      76b7d1480e4f0af9f3d4c3595768d068
      20d2a7bc994987302e5b1ac80fc425fe
      25f8b63169ea78e68fbaaefa59379bbf
      
      01                              
      1d                              
    """)

  val message = null//MerkleBlock()

}
*/