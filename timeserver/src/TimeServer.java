import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/*
 * 使用NIO进行服务端开发步骤：
 * 1.创建ServerSocketChannel，配置非阻塞模式
 * 2.绑定监听，配置TCP参数，比如backLog大小
 * 3.创建独立的IO线程，用于轮询多路复用器Selector
 * 4.创建Selector，将之间创建的ServerSocketChannel注册到Selector上，监听SelectionKey.ACCEPT
 * 5.启动io线程，在循环体中执行Selector.select()方法，轮询就绪的channel
 * 6.当轮询到就绪channel时，判断，如果时OP_ACCEPT状态说明时新客户端接入，则调用ServerSocketChannel.accept()接受新客户端
 * 7.设置新接入的客户端链路SocketChannel为非阻塞模式，配置其他的TCP参数
 * 8.将SocketChannel注册到Selector，监听OP_READ操作位
 * 9.如果轮询到的Channel为OP_READ，说明SocketChannel中有新就绪的数据包需要读取，则构造ByteBuffer对象读取数据包
 * 10.如果轮询的Channel为OP_WRITE，说明有数据没发送完成，需要继续发送
 */

public class TimeServer {
    public void bind(int port){
        
        EventLoopGroup bossGroup =new NioEventLoopGroup();
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        
        ServerBootstrap serverBootstrap=new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG,1024);

    }



    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
    }
}
