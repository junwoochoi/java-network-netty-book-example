package ch6.nio;

import java.nio.ByteBuffer;

public class ByteBufferTest1 {
    public static void main(String[] args) {
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        System.out.println("바이트 버퍼 초깃값 : " + firstBuffer);

        byte[] source = "Hello World".getBytes();
        firstBuffer.put(source);
        System.out.println("11바이트 기록 후 : " + firstBuffer);
    }
}
