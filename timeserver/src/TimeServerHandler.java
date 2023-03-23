import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

public class TimeServerHandler extends ChannelHandlerAdapter{
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws UnsupportedEncodingException
    {
        //ByteBuf:类似ByteBuff。不过功能更多
        ByteBuf buf=(ByteBuf) msg;
        //readableBytes()：获取缓冲区可读字节数
        byte[] req=new byte[buf.readableBytes()];
        buf.readBytes(req);
        String body=new String(req,"UTF-8");
        System.out.println("the time server receive:"+body);
        String currentTime="QUERY TIME ORDER".equalsIgnoreCase(body)?new java.util.Date(System.currentTimeMillis()).toString():"BAD ORDER";
        ByteBuf resp=Unpooled.copiedBuffer(currentTime.getBytes());
        //异步发送消息给客户端
        ctx.write(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx)
    {
        //将消息发送队列中更多消息写入到SocketChannel中发给对方。为了防止频繁唤醒Selector，Netty的write不直接将消息写入SocketChannel，调用write方法只是把待发送消息放到发送缓冲数组，
        //再调用flush方法，将缓冲区中的消息全部写道SocketChannel
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx,Throwable cause)
    {
        ctx.close();
    }
}
