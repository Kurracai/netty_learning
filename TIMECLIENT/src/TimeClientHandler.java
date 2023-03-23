import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TimeClientHandler implements Runnable{
    private String host;
    private int port;
    private Selector selector;
    private SocketChannel socketChannel;
    private volatile boolean stop;

    public TimeClientHandler(){

    }

    public TimeClientHandler(String host,int port)
    {
        this.host=host==null?"127.0.0.1":host;
        this.port=port;
        try {
            selector=Selector.open();
            socketChannel=SocketChannel.open();
            socketChannel.configureBlocking(false);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    @Override
    public void run() {
        try {
            //先尝试建立连接
            doConnect();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        while(!stop)
        {
            try {
                selector.select(1000);
                Set<SelectionKey> keys=selector.selectedKeys();
                Iterator<SelectionKey> iterator=keys.iterator();
                SelectionKey selectionKey=null;
                while(iterator.hasNext())
                {
                    selectionKey=iterator.next();
                    iterator.remove();
                    try{
                        handleInput(selectionKey);
                    }catch(Exception e)
                    {
                        if(selectionKey!=null)
                        {
                            selectionKey.cancel();
                            if(selectionKey.channel()!=null)
                            {
                                selectionKey.channel().close();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        if(selector!=null)
        {
            try {
                selector.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
    private void handleInput(SelectionKey key) throws IOException
    {
        if(key.isValid())
        {
            SocketChannel sc=(SocketChannel)key.channel();
            if(key.isConnectable())
            {
                if(sc.finishConnect())
                {
                    sc.register(selector, SelectionKey.OP_READ);
                    doWrite(sc);
                }
                else{
                    System.exit(1);
                }
            }
            if(key.isReadable())
            {
                ByteBuffer readBuffer=ByteBuffer.allocate(1024);
                int len=sc.read(readBuffer);
                if(len>0)
                {
                    readBuffer.flip();
                    byte[] bytes=new byte[readBuffer.remaining()];
                    readBuffer.get(bytes);
                    String body=new String(bytes,"UTF-8");
                    System.out.println("现在时间是"+body);
                    this.stop=true;
                }
                else if(len<0){
                    key.cancel();
                    sc.close();
                }
            }
        }
    }

    //建立连接
    private void doConnect() throws IOException
    {
        //如果连接成功就注册到多路复用器上，关注他的read信息
        if(socketChannel.connect(new InetSocketAddress(host, port))){
            socketChannel.register(selector, SelectionKey.OP_READ);
            doWrite(socketChannel);
        }
        else{
            //未连接就基于连接
            socketChannel.register(selector,SelectionKey.OP_CONNECT);
        }
    }

    //写操作
    private void doWrite(SocketChannel socketChannel) throws IOException
    {
        //将query time order加入到写操作数据中
        byte[] req="query time order".getBytes();
        ByteBuffer writeBuffer=ByteBuffer.allocate(req.length);
        writeBuffer.put(req);
        writeBuffer.flip();
        socketChannel.write(writeBuffer);
        if(!writeBuffer.hasRemaining())
        {
            System.out.println("send success");
        }
    }
}
