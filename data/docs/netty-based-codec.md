# Project Summary
## What
Redesign proto-codec layer to use Netty's classes for implementing a codec such as ByteBuf, Encoder, Decoder, and so on.
## Why
1. We will use netty when we receive messages from other peers or send messages to other peers. 
2. We need to design our codec compatible with the classes already used by Netty to avoid unnecessary byte array copies.

## How
1. Use ByteBuf for implementing codec. 
2. Avoid copying byte arrays in the codec.

# Design Goal
## Zero copy
Do not copy bytes if possible. Use netty's ByteBuf whenever possible.
## Get rid of scodec
It turned out that scodec is the root of evil. It is very very slow!!

# Design
## Netty pipeline

### Receiving messages
BitcoinMessageFrameDecoder -> BitcoinProtocolDecoder -> BitcoinMessageHandler

### Sending messages
BitcoinMessageHandler -> BitcoinProtocolEncoder -> BitcoinMessageFrameEncoder

## Netty Codec components
### BitcoinMessageFrameDecoder, BitcoinMessageFrameEncoder

These two classes decodes and encodes Bitcoin message frame.
1. BitcoinMessageFrameDecoder decodes the frame and passes the payload to BitcoinProtocolDecoder.
2. BitcoinProtocolEncoder encodes a protocol message to create a payload, which is passed to BitcoinMessageFrameEncoder. It encodes the payload into a Bitcoin message frame.

```
 * Field Size,  Description,  Data type,  Comments
 * ================================================
 *           4,       magic,   uint32_t,  Magic value indicating message origin network, and used to seek to next message when stream state is unknown
 *          12,     command,   char[12],  ASCII string identifying the packet content, NULL padded (non-NULL padding results in packet rejected)
 *           4,      length,   uint32_t,  Length of payload in number of bytes
 *           4,    checksum,   uint32_t,  First 4 bytes of sha256(sha256(payload))
 *           ?,     payload,   uchar[],  The actual data
```

### BitcoinProtocolDecoder, BitcoinProtocolEncoder
These two classes decodes and encodes Bitcoin protocol entity such as transaction or block.
The BitcoinProtocolDecoder can assume that the full payload of a message was received, because BitcoinMessageFrameDecoder made sure that the payload was full, and the checksum of it was correct.

## Zero-copy
Not sure at this point how to achieve zero-copy. Need more investigations.
At least we should not copy data from ByteBuf. We have to use slice method get a part of the buffer.
Also, it will be great, if we can construct an (encoded) ByteBuf without any allocation of it, but use CompositeByteBuf by collecting multiple ByteBufs.


# Interfaces
## BitcoinCommand
```
data class MessagePayload(command : String, payload: ByteBuf)
```

## BitcoinMessageFrameDecoder (java)
```
public class BitcoinMessageFrameDecoder extends ByteToMessageDecoder {
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        ByteBuf payloadBuf = ... get the payload from the 'in' buffer ...
        String command = ... get the command from the 'in' buffer ...
        BitcoinMessage message = new BitcoinMessage(command, payloadBuf)
        out.add(message)
    }
}
```

## BitcoinMessageFrameEncoder (java)
```
public class BitcoinMessageFrameEncoder extends MessageToByteEncoder<MessagePayload> {
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage message, ByteBuf out) throws Exception {
        ... encode the message frame based on the given command via the message.command.
        ... encode the message frame based on the given payload via the message.payload.
        ... use out.write to encode the message frame fields such as magic, command, payload length, payload, and checksum
    }
}

```

## BitcoinProtocolDecoder (java)
```
public class BitcoinProtocolDecoder extends MessageToMessageDecoder<MessagePayload> {
    protected void decode(ChannelHandlerContext ctx, MessagePayload msg, List<Object> out) throws Exception {
        ... select a codec based on the msg.command ... 
        ProtocolMessage protocolMessage = ... call decode function on the codec providing msg.payload as an input data.
        out.add(protocolMessage);
    }
}
```

## BitcoinProtocolEncoder (java)
```
public class BitcoinProtocolEncoder extends MessageToMessageEncoder<ProtocolMessage> {
    protected void encode(ChannelHandlerContext ctx, ProtocolMessage msg, List<Object> out) throws Exception {
        ... select a codec based on the class object of the msg.
        String command = ... get the command based on the class object of the msg.
        String payload = ... encode the msg using the codec
        MessagePayload messagePayload = new MessagePayload(command, payload)
    }
}
```

## Codec
```
interface ProtocolCodec[T] {
    fun encode(message : T) : ByteBuf
    fun decode(ByteBuf) : T
}

```