package ch1.client;

import ch1.client.handler.EchoClientHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class EchoClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(group)  //서버와 다르게 이벤트 루프 그룹이 하나만 설정
                    .channel(NioSocketChannel.class)  // 클라이언트 애플리케이션이 생성하는 채널의 종류를 설정
                    .handler(new ChannelInitializer<SocketChannel>() { // 클라이언트 애플리케이션이므로 채널 파이프라인의 일반 소켓 채널 클래스인 SocketChannel 등록
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new EchoClientHandler());
                        }
                    });

            /*
             비동기 입출력 메서드인 connect 호출.
             connect의 응답값인 ChannelFuture를 통하여 비동기 메서드의 처리 결과 확인 가능.
             ChannelFuture의 .sync 메서드는 객체의 요청이 완료될때까지 대기한다. 단, 요청이 실패하면 예외를 던진다. 즉 connect메서드의 처리가 완료될때까지는 다음 라인으로 넘어가지 않는다
             */
            ChannelFuture f = b.connect("localhost", 8888).sync();
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
