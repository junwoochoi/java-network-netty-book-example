package ch6.nio;

import java.nio.ByteBuffer;

public class ReadByteBufferTest {
    public static void main(String[] args) {
        byte[] tempArray = {1, 2, 3, 4, 5, 0, 0, 0, 0, 0, 0};
        ByteBuffer firstBuffer = ByteBuffer.wrap(tempArray);

        System.out.println("position : " + firstBuffer.position());
        System.out.println("limit : " + firstBuffer.limit());

        System.out.println(firstBuffer.get());
        System.out.println(firstBuffer.get());
        System.out.println(firstBuffer.get());
        System.out.println(firstBuffer.get());

        System.out.println("position : " + firstBuffer.position());
        System.out.println("limit : " + firstBuffer.limit());

        firstBuffer.flip();
        System.out.println("position : " + firstBuffer.position());
        System.out.println("limit : " + firstBuffer.limit());

        firstBuffer.get(3);

        System.out.println("position : " + firstBuffer.position());
    }
}
