import java.io.UnsupportedEncodingException;
import java.util.logging.Logger;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class TimeClientHandler extends ChannelHandlerAdapter{
    private static final Logger logger=Logger.getLogger(TimeClientHandler.class.getName());
    private final ByteBuf firstMessage;
    
    public TimeClientHandler(){
        byte[] req="QUERY TIME ORDER".getBytes();
        firstMessage=Unpooled.buffer(req.length);
        firstMessage.writeBytes(req);
    }

    //当连接建立成功以后，Netty的NIO线程会调用channelActive。当服务端返回应答消息时，channelRead方法被调用。

    @Override
    public void channelActive(ChannelHandlerContext ctx)
    {
        ctx.writeAndFlush(firstMessage);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg) throws UnsupportedEncodingException
    {
        ByteBuf buf=(ByteBuf) msg;
        byte[]req=new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body=new String(req,"UTF-8");
        System.out.println("now is :"+body);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause)
    {
        logger.warning("wronghhhh"+cause.getMessage());
        ctx.close();
    }
}
