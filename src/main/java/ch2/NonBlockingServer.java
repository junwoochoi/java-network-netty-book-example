package ch2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class NonBlockingServer {
    private Map<SocketChannel, List<byte[]>> keepDataTrack = new HashMap<>();
    private ByteBuffer buffer = ByteBuffer.allocate(2 * 1024);

    public static void main(String[] args) throws IOException {
        NonBlockingServer nonBlockingServer = new NonBlockingServer();
        nonBlockingServer.startEchoServer();
    }

    private void startEchoServer() throws IOException {
        // try 블럭 내부에서 변수 선언함으로써 자원이 이후 정상적으로 해제되도록 작성
        try (
                // NIO 컴포넌트인 selector는 자신에게 등록된 채널에 변경사항이 발생했는지 검사, 변경 사항 발생시 채널에 대한 접근을 가능하게 해줌
                Selector selector = Selector.open();
                //논블로킹 소켓의 서버 소켓 채널 생성, 블로킹과 다르게 채널을 먼저 생상ㅊㅇ하고, 이후 사용할 포트를 바인딩
                ServerSocketChannel socketChannel = ServerSocketChannel.open();
        ) {
            if ((socketChannel.isOpen()) && (selector.isOpen())) { // 생성한 소켓채널 및 Selector가 정상적으로 생성되었는지 확인
                socketChannel.configureBlocking(false); // 소켓 채널의 기본 블로킹 모드는 true 이기 때문에 별도로 논블로킹 모드로 지정 필요
                socketChannel.bind(new InetSocketAddress(8888));

                // ServerSocketChannel 객체를 Selector 객체에 등록. Selector가 감지할 이벤트는 연결 요청에 해당하는 OP_ACCEPT
                socketChannel.register(selector, SelectionKey.OP_ACCEPT);
                System.out.println("접속 대기중...");

                while (true) {
                    /*
                        Selector에 등록된 채널에서 변경사항이 발생했는 지 검사. 아무런 I/O 이벤트가 발생하지 않으면 스레드는 이 부분에서 블로킹
                        I/O 이벤트가 발생하지 않았을 때 블로킹을 피하고 싶다면 selectNow 메서드를 사용
                     */
                    selector.select();
                    Iterator<SelectionKey> keys = selector.selectedKeys().iterator(); // I/O 이벤트가 발생한 채널의 목록을 조회

                    while (keys.hasNext()) {
                        SelectionKey key = keys.next();
                        keys.remove(); // IO 이벤트 발생 채널에서 동일한 이벤트가 감지되는 것을 방지하기 위하여 조회된 목록에서 제거

                        if (!key.isValid()) {
                            continue;
                        }

                        // 각 이벤트의 종류에 따라 적절한 메서드로 위임.
                        if (key.isAcceptable()) {
                            this.acceptOP(key, selector);
                        } else if (key.isReadable()) {
                            this.readOP(key);
                        } else if (key.isWritable()) {
                            this.writeOP(key);
                        }
                    }
                }
            } else {
                System.out.println("서버 소켓을 생성하지 못했습니다");
            }

        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void acceptOP(SelectionKey key, Selector selector) throws IOException {
        //연결 요청 이벤트가 발생한 채널은 항상 ServerSocketChannel이므로 이벤트가 발생한 채널을 캐스팅한다.
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        //클라이언트 연결을 수락하고 연결된 소켓 채널을 가져온다
        SocketChannel socketChannel = serverChannel.accept();
        //연결된 클라이언트 소켓 채널을 논블로킹 모드로 설정한다.
        socketChannel.configureBlocking(false);

        System.out.println("클라이언트 연결됨 : " + socketChannel.getRemoteAddress());

        //클라이언트 소켓 채널을 Selector에 등록하여 IO이벤트를 감시한다
        keepDataTrack.put(socketChannel, new ArrayList<>());
        socketChannel.register(selector, SelectionKey.OP_READ);
    }

    private void writeOP(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        Iterator<byte[]> its = channelData.iterator();

        while (its.hasNext()) {
            byte[] it = its.next();
            its.remove();
            socketChannel.write(ByteBuffer.wrap(it));
        }

        key.interestOps(SelectionKey.OP_READ);
    }

    private void readOP(SelectionKey key) throws IOException {
        try {
            SocketChannel socketChannel = (SocketChannel) key.channel();
            buffer.clear();
            int numRead = -1;
            try {
                numRead = socketChannel.read(buffer);
            } catch (IOException e) {
                System.err.println("데이터 읽기 에러!");
            }

            if (numRead == -1) {
                this.keepDataTrack.remove(socketChannel);
                System.out.println("클라이언트 연결 종료 : " + socketChannel.getRemoteAddress());
                socketChannel.close();
                key.cancel();
                return;
            }
            byte[] data = new byte[numRead];
            System.arraycopy(buffer.array(), 0, data, 0, numRead);
            System.out.println(new String(data, StandardCharsets.UTF_8) + "from " + socketChannel.getRemoteAddress());

            doEchoJob(key, data);
        } catch (IOException e) {
            System.err.println(e);
        }
    }

    private void doEchoJob(SelectionKey key, byte[] data) {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        List<byte[]> channelData = keepDataTrack.get(socketChannel);
        channelData.add(data);

        key.interestOps(SelectionKey.OP_WRITE);
    }

}
