package server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;

public class EchoServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset()); //수신된 데이터를 가지고 네티의 바이트 버퍼 객체로부터 문자열 데이터 읽기

        System.out.println("수신한 문자열 : [" + readMessage + "]");
        ctx.write(msg);//ctx는 ChannelHandlerContext 의 인터페이스로 채널 파이프라인에 대한 이벤트를 처리
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush(); // channelRead 이벤트의 처리가 완료된 후 자동으로 수행되는 이벤트 메서드, 채널 파이프라인에 저장된 버퍼를 전송하는 flush 메소드를 호출.
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
