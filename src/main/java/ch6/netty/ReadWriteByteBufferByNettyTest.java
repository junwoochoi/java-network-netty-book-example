package ch6.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

public class ReadWriteByteBufferByNettyTest {
    public static void main(String[] args) {
        //바이트 버퍼 풀을 사용하지 않는 힙 버퍼 생성
        ByteBuf unpooledHeapBuffer = Unpooled.buffer(11);

        //바이트 버퍼 풀을 사용하지 않는 다이렉트 버퍼 생성
        ByteBuf unpooledDirectBuffer = Unpooled.directBuffer(11);

        //풀링된 11바이트 크기의 힙 버퍼를 생성
        ByteBuf pooledHeapBuffer = PooledByteBufAllocator.DEFAULT.buffer(11);

        //풀링된 다이렉트 버퍼 생성
        ByteBuf pooledDirectBuffer = PooledByteBufAllocator.DEFAULT.directBuffer(11);

        System.out.println("capacity : " + pooledHeapBuffer.capacity());

        pooledHeapBuffer.writeInt(65537);

        System.out.println("readableBytes : " + pooledHeapBuffer.readableBytes());
        System.out.println("writableBytes : " + pooledHeapBuffer.writableBytes());

        System.out.println("readShort : " + pooledHeapBuffer.readShort());
        System.out.println("readableBytes : " + pooledHeapBuffer.readableBytes());
        System.out.println("writableBytes : " + pooledHeapBuffer.writableBytes());
        System.out.println("isReadable : " + pooledHeapBuffer.isReadable());

        pooledHeapBuffer.clear(); // 남은 데이터 버리고, 읽기 인덱스와 쓰기 인덱스 0으로 초기화

        System.out.println("---- buf clear ---- ");

        System.out.println("readableBytes : " + pooledHeapBuffer.readableBytes());
        System.out.println("writableBytes : " + pooledHeapBuffer.writableBytes());
    }
}
