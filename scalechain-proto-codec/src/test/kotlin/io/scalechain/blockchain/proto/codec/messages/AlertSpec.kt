package io.scalechain.blockchain.proto.codec.messages

import io.scalechain.blockchain.proto._
import io.scalechain.blockchain.proto.codec._
import io.scalechain.util.HexUtil._
import scodec.bits.BitVector

/**
  *  [Bitcoin Core Packets Not Captured]
  *  73 ................................. Bytes in encapsulated alert: 115
  *  01000000 ........................... Version: 1
  *  3766404f00000000 ................... RelayUntil: 1329620535
  *  b305434f00000000 ................... Expiration: 1330917376
  *
  *  f2030000 ........................... ID: 1010
  *  f1030000 ........................... Cancel: 1009
  *  00 ................................. setCancel count: 0
  *
  *  10270000 ........................... MinVer: 10000
  *  48ee0000 ........................... MaxVer: 61000
  *  00 ................................. setUser_agent bytes: 0
  *  64000000 ........................... Priority: 100
  *
  *  00 ................................. Bytes In Comment String: 0
  *  46 ................................. Bytes in StatusBar String: 70
  *  53656520626974636f696e2e6f72672f
  *  666562323020696620796f7520686176
  *  652074726f75626c6520636f6e6e6563
  *  74696e67206166746572203230204665
  *  627275617279 ....................... Status Bar String: "See [...]"
  *  00 ................................. Bytes In Reserved String: 0
  *
  *  47 ................................. Bytes in signature: 71
  *  30450221008389df45f0703f39ec8c1c
  *  c42c13810ffcae14995bb648340219e3
  *  53b63b53eb022009ec65e1c1aaeec1fd
  *  334c6b684bde2b3f573060d5b70c3a46
  *  723326e4e8a4f1 ..................... Signature
  */
/*
class AlertSpec extends PayloadTestSuite[Alert]  {

  val codec = AlertCodec.codec

  val payload = bytes(
    """
      73                                   
      01000000                             
      3766404f00000000                     
      b305434f00000000                     
      
      f2030000                             
      f1030000                             
      00                                   
      
      10270000                             
      48ee0000                             
      00                                   
      64000000                             
      
      00                                   
      46                                   
      53656520626974636f696e2e6f72672f
      666562323020696620796f7520686176
      652074726f75626c6520636f6e6e6563
      74696e67206166746572203230204665
      627275617279                         
      00                                   
      
      47                                   
      30450221008389df45f0703f39ec8c1c
      c42c13810ffcae14995bb648340219e3
      53b63b53eb022009ec65e1c1aaeec1fd
      334c6b684bde2b3f573060d5b70c3a46
      723326e4e8a4f1                       
    """)

  val message = null//Alert()

}
*/