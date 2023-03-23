import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/*
* 打开ServerSocketChannel，用于监听客户端连接，作为所有客户端连接的父管道
* 绑定监听端口，设置为非阻塞模式
* 创建Reactor线程，创建多路复用器并启动线程
* 将ServerSocketChannel注册到Reactor线程的多路复用器Selector上，监听Accept事件
* Selector在run方法的循环体内轮询准备就绪的key
* Selector监听到客户端接入，处理新请求，完成TCP三次握手，建立物理链路
* 设置客户端链路为非阻塞模式
* 将新接入的客户端连接注册到Reactor线程的多路复用器上，监听读操作，读取客户端发送改动网络信息
* 异步读取客户端请求到缓冲区
* 对ByteBuffer做编解码，如果有半包消息指针reset，继续读取后续保温，将解码成功的消息封装成Task，投递到业务线程池中，进行业务逻辑编排。
* 将POJO对象encode成ByteBuffer，调用SocketChannel的异步write接口将消息异步发送给客户端
 */
public class MulTimeServer implements Runnable{
    private Selector selector;
    //多路复用器
    private ServerSocketChannel serverSocketChannel;
    //服务端Channel
    private volatile boolean stop;
    public MulTimeServer(){

    }
    public MulTimeServer(int port)
    {
        try {
            //使用.open()创建一个新的未绑定的实例。处于未连接状态
            selector=Selector.open();
            serverSocketChannel=ServerSocketChannel.open();
            //设置非阻塞
            serverSocketChannel.configureBlocking(false);
            //获取与该Channel实例关联的ServerSocket对象，并绑定接口
            serverSocketChannel.socket().bind(new InetSocketAddress(port),1024);
            //注册到多路选择器,SelectionKey.OP_ACCEPT：相当于指定该Channel感兴趣的消息类型。比如这里如果是OP_ACCEPT就会激活Selector抓取
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.out.println("timeserver正在监听端口："+port);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    public void stop()
    {
        this.stop=true;
    }

    @Override
    public void run() {
        //在未停止的情况下Selector轮询，那是怎么轮询的
        while(!stop)
        {
            try {
                selector.select(1000);
                Set<SelectionKey> keys=selector.selectedKeys();
                //获取SelectionKey集合对应的iterator，方便后续进行轮询
                Iterator<SelectionKey> iterator=keys.iterator();
                SelectionKey key=null;
                while(iterator.hasNext())
                {
                    key=iterator.next();
                    iterator.remove();
                    try{
                    handleInput(key);}
                    catch(Exception e){
                    if(key!=null)
                    {
                        key.cancel();
                        if(key.channel()!=null)
                        {
                            key.channel().close();
                        }
                    }
                }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleInput(SelectionKey key) throws IOException
    {
        //.ifAcceptable()：当前就绪状态为OP_ACCEPT
        if(key.isAcceptable())
        {
            //获取key对应的父channel
            ServerSocketChannel ssc=(ServerSocketChannel)key.channel();
            //创建连接的Channel并对他进行注册，这里的SocketChannel就是用于连接客户端的Channel
            SocketChannel socketChannel=ssc.accept();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
        }
        //isReadable()当前通道就绪状态为可读
        if(key.isReadable())
        {
            //获取存在可读信息的与客户端连接通道
            SocketChannel socketChannel=(SocketChannel)key.channel();
            //创建缓冲区并分配空间
            ByteBuffer readBuffer=ByteBuffer.allocate(1024);
            //绑定缓冲区与连接通道
            int len=socketChannel.read(readBuffer);
            if(len>0)
            {
                readBuffer.flip();
                byte[] bytes=new byte[readBuffer.remaining()];
                readBuffer.get(bytes);
                String body=new String(bytes,"UTF-8");
                System.out.println("接收到来自客户端的数据"+body);
                String currentTime="QUERY TIME ORDER".equalsIgnoreCase(body)?
                new java.util.Date(System.currentTimeMillis()).toString():"wrong order";
                doWrite(socketChannel,currentTime);
            }
            else if(len<0)
            {
                key.cancel();
                socketChannel.close();
            }
        }
    }
    private void doWrite(SocketChannel sc,String response) throws IOException
    {
        if(response!=null&&response.trim().length()>0)
        {
            byte[] bytes=response.getBytes();
            ByteBuffer writeBuffer=ByteBuffer.allocate(bytes.length);
            writeBuffer.put(bytes);
            //使用flip
            writeBuffer.flip();
            sc.write(writeBuffer);
        }
    }

    
}
