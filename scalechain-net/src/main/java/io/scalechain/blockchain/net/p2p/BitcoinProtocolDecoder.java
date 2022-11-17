/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.scalechain.blockchain.net.p2p;


import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.scalechain.blockchain.proto.ProtocolMessage;
import io.scalechain.blockchain.proto.codec.BitcoinProtocol;
import io.scalechain.blockchain.proto.codec.BitcoinProtocolCodec;

import java.util.List;
import java.util.Vector;

/**
 * Decodes a received {@link ByteBuf} into a case class that represents Bitcoin protocol message.
 */
@Sharable
public class BitcoinProtocolDecoder extends MessageToMessageDecoder<ByteBuf> {

    /**
     * An incomplete message, which needs to receive more data to construct a complete message.
     *
     * This happens situations as follow.
     * (1) when the data received is less than 24 bytes, which is the length for the header of bitcoin message.
     * (2) when we received less than the length specified at the payload length( BitcoinMessageEnvelope.length ).
     */
//    private BitVector incompleteMessage = null;

    /**
     * Creates a new instance with the current system character set.
     */
    public BitcoinProtocolDecoder() {
    }

    private BitcoinProtocolCodec codec = new BitcoinProtocolCodec( new BitcoinProtocol() );

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
        codec.decode(msg, out);
        /*
        if (out.size() > 0) {
            System.out.println("decoded : " + out.size());
        } else {
            System.out.println("nothing decoded.");
        }
        */
    }
}
