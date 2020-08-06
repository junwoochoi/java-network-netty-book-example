package ch6.nio;

import java.nio.ByteBuffer;

public class ByteBufferTest3 {
    public static void main(String[] args) {
        ByteBuffer firstBuffer = ByteBuffer.allocate(11);
        System.out.println("초기 상태 : " + firstBuffer);

        firstBuffer.put((byte) 1);
        firstBuffer.put((byte) 2);
        System.out.println("firstBuffer.position() : " + firstBuffer.position());

        firstBuffer.rewind();
        System.out.println("position after firstBuffer.rewind() : " + firstBuffer.position());
        System.out.println("firstBuffer.get() : " + firstBuffer.get());
        System.out.println("firstBuffer.position() : " + firstBuffer.position());

        System.out.println(firstBuffer);

        /**
         * 초기 상태 : java.nio.HeapByteBuffer[pos=0 lim=11 cap=11]
         * firstBuffer.position() : 2
         * position after firstBuffer.rewind() : 0
         * firstBuffer.get() : 1
         * firstBuffer.position() : 1
         * java.nio.HeapByteBuffer[pos=1 lim=11 cap=11]
         */
    }
}
