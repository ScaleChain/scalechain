package io.scalechain.blockchain.proto.codec

interface CodecTestUtil {
  fun<T> decodeFully(codec:Codec<T>, bytes : ByteArray) : T = codec.decode(bytes)!!
  fun<T> encode(codec:Codec<T>, message : T) : ByteArray = codec.encode(message)
  fun<T> roundTrip(codec:Codec<T>, message : T) : Boolean {
    return codec.decode(codec.encode(message)) == message
  }
}

