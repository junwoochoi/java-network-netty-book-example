package ch6.nio;

import java.nio.ByteBuffer;

public class WriteByteBufferTest {
    public static void main(String[] args) {
        ByteBuffer direct = ByteBuffer.allocateDirect(11);
        System.out.println("position : "+ direct.position());
        System.out.println("limit : "+ direct.limit());

        direct.put((byte) 1);
        direct.put((byte) 2);
        direct.put((byte) 3);
        direct.put((byte) 4);
        System.out.println("position : "+ direct.position());
        System.out.println("limit : "+ direct.limit());

        direct.flip();
        System.out.println("-------- flip -----------");
        System.out.println("position : "+ direct.position());
        System.out.println("limit : "+ direct.limit());
    }
}
