/*
 * 打开SocketChannel并绑定
 * 设置channel为非阻塞模式并设置客户端连接的TCP参数
 * 异步连接服务器
 * 判断是否连接成功，连接成功就注册
 * 向Reactor线程的多路复用器注册OP_CONNECT监听服务器端TCP ACK应答
 * 创建Reactor线程，创建多路复用器并启动线程
 * 多路复用器再run方法这个i部分轮询就绪key
 * 接受connnect事件并处理
 * 注册读事件到多路复用器
 * 异步读事件到多路复用器
 * 异步读客户端请求消息到缓冲区
 * 对ByteBuffer编解码，如果有半包消息接收缓冲区Reset，继续读取后续保温，将解码成功
 * -消息封装成Task，投递到业务线程池并进行业务逻辑编排
 * 将POJO对象encode成ByteBuffer，调用SocketChannel的异步write接口将消息异步发送给客户端
 */

public class TimeClient {
    public static void main(String[] args) {
    int port=8080;
    new Thread(new TimeClientHandler("127.0.0.1",port),"client").start();   
    }
}
