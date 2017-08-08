package io.scalechain.blockchain.proto.codec.primitive

import io.netty.buffer.ByteBuf
import io.scalechain.blockchain.proto.codec.Codec
import io.scalechain.blockchain.proto.codec.CodecInputOutputStream
import io.scalechain.util.writeUnsignedInt
import io.scalechain.util.writeUnsignedIntLE

class Int32Codec() : Codec<Int> {
    override fun transcode(io : CodecInputOutputStream, obj : Int? ) : Int? {
        if (io.isInput) {
            return io.byteBuf.readInt()
        } else {
            io.byteBuf.writeInt(obj!!)
            return null
        }
    }
}


class Int32LCodec() : Codec<Int> {
    override fun transcode(io : CodecInputOutputStream, obj : Int? ) : Int? {
        if (io.isInput) {
            return io.byteBuf.readIntLE()
        } else {
            io.byteBuf.writeIntLE(obj!!)
            return null
        }
    }
}


class UInt32Codec() : Codec<Long> {
    override fun transcode(io : CodecInputOutputStream, obj : Long? ) : Long? {
        if (io.isInput) {
            return io.byteBuf.readUnsignedInt()
        } else {
            io.byteBuf.writeUnsignedInt(obj!!)
            return null
        }
    }
}

class UInt32LCodec() : Codec<Long> {
    override fun transcode(io : CodecInputOutputStream, obj : Long? ) : Long? {
        if (io.isInput) {
            return io.byteBuf.readUnsignedIntLE()
        } else {
            io.byteBuf.writeUnsignedIntLE(obj!!)
            return null
        }
    }
}

