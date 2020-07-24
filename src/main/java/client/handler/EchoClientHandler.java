package client.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String sendMessage = "Hello, Netty!";

        ByteBuf messageBuffer = Unpooled.buffer();
        messageBuffer.writeBytes(sendMessage.getBytes());

        StringBuilder sb = new StringBuilder();
        sb.append("전송한 문자열 [").append(sendMessage).append("]");

        System.out.println(sb.toString());

        /*
         writeAndFlush() 메서드는 내부적으로 데이터 기록과 전송의 두가지 메서드 호출.
         write() -> flush() 순으로 작동
         */
        ctx.writeAndFlush(messageBuffer);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception { //서버로부터 데이터 받았을 시 실행
        String readMessage = ((ByteBuf) msg).toString(Charset.defaultCharset());

        StringBuilder sb = new StringBuilder();
        sb.append("수신한 문자열 [").append(readMessage).append("]");

        System.out.println(sb.toString());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception { // 서버로부터 수신된 데이터를 모두 읽었을 때 수행
        ctx.close(); // 다 수신완료 후 서버와 연결된 채널을 닫는다 이후 데이터 송수신 채널은 닫히게 되고 클라이언트 프로그램은 종료된다.
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
