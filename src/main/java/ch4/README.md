# 채널 파이프라인과 코덱

채널 파이프라인은 채널에서 발생한 이벤트가 이동하는 통로. 이 통로를 통해 이동하는 이벤트를 처리하는 클래스를 이벤트 핸들러라고 한다. 이러한 이벤트 핸들러를 상속받아 구현한 구현체들을 *코덱*이라고 한다.
자주 사용하는 이벤트 핸들러를 미리 구현해둔 코덱 묶음은 `io.netty.handler.codec`패키지에 있다.

## 이벤트 실행
네티는 이벤트를 채널 파이프라인과 이벤트 핸들러로 추상화한다. 따라서 네티를 사용하면 데이터가 수신되었는지 소켓의 연결이 끊어졌는 지 같은 예외 상태에서 메소드 호출에 관여할 필요가 없다.

1. 부트스트랩으로 네트워크 애플리케이션에 필요한 설정을 지정한다.
2. 부트스트랩에 이벤트 핸들러를 사용하여 채널 파이프라인을 구성한다.
3. 이벤트 핸들러의 데이터 수신 이벤트 메서드에서 데이터를 읽어들인다.
4. 이벤트 핸들러의 네트워크 끊김 이벤트 메서드에서 에러 처리를 한다.

위와 같이 구현하면 네티의 이벤트 루프가 소켓 채널에서 발생한 이벤트에 해당하는 핸들링 메서드를 자동으로 실행한다. 소켓 채널에 데이터가 수신되었을 때 이벤트 메서드를 실행하는 방법은 다음과 같다.

1. 네티의 이벤트 루프가 채널 파이프라인에 등록된 첫 번째 이벤트 핸들러를 가져온다.
2. 이벤트 핸들러에 데이터 수신 이벤트 메서드가 구현되어있으면 실행한다.
3. 데이터 수신 이벤트 메서드가 구현되어 있지 않으면 다음 이벤트 핸들러를 가져온다.
4. 2를 수행한다.
5. 마지막 이벤트 핸들러에 도달할때까지 1~2를 반복한다.

이러한 방식의 장점으로 네티의 이벤트 모델을 따르면 프로그래머가 구현해야할 코드의 구분과 위치가 명확해지고, 더 적은 코드로 튼튼한 애플리케이션을 구현할 수 있다.

## 채널 파이프라인
채널 파이프라인은 네티의 채널과 이벤트 핸들러 사이에서 연결 통로 역할을 수행한다.

### 채널 파이프라인의 구조

채널은 소켓과 같다고 보면 되는데, 소켓에서 발생한 이벤트는 채널 파이프라인을 따라 흐른다. 
채널에서 발생한 이벤트들을 수신하고 처리하는 기능은 이벤트 핸들러에서 수행한다.
하나의 채널 파이프라인에 여러 이벤트 핸들러를 등록할 수 있다. 
이와 같이 네티는 이벤트 처리를 위한 추상화 모델로서 채널 파이프라인을 사용한다.

### 채널 파이프라인의 동작
```java
public class EchoServer {
    public static void main(String[] args) throws Exception{
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new EchoServerHandler()); // 접속된 클라이언트로부터 수신된 데이터를 처리할 핸들러를 지정
                        }
                    });

            ChannelFuture f = b.bind(8888).sync(); // 부트스트랩 클래스의 bind메서드로 접속할 포트 지정

            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
```

위 예제에서 가장 중요한 부분은 childHandler 메서드를 사용하여 채널 파이프라인에 이벤트 핸들러를 등록한 부분이다. 
네티는 소켓 채널에 채널 파이프라인을 등록하고 이벤트 핸들러의 설정을 완료하기 위해서 세단계의 포르세스를 거친다.

1. 클라이언트 연결에 대응하는 소켓 채널 객체를 생성, 빈 채널 파이프라인 객체를 생성하여 소켓 채널에 할당
2. 소켓 채널에 등록된 ChannelInitializer 인터페이스의 구현체를 가져와 initChannel 메서드 실행
3. 소켓 채널 참조로부터 1에서 등록한 파이프라인 객체를 가져오고 채널 파이프라인에 입력된 이벤트 핸들러의 객체를 등록

위 과정이 종료괴면 채널이 등록됐다는 이벤트가 발생하고, 이때부터는 데이터 송수신을 위한 이벤트 처리가 시작된다.

## 이벤트 핸들러

네티는 비동기 호출을 지원하는 Future패턴과 리액터 패턴을 구현한 이벤트 핸들러 ,두가지 패턴을 제공한다. 
이벤트 핸들러는 네티의 소켓 채널에서 발생한 이벤트를 처리하는 인터페이스로, 소켓채널의 이벤트를 인터페이스로 정의하고 이 인터페이스를 상속받은 이벤트 핸들러를 작성하여 채널 파이프라인에 등록한다.
채널 파이프라인으로 입력되는 이벤트를 이벤트 루프가 가로채어 이벤트에 해당하는 핸들링 메서드를 수행하는 구조로 되어있다. 이를 위해 이벤트 핸들러가 제공하는 이벤트의 목록과 발생조건에 대해 아는 것이 필요하다.

### 채널 인바운드 이벤트
네티의 소켓 채널에서 발생하는 이벤트는 인바운드 이벤트와 아웃바운드 이벤트로 추상화된다.
인바운드 이벤트는 소켓 채널에서 발생한 이벤트 중에서 연결 상대방이 어떤 동작을 취했을 때 발생한다. 서버의 관점에서는 데이터가 수신되는데, 이 떄 네티는 소켓채널에서 읽은 데이터가 있다는 이벤트를 채널 파이프라인으로 흘려보내고 채널 파이프라인에 등록된 인바운드 이벤트 핸들러가 해당 이벤트에 해당하는 메소드를 실행한다.

네티의 기본제공 하는 ChannelInboundHandler 인터페이스에서 제공하는 주요 이벤트들은 아래와 같다.

1. `channelRegistered`
    - 서버와 클라이언트 상관없이 새로운 채널이 생성되는 시점에 발생
2. `channelActive`
    - 채널이 생성되고 이벤트 루프에 등록된 이후에 네티 API를 사용하여 채널 입출력을 수행할 상태가 되었음을 알려주는 이벤트
    - 서버 또는 클라이언트가 상대방에 연결한 직후 한번만 수행할 작업을 처리하기에 적합하다.
3. `channelRead`
    - 네티로 작성된 애플리케이션에서 빈도 높게 생성되는 이벤트로서 데이터가 수신되었음을 알려준다.
    - 수신된 데이터는 네티의 `ByteBuf` 객체에 저장되어 있으며 msg를 통해서 접근이 가능하다.
    - 네티 내부에서는 모든 데이터가 `ByteBuf`로 관리된다.
4. `channelReadComplete`
    - 데이터 수신이 완료되었음을 알려준다.
    - 채널의 데이터를 다 읽어서 더 이상 데이터가 없을 때 발생하는 이벤트이다.
5. `channelInactive`
    - 채널이 비활성화되었을 때 발생한다.
6. `channelUnregistered`
    - 채널이 이벤트 루프에서 제거되었을 때 발생한다.

실제 이벤트의 발생 순서는 위에서 나열한 순서와 동일하다.

### 아웃바운드 이벤트
아웃바운드 이벤트는 네티 사용자(프로그래머)가 요청한 동작에 해당하는 이벤트를 말한다. 연결 요청, 데이터 전송, 소켓 닫기 등이 이에 해당한다.
네티에서는 `ChannelOutboundHandler`인터페이스로 아웃바운드 이벤트를 제공한다.

- `bind`
    - 서버 소켓 채널이 클라이언트의 연결을 대기하는 ip와 포트가 설정되었을 때 발생
- `connect`
    - 클라이언트 소켓 채널이 서버에 연결되었을 때 발생
- `disconnect`
    - 클라이언트 소켓 채널의 연결이 끊어졌을 때 발생
- `close`
    - 클라이언트 소켓 채널의 연결이 닫혔을 때 발생
- `write`
    - 소켓 채널에 데이터가 기록되었을 때 발생
- `flush`
    - 소켓 채널에 대한 flush 메서드가 호출되었을 때 발생
    

### 이벤트 이동 경로와 이벤트 메서드 실행

여러 이벤트 핸들러가 등록되었을 때 어떻게 이벤트 메서드가 실행될까?

```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg){
    Bytebuf readMessage = (ByteBuf) msg;
    System.out.println("First channelRead :" + readMessage.toString(Charset.defaultCharset()));
    ctx.write(msg);
}
```


```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg){
    Bytebuf readMessage = (ByteBuf) msg;
    System.out.println("Second channelRead :" + readMessage.toString(Charset.defaultCharset()));
}
```

위 핸들러를 등록하고 실행하면 생각했던 것과 다르게 FirstHandler의 channelRead메서드만 작동한다. 이벤트에 해당하는 이벤트 메서드가 실행되면서 이벤트가 사라졌기 때문이다.
이를 우리가 생각했던 것처럼 둘다 작동하려면 아래와 같이 수정해야한다.

```java
@Override
public void channelRead(ChannelHandlerContext ctx, Object msg){
    Bytebuf readMessage = (ByteBuf) msg;
    System.out.println("First channelRead :" + readMessage.toString(Charset.defaultCharset()));
    ctx.write(msg);
    ctx.fireChannelRead(msg);
}
```

`ctx.fireChannelRead(msg)`가 다음ㅇ ㅣ벤트 핸들러로 이벤트를 넘겨준다.

### 코덱
네티에서 코덱은 전송할 데이터를 전송 프로토콜에 맞추어 인코딩 디코딩 해주는 역할을 한다.

## 코덱의 구조
코덱은 데이터를 전송할 때는 인코더를 이용하여 패킷으로 변환하고 수신할 때는 디코더를 사용하여 패킷을 우리가 원하는 데이터 형태로 변환해야한다.

### 코덱의 실행 과정
코덱은 템플릿 메서드 패턴으로 구현되어있다. 상위 구현체에서 메서드의 실행순서만을 지정하고 수행될 메서드의 구현은 하위 구현체로 위임한다.
인코더의 메서드 호출 순서는 아래와 같다.

1. ctx.write()
2. write이벤트 발생
3. channelOutBoundHandler write이벤트 발생
4. encode() 실행

## 기본 제공 코덱
네티에서는 자주 사용되는 인코더와 디코더를 기본 제공한다. `io.netty.handler.codec`에 기본 제공하는 코덱들이 위치해있다.

- base64 codec
- bytes codec
- compression codec
- http codec
- marshalling codec
- protobuf codec
- rtsp codec
- sctp codec
- spdy codec
- string codec
- serialization codec

## 사용자 정의 코덱 
사용자 정의 코덱은 사용자가 직접 필요한 프로토콜을 구현하는 것이다. 필요에따라 인바운드와 아웃바운드 핸들러를 구현한다.

### HttpHelloWorldServer 예제 실습
HttpServerCodec 클래스는 인바운드 아웃바운드 이벤트 핸들러를 모두 구현한다. 따라사 이벤트 핸들러의 순서가 중요하다.
만약 예제의 이벤트 핸들러의 순서를 뒤바꾸면 HttpHelloWorldServerHandler가 먼저 이벤트를 받아서 디코딩되지 않은 정보가 들어와서 아무런 작동을 하지 않게된다.
항상 이벤트 핸들러의 순서에 유의하자.


## 마치며
이벤트 핸들러는 네티 추상화 계층 중 가장 개발자와 밀접한 부분이다. 대부분의 비즈니스 로직이 이벤트 핸들러에 구현되기 때문이다. 그러므로 이벤트 핸들러에서 발생하는 이벤트와 발생 순서를 잘 숙지하자.