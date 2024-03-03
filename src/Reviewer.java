import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Reviewer {
    public static boolean rm(String[] args) throws ArrayIndexOutOfBoundsException, IOException, ClassNotFoundException {
        //首先设定工作目录。
        String property = System.getProperty("user.dir");
        property = property + File.separator;
        //System.out.println("工作目录是：" + property);
        //index.txt目录
        String file_path = property + ".git" + File.separator + "index.txt";
        File index_file = new File(file_path);

        try{//判断输入格式是 rm + 文件名 还是 rm + --cached + 文件名
            if (!args[1].equals("--cached")) {
                File tobe_deleted = new File(args[1]);
                if (!tobe_deleted.exists()) {
                    System.out.println("你想删除的文件不存在！");
                    return false;
                }
                if(tobe_deleted.delete()){
                    System.out.println("已经删除文件。");
                }
                try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(index_file))) {
                    HashMap<String, String> accept = (HashMap<String, String>) input.readObject();
                    if (!accept.containsKey(args[1])) {
                        System.out.println("暂存区没有您想删除的记录。");
                        return false;
                    }
                    accept.remove(args[1]);
                    ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(index_file));
                    output.writeObject(accept);
                    output.close();
                    System.out.println("已经删除对应暂存区记录。");
                }
            }
            else{
                try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(index_file))) {
                    HashMap<String, String> accept = (HashMap<String, String>) input.readObject();
                    if (!accept.containsKey(args[2])) {
                        System.out.println("暂存区没有您想删除的记录。");
                        return false;
                    }
                    accept.remove(args[2]);
                    ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(index_file));
                    output.writeObject(accept);
                    output.close();
                    System.out.println("已经删除对应暂存区记录。");
                }
            }
        }
        catch (ArrayIndexOutOfBoundsException ex){
            System.out.println("rm命令使用格式错误。正确的格式为: ");
            System.out.println("rm + 文件名 或者 rm + --cached + 文件名");
            return false;
        }
        return true;
    }
    public static boolean log() throws IOException, ClassNotFoundException{
        String property = System.getProperty("user.dir");
        property = property + File.separator;

        //首先声明objects文件夹路径。
        String objects = property + ".git" + File.separator + "objects" + File.separator;

        //读取HEAD.txt文件
        File HEAD_file = new File(property + ".git" + File.separator + "HEAD.txt");

        //若HEAD.txt文件不存在输出错误
        if(!HEAD_file.exists()){
            System.out.println("还未进行任何commit或reset。");
            return false;
        }

        //获取最近一次commit的哈希值
        DataInputStream read = new DataInputStream(new FileInputStream(HEAD_file));
        String parent = read.readUTF();
        read.close();

        while(parent != null){
            try(ObjectInputStream read_commit = new ObjectInputStream (new FileInputStream(objects + parent))){
                CommitObj cur_commit = (CommitObj) read_commit.readObject();
                System.out.println("commit: " + " " + parent);
                System.out.println("message: " + cur_commit.message);
                System.out.println("date: " + cur_commit.date);
                System.out.println();
                parent = cur_commit.parent;
            }
        }

        return true;
    }
    public static boolean reset(String[] args) throws IOException, ClassNotFoundException{
        String property = System.getProperty("user.dir");
        property = property + File.separator;

        //首先声明objects文件夹路径。
        String objects = property + ".git" + File.separator + "objects" + File.separator;

        //读取HEAD.txt文件
        File HEAD_file = new File(property + ".git" + File.separator + "HEAD.txt");

        //若HEAD.txt文件不存在输出错误
        if(!HEAD_file.exists()){
            System.out.println("还未进行任何commit或reset。");
            return true;
        }

        //三种不同模式 --soft|--mixed|--hard 下的不同处理
        try{
            if(args[1].equals("--soft")){//--soft模式
                File given_ID = new File(objects + args[2]);//检查是否存在输入的commitID文件
                if(!given_ID.exists()){
                    System.out.println("不存在commit " + args[2]);
                    return false;
                }

                //改写HEAD.txt文件
                DataOutputStream write = new DataOutputStream(new FileOutputStream(HEAD_file));
                System.out.println("将HEAD.txt文件内容改写为：" + args[2]);
                write.writeUTF(args[2]);
                write.close();
            }
            else if(!args[1].equals("--hard")){

                String commit_hash;
                if(args[1].equals("--mixed")){//--mixed模式
                    commit_hash = args[2];
                }
                else{//默认模式
                    commit_hash = args[1];
                }

                //检查是否存在输入的commitID文件
                File given_ID = new File(objects + commit_hash);
                if(!given_ID.exists()){
                    System.out.println("不存在commit " + commit_hash);
                    return false;
                }

                CommitObj trace_commit;

                //改写HEAD.txt文件
                DataOutputStream write = new DataOutputStream(new FileOutputStream(HEAD_file));
                System.out.println("将HEAD.txt文件内容改写为：" + commit_hash);
                write.writeUTF(commit_hash);
                write.close();

                //重置暂存区
                try(ObjectInputStream read_commit = new ObjectInputStream(new FileInputStream(objects + commit_hash))){
                    trace_commit = (CommitObj) read_commit.readObject();
                }

                HashMap<String, String> accept = new HashMap<>();
                Trace.restoreIndex(trace_commit.ID, accept, objects);

                try(ObjectOutputStream write_index = new ObjectOutputStream(new FileOutputStream(property + ".git" + File.separator + "index.txt"))){
                    write_index.writeObject(accept);
                }
                System.out.println("将暂存区index.txt文件内容还原");
            }
            else{
                //检查是否存在输入的commitID文件
                File given_ID = new File(objects + args[2]);
                if(!given_ID.exists()){
                    System.out.println("不存在commit " + args[2]);
                    return false;
                }

                CommitObj trace_commit;

                //改写HEAD.txt文件
                DataOutputStream write = new DataOutputStream(new FileOutputStream(HEAD_file));
                System.out.println("将HEAD.txt文件内容改写为：" + args[2]);
                write.writeUTF(args[2]);
                write.close();

                //重置暂存区
                try(ObjectInputStream read_commit = new ObjectInputStream(new FileInputStream(objects + args[2]))){
                    trace_commit = (CommitObj) read_commit.readObject();
                }

                HashMap<String, String> accept = new HashMap<>();//用于接受还原完成后的HashMap
                ArrayList<String> filename_set = new ArrayList<>();//用于记录Tree中存储的文件名，便于稍后删除工作区的多余文件。

                System.out.println("已将暂存区index.txt文件内容还原");

                Trace.restoreWork(trace_commit.ID, accept, property, objects, filename_set);//递归方法还原工作区

                Reviewer.writeTo(accept, property + ".git" + File.separator + "index.txt");

                //递归方法区删除工作目录与目标commit的index中不对应的多余文件。
                Trace.deleteDirFiles(property, "", filename_set);

                System.out.println("已将工作区文件还原");
            }
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.out.println("reset命令使用格式错误。正确的格式为: ");
            System.out.println("reset + --soft|--mixed|--hard + 某次commitID(即某次commit的哈希值)");
            return false;
        }
        return true;
    }
    public static Object readFrom(String path) {
        File f = new File(path);
        if (!f.exists()) {
            System.out.println("还未创建这个文件，无法读");
            return null;
        }
        try {
            try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(path))) {
                return input.readObject();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }
    public static <T> boolean writeTo(T obj, String path) {
        File f = new File(path);
        if(!f.exists()){
            try{
                f.createNewFile();
            }
            catch(IOException ex){
                System.out.println("未创建父文件夹，无法创建文件");
                return false;
            }
        }
        try{
            try(ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(path))){
                output.writeObject(obj);
            }
            return true;
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
            return false;
        }
    }
}


