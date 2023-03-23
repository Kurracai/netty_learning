public class TimeServer {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
        int port=8080;
        MulTimeServer timeServer=new MulTimeServer(port);
        new Thread(timeServer,"time server").start();
    }
}
