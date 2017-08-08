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

import io.netty.buffer.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.scalechain.blockchain.proto.ProtocolMessage;
import io.scalechain.blockchain.proto.codec.BitcoinProtocol;
import io.scalechain.blockchain.proto.codec.BitcoinProtocolCodec;
import io.scalechain.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.util.List;

/**
 * Encodes the requested case class that represents a bitcoin protocol message into a {@link ByteBuf}.
 */

@Sharable
public class BitcoinProtocolEncoder extends MessageToMessageEncoder<ProtocolMessage> {
    public BitcoinProtocolEncoder() {
    }

    private BitcoinProtocolCodec codec = new BitcoinProtocolCodec( new BitcoinProtocol() );

    /**
     * Allocate a {@link ByteBuf} which will be used for constructing an encoded byte buffer of protocol message.
     * BUGBUG : Modify this method to return a {@link ByteBuf} with a perfect matching initial capacity.
     */
    protected ByteBuf allocateBuffer(
      ChannelHandlerContext ctx,
      @SuppressWarnings("unused") ProtocolMessage msg) throws Exception {
        return ctx.alloc().ioBuffer(1024);
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage msg, List<Object> out) throws Exception {
        ByteBuf encodedByteBuf = allocateBuffer(ctx, msg);

        codec.encode(msg, encodedByteBuf);

        out.add(encodedByteBuf);
    }
}
