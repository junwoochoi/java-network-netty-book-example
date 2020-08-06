package ch6.nio;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class CreateByteBufferTest {
    public static void main(String[] args) {
        ByteBuffer heapBuffer = ByteBuffer.allocate(11); //11바이트를 저장할 수 있는 초기화된 바이트 버퍼
        System.out.println("heapBuffer capacity : " + heapBuffer.capacity());
        System.out.println("heapBuffer position : " + heapBuffer.position());
        System.out.println("heapBuffer isDirect : " + heapBuffer.isDirect());

        ByteBuffer directBuffer = ByteBuffer.allocateDirect(11); //11바이트를 저장할 수 있는 초기화된 다이렉트 버퍼
        System.out.println("directBuffer capacity : " + directBuffer.capacity());
        System.out.println("directBuffer position : " + directBuffer.position());
        System.out.println("directBuffer isDirect : " + directBuffer.isDirect());

        int[] array = {1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 0};
        IntBuffer intHeapBuffer = IntBuffer.wrap(array); // array 배열을 감싸는 int형 버퍼 생성
        System.out.println("intHeapBuffer capacity : " + intHeapBuffer.capacity());
        System.out.println("intHeapBuffer position : " + intHeapBuffer.position());
        System.out.println("intHeapBuffer isDirect : " + intHeapBuffer.isDirect());
    }
}
