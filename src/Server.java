import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Scanner;

public class Server {
    int port = 8888;
    ServerSocket ss;
    public Server() {
        Scanner sc = new Scanner(System.in);
        System.out.println("服务器启动");
        try{
            System.out.print("IP地址默认选择本机地址。请输入你选择的端口号，若选择默认端口请输入\"0\"。你的选择是：");
            String select_port = sc.nextLine();
            if(select_port.equals("0")) {
                System.out.print("> 建立默认服务器端口 8888。");
            }
            else {
                port = Integer.parseInt(select_port);
                System.out.print("> 建立服务器端口 " + port + "。");
            }
            System.out.println();
            ss = new ServerSocket(port);
            System.out.print("> 服务器主机IP地址为：");
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println(hostAddress);
            System.out.print("> 本机环回地址为：127.0.0.1");
        }
        catch(Exception ex){
            System.out.println("Port端口建立出现异常。错误情况：");
            System.out.println(ex.getMessage());
            System.out.println("使用方法为：输入一个四位数字（代表代表想连接到的服务器的端口）。");
            System.out.print("如果想使用默认服务器IP地址和默认Port，则输入0");
            System.exit(-1);
        }
    }
    public void service() {
        String mode;
        while(true){
            Socket server;
            try {
                //接收客户端的连接请求，返回套接字。
                System.out.println();
                System.out.println("**********************************");
                System.out.println("......等待客户端连接");
                server = ss.accept();
                System.out.println("收到客户端：" + server.getRemoteSocketAddress() + " 的连接请求 | 服务器套接字：" + server.getLocalSocketAddress());
                //获取到输入输出流，接受客户端发来的数据或者给客户端发送数据。
                try (DataOutputStream out = new DataOutputStream(server.getOutputStream());
                     DataInputStream in = new DataInputStream(server.getInputStream())) {

                    StringBuffer rec_str = new StringBuffer("服务器收到了客户端的指令：\"");
                    mode = in.readUTF();
                    rec_str.append(mode).append("\"");
                    System.out.println(rec_str);
                    System.out.println();

                    switch (mode) {
                        case "push" -> {
                            String property = System.getProperty("user.dir") + File.separator;

                            Trace.senderDelete(property, "");//删除原有文件
                            Trace.receiveDir(in);//新建传送而来的文件

                            System.out.println("服务器收到了客户端的来信：" + "\"" + in.readUTF() + "\"");

                            out.writeUTF("服务器已收到了文件。");
                            out.flush();

                            System.out.println("成功收到文件。已向客户端发送回信。");
                            server.close();
                        }
                        case "pull" -> {
                            Trace.sendDir("", out);

                            long ender = -1;
                            out.writeLong(ender);//写入push，用于客户端检验
                            out.flush();

                            out.writeUTF("文件发送完毕。");
                            out.flush();

                            System.out.println("服务器收到了客户端是否收到文件的回信：" + "\"" + in.readUTF() + "\"");//读入客户端发来的回应消息。
                            server.close();
                        }
                        case "detach" -> {
                            System.out.println("停止侦听，关闭服务器");
                            //释放资源
                            server.close();
                            System.exit(0);
                        }
                    }
                }
            }
            catch(IOException e){
                System.out.println("连接失败。错误情况：");
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public static void main(String[] args) {
        new Server().service();
    }
}
