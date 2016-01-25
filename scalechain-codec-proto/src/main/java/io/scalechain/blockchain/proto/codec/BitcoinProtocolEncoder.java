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
package io.scalechain.blockchain.proto.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Encodes the requested case class that represents a bitcoin protocol message into a {@link ByteBuf}.
 */
@Sharable
public class BitcoinProtocolEncoder extends MessageToMessageEncoder<CharSequence> {

    // TODO Use CharsetEncoder instead.
    private final Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public BitcoinProtocolEncoder() {
        this(Charset.defaultCharset());
    }

    /**
     * Creates a new instance with the specified character set.
     */
    public BitcoinProtocolEncoder(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, CharSequence msg, List<Object> out) throws Exception {
        if (msg.length() == 0) {
            return;
        }

        System.out.println("[Debug] BitcoinProtocolEncoder : " + msg);

        out.add(ByteBufUtil.encodeString(ctx.alloc(), CharBuffer.wrap(msg), charset));
    }
}
