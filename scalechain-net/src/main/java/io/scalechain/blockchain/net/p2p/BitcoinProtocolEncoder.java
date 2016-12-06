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

    //private BitcoinProtocolCodec codec = new BitcoinProtocolCodec( new BitcoinProtocol() );

    @Override
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage msg, List<Object> out) throws Exception {
        // TODO : Implement
        /*
        // TODO : Make sure that we are not having any performance issue here.
        byte[] bytes = codec.encode(msg);

        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);

        out.add( buffer );
        */
    }
}
