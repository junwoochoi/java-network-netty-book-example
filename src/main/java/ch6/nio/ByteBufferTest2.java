package ch6.nio;

import java.nio.ByteBuffer;

public class ByteBufferTest2 {
    public static void main(String[] args) {
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        System.out.println("바이트 버퍼 초깃값 : " + firstBuffer);

        byte[] source = "Hello World!".getBytes();
        for (byte item : source) {
            firstBuffer.put(item);
            System.out.println("현재 상태 : " + firstBuffer);
        }

        //마지막은 11자리를 넘어가기 때문에 BufferOverflowException 발생
    }
}
