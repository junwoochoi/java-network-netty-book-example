# 부트스트랩

## 부트스트랩의 정의
부트 스트랩은 네티로 작성한 네트워크 애플리케이션의 동작 방식과 환경 설정을 도와주는 Helper클래스이다.

## 부트스트랩의 구조

부트 스트랩에 설정 가능한 내용
- 전송 계층(소켓 모드 및 I/O 종류)
- 이벤트 루프(단일 스레드, 다중 스레드)
- 채널 파이프라인 설정
- 소켓 주소와 포트
- 소켓 옵션

네티의 부트스트랩은 서버 애플리케이션을 위한 `ServerBootstrap`과 클라이언트 애플리케이션을 위한 `Bootstrap`클래스로 나뉜다.
여기서 말하는 서버, 클라이언트의 구분 기준은 소켓 연결을 요청하느냐 아니면 대기하느냐에 따른 구분이다.
`ServerBootstrap`에는 클라이언트 접속을 대기할 포트를 설정하는 메소드가 추가되었을 뿐 `Bootstrap`과 API 구조가 같다.
부트스트랩은 빌더 패턴을 사용해서 복잡한 생성자 구조를 고민할 필요가 없이 더 가독성 좋은 설정이 가능하도록 하였다.
앞에서 작성하였던 Blocking Socket Server / Non Blocking Socket Server 에서는 연결된 소켓 채널에 대한 입출력 처리를 개발자가 직접 처리했다. 
즉, 블로킹 소켓 -> 논블로킹 소켓으로 전환시 아주 많은 코드를 변경해야한다는 뜻이다.
 하지만 네티는 부트스트랩의 설정만을 통해 데이터 처리 코드는 변경하지않고, 동일한 동작을 하는 애플리케이션 작성이 가능하다.
 
## ServerBootstrap
보통 ServerBootstrap은 애플리케이션이 시작할 때 초기화된다.

```java
EventLoopGroup bossGroup = new NioEventLoopGroup(1);
EventLoopGroup workerGroup = new NioEventLoopGroup();

ServerBootstrap b = new ServerBootstrap();
b.group(bossGroup, workerGroup)
    .channel(NioServerSocketChannle.class)
    .childHandler(new ChannelInitializaer<SocketChannel>(){
    @Override
    public void initChannel(SocketChannel ch){
        ChannelPipeline p = ch.pipeline();
        p.addLast(new EchoServerHandler()); 
    }
});

```

위에 말한바와 같이 `ServerBootstrap`은 빌더 패턴을 사용하므로, 매개변수 없이 기본 생성자로 생성한 후, 각 인자를 적절히 셋팅해준다.

`b.group(bossGroup, workerGroup)`에서 첫번째 스레드 그룹은 클라이언트의 연결을 수락하는 부모 스레드 그룹이며 두번째 스레드 그룹은 연결된 클라이ㅓㄴ트의 소켓으로부터 데이터 입출력 및 이벤트 처리를 담당하는 자식 스레드 그룹이다.
여기서 `NioEventLoopGroup`의 생성자의 인수로 사용된 숫자는 스레드 그룹내에서 생성할 최대 스레드 수를 의미한다. 생성자의 인수로 설정하지 않으면 기본 값은 *CPU 코어 수의 2배*를 사용한다.

위 예제에서는 NIO방식으로 동작하던 소켓 서버를 만약 블로킹 모드로 변경하고 싶다면 아래와 같이 세줄만 코드를 바꾸면 된다.

```java
EventLoopGroup bossGroup = new OioEventLoopGroup(1);
EventLoopGroup workerGroup = new OioEventLoopGroup();

ServerBootstrap b = new ServerBootstrap();
b.group(bossGroup, workerGroup)
    .channel(OioServerSocketChannel.class)
    .childHandler(new ChannelInitializaer<SocketChannel>(){
    @Override
    public void initChannel(SocketChannel ch){
        ChannelPipeline p = ch.pipeline();
        p.addLast(new EchoServerHandler()); 
    }
});
```

이와 같이 부트스트랩이 우아한 추상화 모델을 제공하기 때문에 네트워크 애플리케이션 개발자가 확장성과 유연성을 동시에 만족하는 코드를 작성할 수 있다.
만약 Epoll방식으로 변경하고 싶다고 한다면 방금 변경했던 코드 부분만 또 다시 변경하면 된다.
```java
EventLoopGroup bossGroup = new EpollEventLoopGroup(1);
EventLoopGroup workerGroup = new EpollEventLoopGroup();
    ...
.channel(EpollServerSocketChannel.class)

```

하지만 Epoll방식은 리눅스에서만 작동하므로 우리의 컴퓨터가 윈도우라면 작동하지 않는다.


## ServerBootstrap API

### group - 이벤트 루프 설정

`AbstractBootstrap` 클래스 내의 `group`메소드에서드는 하나의 이벤트 루프로 모든 걸 사용하게 되어있고, `ServerBootstrap`에서는 인자가 하나만 들어오면 해당 인자로 bossGroup과 workerGroup을 하나로 설정한다.
만일 두개의 group이 들어오면 각각 클라이언트의 연결을 수락하는 부모 스레드 그룹과 연결된 클라이언트 소켓에 대한 데이터 입출력을 처리하는 자식 스레드 그룹으로 각각 설정하게 되어있다.

### channel - 소켓 입출력 모드 설정

channel API는 부트스트랩을 통해서 생성된 채널의 입출력 모드를 설정할 수 있다.
여기에 설정 가능한 클래스 목록과 설명은 아래와 같다.

- `LocalServerChannel.class`
    - 하나의 자바 가상머신에서 가상 통신을 위한 서버 소켓 채널을 생성하는 클래스
_ `OioServerChannel.class`
    - 블로킹 모드의 서버 소켓 채널을 생성하는 클래스
- `NioServerSocketChannel.class`
    - 논블로킹 모드의 서버 소켓 채널을 생성하는 클래스
- `EpollServerSocketChannel.class`
    - 논블로킹 모드의 서버 소켓 채널을 생성하는 클래스
- `OioSctpServerChannel.class`
    - SCTP 전송 계층을 사용하는 블로킹 모드의 서버 소켓 채널을 생성하는 클래스
- `NioSctpServerChannel.class`
    - SCTP 전송 계층을 사용하는 논블로킹 모드의 서버 소켓 채널을 생성하는 클래스
- `NioUdtByteAcceptorChannel.class`
    - UDT 프로토콜을 지원하는 논블로킹 모드의 서버 소켓 채널을 생성하는 클래스
- `NioUdtMessageAcceptorChannel.class`
    - UDT 프로토콜을 지원하는 블로킹 모드의 서버 소켓 채널을 생성하는 클래스.

### channelFactory - 소켓 입출력 모드 설정

channelFactory메소드는 channel 메서드와 동일한 기능을 수행한다. 네티가 제공하는 ChannelFactory 인터페이스의 구현체에는 `NioUdtProvider`가 있다.

### handler - 서버 소켓 채널의 이벤트 핸들러 설정

서버 소켓 채널의 이벤트를 처리할 핸들러를 설정하는 API이다. 이 메서드를 통해서 등록되는 이벤트 핸들러는 서버 소켓 채널에서 발생하는 이벤트를 수신하여 처리한다.

```java
.handler(new LoggingHandler(LogLevel.INFO))
```

LoggingHandler는 네티에서 기본으로 제공하는 코덱이다. LoggingHandler는 네티가 제공하는 ChannelDuplexHandler를 상속받고 있다. 이 코드를 따라가보면 ChannelInboundHandler와 ChannelOutboudHandler를 상속받아 구현하고 있다.
즉 채널에서 발생하는 양방향 이벤트 모두를 로그로 출력하도록 구현이 되어있지만, 실제로는 그렇지 않다. ServerBootstrap의 handler메서드에 등록된 이벤트 핸들러는 서버 소켓 채널에서 발생한 이벤트만을 처리하기 때문이다.

### childHandler - 소켓 채널의 데이터 가공 핸들러 설정 

handler메서드와 childHandler메서드는 ChannelHandler 인터페이스의 구현체를 인수로 입력할 수 있다. 

```java
...
.childHandler(new ChannelInitializaer<SocketChannel>(){
    @Override
    public void initChannel(SocketChannel ch){
        ChannelPipeline p = ch.pipeline();
        p.addLast(new LoggingHandler(LogLevel.INFO));
        p.addLast(new EchoServerHandler()); 
    }
});
```

위 예제는 handler메서드에 로그 핸들러를 등록했던 코드에서 childHandler에서 등록하도록 변경한 코드이다. 즉, LoggingHandler를 클라이언트 소켓 채널의 파이프라인에 등록한 것이다.
여기서 childHandler 메서드는 서버 소켓 채널로 연결된 클라이언트 채널에 파이프라인을 설정하는 역할을 수행한다.


### option - 서버 소켓 채널의 소켓 옵션 설정

서버 소켓 채널의 소켓 옵션을 설정하는 API이다. 소켓 옵션이란 소켓의 동작 방식을 지정하는 것을 말한다. 예를 들어 SO_SNDBUF 옵션은 소켓이 사용할 송신 버퍼의 크기를 지정한다. 여기서 혼동하지 말아야할 부분이 소켓 옵션은 애플리케이션의 값을 바꾸는 것이 아니라 *커널*에서 사용되는 값을 변경한다는 의미다.

#### 네티의 부트스트랩을 통해서 설정할 수 있는 소켓 옵션
- `TCP_NODELAY`
    - 데이터 송수신에 Nagle 알고리즘의 비활성화 여부를 지정한다. [네이글 알고리즘이란](https://ozt88.tistory.com/18)
    - default : FALSE
- `SO_KEEPALIVE`
    - 운영체제에서 지정된 시간에 한번씩 keepalive 패킷을 상대방에게 전송한다.
    - default : FALSE
- `SO_SNDBUF`
    - 상대방으로 송신할 커널 송신 버퍼의 크기
- `SO_RCVBUF`
    - 상대방으로부터 수신할 커널 수신 버퍼의 크기
- `SO_REUSEADDR`
    - TIME_WAIT상태의 포트를 서버 소켓에 바인드할 수 있게 한다.
    - default : FALSE
- `SO_LINGER`
    - 소켓을 닫을 때 커널의 송신 버퍼에 전송되지 않은 데이터의 전송 대기시간을 지정한다.
    - default : FALSE
- `SO_BACKLOG`
    - 동시에 수용 가능한 소켓 연결 요청 수
    
    
### childOption - 소켓 채널의 소켓 옵션 설정 
childOption 메서드는 앞에서 살펴본 option 메서드와 같이 소켓 채널에 소켓옵션을 설정한다. option 메서드는 서버 소켓 채널의 옵션을 설정하는데 반해 childOption 메서드는 서버에 접속한 클라이언트 소켓 채널에 대한 옵션을 설정하는 데 사용한다.

클라이언트 소켓 채널의 대표적 옵션으로는 `SO_LINGER`가 있다. `SO_LINGER`는 `SO_REUSEADDR`옵션과 같이 소켓 종료와 관련이 있다. 소켓에 대하여 close메서드를 호출한 이후 커널 버퍼에 아직 전송되지않은 데이터가 남아 있으면 어떻게 처리할 지 지정하는 옵션이다.
포트 상태가 TIME_WAIT로 전환되는 것을 방지하기 위해 해당 옵션을 활성화하고 타임아웃을 0 으로 설정하는 편법이 서버 애플리케이션에서 많이 사용된다.
이 방법은 TIME_WAIT이 발생하지 않는 장점이 있는 반면에, 마지막으로 전송한 데이터가 클라이언트로 모두 전송되었는지 확인할 방법이 없다.

## Bootstrap API
기본적으로는 ServerBootstrap과 같다. 단, 클라이언트에서 사용하는 단일 소켓 채널에 대한 설정이므로 부모, 자식 관계가 없다. Bootstrap API는 ServerBootstrap 과의 차이점 위주로 설명한다.

### group - 이벤트 루프 설정
ServerBootstrap에서와는 달리 단 하나의 이벤트 루프만 설정할 수 있다.

### channel - 소켓 입출력 모드 설정
ServerBootstrap과 달리 *클라이언트 소켓 채널만* 설정이 가능하다.

- `LocalChannel.class`
    - 하나의 자바 가상머신에서 가상 통신을 위한 서버 소켓 채널을 생성하는 클래스
_ `OioChannel.class`
    - 블로킹 모드의 클라이언트 소켓 채널을 생성하는 클래스
- `NioSocketChannel.class`
    - 논블로킹 모드의 클라이언트 소켓 채널을 생성하는 클래스
- `EpollSocketChannel.class`
    - 논블로킹 모드의 클라이언트 소켓 채널을 생성하는 클래스
- `OioSctpChannel.class`
    - SCTP 전송 계층을 사용하는 블로킹 모드의 클라이언트 소켓 채널을 생성하는 클래스
- `NioSctpChannel.class`
    - SCTP 전송 계층을 사용하는 논블로킹 모드의 클라이언트 소켓 채널을 생성하는 클래스

### channelFactory - 소켓 입출력 모드 설정
ServerBootstrap과 동일

### handler - 클라이언트 소켓 채널의 이벤트 핸들러 설정
클라이언트 소켓 채널에서 발생하는 이벤트를 수신하여 처리.

### option - 소켓 채널의 소켓 옵션 설정
ServerBootstrap의 option 메서드가 서버 소켓 채널의 옵션을 설정한 반면 Bootstrap의 option메서드는 서버와 연결된 클라이언트 소켓 채널의 옵션을 설정.
