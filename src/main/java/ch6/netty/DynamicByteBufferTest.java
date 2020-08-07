package ch6.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;

public class DynamicByteBufferTest {
    public static void main(String[] args) {
        ByteBuf buffer = Unpooled.buffer(11);
        System.out.println("capacity : " + buffer.capacity());

        String sourceData = "hello world";

        buffer.writeBytes(sourceData.getBytes());
        System.out.println("readableBytes : " + buffer.readableBytes());
        System.out.println("writableBytes : " + buffer.writableBytes());

        System.out.println(buffer.toString(Charset.defaultCharset()));

        buffer.capacity(6);

        System.out.println("buffer.toString() : " + buffer.toString(Charset.defaultCharset()));

        buffer.capacity(13);
        System.out.println("buffer.toString() : " + buffer.toString(Charset.defaultCharset()));

        buffer.writeBytes("world".getBytes());
        System.out.println(buffer.toString(Charset.defaultCharset()));

        System.out.println("capacity : " + buffer.capacity());
        System.out.println("writableByte : " + buffer.writableBytes());
    }
}
