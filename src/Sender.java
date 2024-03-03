import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Sender {
    public static boolean send (String mode) throws IOException {
        Scanner sc = new Scanner(System.in);
        int port = 8888;
        String server_name = "127.0.0.1";

        try{
            System.out.print("输入 . 选择连接到默认服务器套接字 /127.0.0.1:8888 ,否则任意输入: ");
            if(!sc.nextLine().equals(".")){
                System.out.println("> 自行指定连接到的服务器IP与套接字端口");
                System.out.print("选择连接到的服务器的IP地址：");
                server_name = sc.nextLine();
                System.out.print("选择连接到的服务器的端口：");
                port = Integer.parseInt(sc.nextLine());
            }
            System.out.println();
        }
        catch(Exception ex){
            System.out.println("服务器IP地址与Port端口号获取出现异常。错误情况：");
            System.out.println(ex.getMessage());
            System.out.println("根据提示先后输入一个字符串（代表想连接到的服务器IP地址）与 一个四位数字（代表代表想连接到的服务器的端口）。");
            System.out.print("如果想使用默认服务器套接字 /127.0.0.1:8888，则直接输入 . 。");
            return false;
        }

        try{
            Client my_client  = new Client(server_name, port);
            switch (mode) {
                case "push" -> {
                    return my_client.cliPush();
                }
                case "pull" -> {
                    return my_client.cliPull();
                }
                case "detach" -> {
                    return my_client.detach();

                }
                default -> throw new IllegalStateException("Unexpected value: " + mode);
            }
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }
}

class Client {
    //提示用户输入想要连接的服务器IP地址与Port端口号。
    String server_name = "127.0.0.1";
    int port = 8888;
    Socket client;
    String property = System.getProperty("user.dir");
    Client(String sn, int pt) throws IOException {
        //建立客户端套接字，指定要连接的服务器，同时指定服务器的IP与Port。
        server_name = sn;
        port = pt;
        try{
            client = new Socket(server_name, port);
        }
        catch(java.net.ConnectException ex){
            System.out.println("连接失败，***请确保先运行了服务器***。");
            System.exit(1);
        }
        catch( java.net.UnknownHostException Un){
            System.out.println("连接失败，***IP地址格式错误***。");
            throw new java.net.UnknownHostException(Un.getMessage());
        }
        catch( IllegalArgumentException Ill){
            System.out.println("连接失败，***端口号格式错误***。");
            throw new java.net.UnknownHostException(Ill.getMessage());
        }
        System.out.println("客户端为：" + client.getLocalSocketAddress());
        System.out.println("尝试连接到服务器的IP地址为：" + client.getInetAddress().getHostAddress() + " ，端口号为：" + client.getPort());
    }
    Client() throws IOException {
        //建立客户端套接字，指定要连接的服务器，同时指定服务器的IP与Port。
        client = new Socket(server_name, port);
        System.out.println("客户端为：" + client.getLocalSocketAddress());
        System.out.println("尝试连接到服务器的IP地址为：" + client.getInetAddress().getHostAddress() + " ，端口号为：" + client.getPort());
    }
    boolean cliPush(){//传入客户端想要向服务端传送的文件名。
        try{
            if(client.getRemoteSocketAddress() != null) System.out.println("与服务器连接成功！可以发送数据。");
            else System.out.println("还未连接成功。");
            try(DataOutputStream out = new DataOutputStream(client.getOutputStream());
                DataInputStream in = new DataInputStream(client.getInputStream())){

                out.writeUTF("push");//写入push，用于客户端检验
                out.flush();

                System.out.println();
                System.out.println("请求已发送");

                Trace.sendDir("", out);

                long ender = -1;
                out.writeLong(ender);//写入push，用于客户端检验
                out.flush();

                out.writeUTF("文件发送完毕。");
                out.flush();

                System.out.println("客户端收到了服务器是否收到文件的回信：" + "\"" + in.readUTF() + "\"");//读入客户端发来的回应消息。
            }
            //释放资源。
            client.close();
        }catch(Exception e)
        {
            System.out.println("连接失败，***请确保先运行了服务器***。错误情况：");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
    boolean cliPull() {//传入客户端想要向服务端申请的文件名。
        try{
            if(client.getRemoteSocketAddress() != null) System.out.println("与服务器连接成功！可以发送数据。");
            else System.out.println("还未连接成功。");
            try(DataOutputStream out = new DataOutputStream(client.getOutputStream());
                DataInputStream in = new DataInputStream(client.getInputStream())){

                out.writeUTF("pull");//写入push，用于客户端检验
                out.flush();

                System.out.println();
                System.out.println("请求已发送");

                String property = System.getProperty("user.dir") + File.separator;
                Trace.senderDelete(property, "");//删除原有文件
                Trace.receiveDir(in);//新建传送而来的文件

                System.out.println("客户端收到了服务器的回应： "+ "\""  + in.readUTF() + "\"" );

                out.writeUTF("客户端已收到了文件。");
                out.flush();

                System.out.println("成功收到文件。已向服务器端发送回信。");
                }
            //释放资源。
            client.close();
            }
        catch(Exception e)
        {
            System.out.println("连接失败，***请确保先运行了服务器***。错误情况：");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
    boolean detach(){
        try{
            if(client.getRemoteSocketAddress() == null) System.out.println("还未连接成功。");
            try(DataOutputStream out = new DataOutputStream(client.getOutputStream())){

                out.writeUTF("detach");//写入push，用于客户端检验
                out.flush();

                System.out.println();
                System.out.println("关闭连接申请已发送");
                client.close();//释放资源。
                System.exit(0);
            }
        }catch(Exception e)
        {
            System.out.println("发送失败，***请确保服务器确实在侦听***。错误情况：");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
}