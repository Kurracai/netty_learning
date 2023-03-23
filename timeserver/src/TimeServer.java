import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;



public class TimeServer {
    public void bind(int port){
        //NioEventLoopGroup是一个线程组，包含一组NIO线程，处理网络事件，实际上是Reactor线程组。
        //一个用于服务端接收客户端连接，一个用户SocketChannel网络读写
        EventLoopGroup bossGroup =new NioEventLoopGroup();
        EventLoopGroup workerGroup=new NioEventLoopGroup();
        try {
        //ServerBootStrap是Netty用于启动NIO服务端的辅助启动类，目的是降低服务端开发复杂度
        ServerBootstrap serverBootstrap=new ServerBootstrap();
        //调用group方法，将两个NIO线程组当作入参传递到ServerBootStrap中。
        //创建的NioServerSocketChannel，功能对应与JDK NIO类库中ServerSocketChannel类
        //再配置NioServerSocketChannel的TCP参数。最后绑定IO时间处理类ChildChannelHandler，作用类似Reactor模式中的Handler类
        //用于处理网络IO事件，比如日志记录、对消息进行编解码

        //服务端启动辅助类配置完成后，调用bind方法绑定监听端口，随后调用sync等待绑定操作完成。完成以后Netty会返回一个ChannelFuture，功能类似java.util.concurrent.Future
        //用于异步操作的通知回调
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
        .option(ChannelOption.SO_BACKLOG,1024).childHandler(new ChildChannelHandler());
            ChannelFuture future=serverBootstrap.bind(port).sync();
            //使用sync()进行阻塞，等待服务端链路关闭后main函数才退出
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            e.printStackTrace();
        }
    }

    private class ChildChannelHandler extends ChannelInitializer<SocketChannel>{

        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ch.pipeline().addLast(new TimeServerHandler());
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        int port=8080;
        new TimeServer().bind(port);
    }
}
