package ch6.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class OrderedByteBufferTest {
    public static void main(String[] args) {
        ByteBuf buf = Unpooled.buffer(11);

        buf.writeBytes("hello world".getBytes());
        System.out.println(buf.toString(Charset.defaultCharset()));

        ByteBuffer nioBuffer = buf.nioBuffer();
        System.out.println("nioBuffer.array() : " + Arrays.toString(nioBuffer.array()));
        System.out.println("nioBuffer.arrayOffset() : " + nioBuffer.arrayOffset());
        System.out.println("nioBuffer.remaining() : " + nioBuffer.remaining());

        //반대로 NioBuffer -> netty Buffer 로 변환
        ByteBuf byteBuf = Unpooled.wrappedBuffer(nioBuffer);
    }
}
