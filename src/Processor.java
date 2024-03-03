import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Processor {
    public static String EncoderStringSHA1(String str) throws NoSuchAlgorithmException {
        //确定计算方法
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");//（1）生成MessageDigest对象
        sha1.update(str.getBytes(StandardCharsets.UTF_8));//（2）传入需要计算的字符串,update传入的参数是字节类型或字节类型数组，对于字符串，需先生成字符数组
        byte[] s = sha1.digest();//（3）计算消息摘要.执行MessageDigest对象的digest( )方法完成计算，计算的结果通过字节类型的数组返回。
        StringBuilder result = new StringBuilder();
        for (byte b : s) result.append(Integer.toHexString((0x000000ff & b) | 0xffffff00).substring(6));
        return result.toString();
    }

    public static String EncoderByteSHA1(byte[] bi) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");//（1）生成MessageDigest对象
        sha1.update(bi);//（2）传入需要计算的字符串,update传入的参数是字节类型或字节类型数组，对于字符串，需先生成字符数组
        byte[] s = sha1.digest();//（3）计算消息摘要.执行MessageDigest对象的digest( )方法完成计算，计算的结果通过字节类型的数组返回。
        StringBuilder result = new StringBuilder();
        for (byte b : s) result.append(Integer.toHexString((0x000000ff & b) | 0xffffff00).substring(6));
        return result.toString();
    }

    public static boolean init() throws IOException {
        String property = System.getProperty("user.dir");
        File root_path = new File(property + File.separator +".git" + File.separator + "objects");
        return root_path.mkdirs();
    }

    public static boolean add(String[] args) throws IOException, ClassNotFoundException {
        String property = System.getProperty("user.dir");//首先设定工作目录。
        property = property + File.separator;
        //System.out.println("工作目录是：" + property);
        String objects = property + ".git" + File.separator + "objects" + File.separator ;//objects目录
        //System.out.println("objects目录是：" + objects);

        //创建index文件，如果还未实际创建index文件，就创建它。
        String file_path = property + ".git" + File.separator + "index.txt";
        File index_file = new File(file_path);
        if (!index_file.exists()) {
            if (index_file.createNewFile()) System.out.println("第一次add，创建了 index.txt 文件。");
            //写入空的HashMap
            HashMap<String, String> index = new HashMap<>();
            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(index_file));
            output.writeObject(index);
            output.close();
        }

        HashMap<String, String> accept = new HashMap<>();
        try {
            try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(index_file))) {
                if (args[1].equals(".")) {//检测到add后的参数为.，依次读取根目录里的文件，更新index.txt。
                    ArrayList<String> filename_set = new ArrayList<>();
                    try{
                        Trace.browseDir("", accept, objects, filename_set);
                    }
                    catch(Exception ex){
                        System.out.println("Trace.browseDir调用出现异常");
                        System.out.println(ex.getMessage());
                        return false;
                    }
                    ArrayList<String> remove_keyset = new ArrayList<>();
                    for (HashMap.Entry<String,String> entry: accept.entrySet()) {
                        if(!filename_set.contains(entry.getKey())){
                            remove_keyset.add(entry.getKey());
                        }
                    }
                    for(String s : remove_keyset){
                        accept.remove(s);
                    }
                    ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(index_file));
                    output.writeObject(accept);
                    output.close();
                }
                else {//检测到add后的参数为单个文件，依次读取根目录里的特定文件，更新index.txt。//反序列化读出HashMap进行操作。
                    accept = (HashMap<String, String>) input.readObject();
                    File wait_for_added = new File(property + args[1]);//要求加入暂存区的文件路径
                    //如果要求加入的文件不存在，则进行特殊处理。
                    if (!wait_for_added.exists()) {
                        if (accept.containsKey(args[1])) {
                            //由于文件名存在与暂存区，但工作目录没有该文件 → 从暂存区删除该文件。
                            accept.remove(args[1]);
                            //将更新后的HashMap存储于 index.txt。
                            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(index_file));
                            output.writeObject(accept);
                            output.close();
                            return true;
                        }//否则
                        System.out.println("> 暂存区与工作目录均不存在你想上传的文件。");
                        return false;
                    }
                    //要求加入的文件存在，更新index。
                    try (FileInputStream is = new FileInputStream(wait_for_added);
                         ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(index_file))) {
                        try {
                            byte[] bytes = is.readAllBytes();
                            String hash = EncoderByteSHA1(bytes);
                            String Blob_content = new String(bytes, StandardCharsets.UTF_8);

                            //将键值对放入哈希图，经将更新后的HashMap存储于 index.txt。
                            accept.put(args[1], hash);
                            output.writeObject(accept);
                            //System.out.print("> 更新文件：" + args[1] + " " + hash);

                            //同步在objects文件夹建立Blob。
                            Blob one_Blob = new Blob(Blob_content);
                            File Blob_file = new File(objects + hash);
                            try (ObjectOutputStream write_Blob = new ObjectOutputStream(new FileOutputStream(Blob_file))) {
                                write_Blob.writeObject(one_Blob);
                            }
                        } catch (NoSuchAlgorithmException ex) {
                            System.out.println(ex.getMessage());
                            return false;
                        }
                    }
                }
                //System.out.println("已经将更新后的HashMap存储于 index.txt。");
            }
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.out.print("您没有在add命令中输入合适的参数。");
            System.out.println("格式为 add + \".\"或\"单个文件名\"");
            System.out.println(ex.getMessage());
            return false;
        }
        return true;
    }

    public static boolean commit(String[] args) throws IOException {
        //首先设定工作目录。
        String property = System.getProperty("user.dir");
        property = property + File.separator;
        //objects目录
        String objects = property + ".git" + File.separator + "objects" + File.separator ;//objects目录
        Scanner sc = new Scanner(System.in);

        try{
            if (!(args[1].equals("-m"))) {
                System.out.println("输入格式有误，格式应为 commit + -m + \"你的备注信息\" + \".\"或\"单个文件名\"");
                return false;
            }
            //其指向的根树的ID、author、committer、提交备注、parent提交
            String ID = null;
            String author;
            String committer;
            String message = args[2];
            String parent;
            String mode = args[3];//用来检测输入是否合规。

            String file_path = property + ".git" + File.separator + "index.txt";
            File index_file = new File(file_path);//index文件的目录，待使用。
            if(!index_file.exists()){
                System.out.println("还未初始化暂存区。");
                return false;
            }

            System.out.print("请输入作者author：");
            author = sc.nextLine();
            System.out.print("请输入提交者committer：");
            committer = sc.nextLine();

            StringBuilder tree_data = new StringBuilder(); //用来存放commit所指向子树的独特内容，用以生成哈希值。
            String hash;//用来存放commit自身生成的哈希值。。
            Tree target;//声明一棵树，后续会用index里的HashMap初始化。
            HashMap<String, String> accept;//声明一个哈希图，用于修改index.txt。

            try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(index_file))) {
                //反序列化读出HashMap进行操作，用这个HashMap给commit的tree进行赋值。
                accept = (HashMap<String, String>) input.readObject();
                target = new Tree(accept);
                if(args[3].equals(".")){//commit暂存区的所有内容
                    try{//将index里所有的文件名+hash值链接在一起形成每个tree独有的字符串，用这个字符串生成tree的文件名。
                        Set<String> s = accept.keySet();
                        Iterator<String> it = s.iterator();
                        ID = Trace.generateTree(0,"",s,accept,objects,it,false,"");
                        System.out.println("-----------------------------------------------------------------");
                    }
                    catch(NullPointerException ex){
                        System.out.println(ex.getMessage());
                        ex.printStackTrace();
                    }
                }
                else{//commit暂存区的指定文件，残缺的方法，可以不看
                    if(!accept.containsKey(args[3])){
                        System.out.println("您想commit的文件不存在。");
                        return false;
                    }
                    else{
                        HashMap<String, String> single_hmp = new HashMap<>();//生成新的仅包含指定Blob文件的哈希图，用以生成树。
                        String single_tree_hash = accept.get(args[3]);
                        single_hmp.put(args[3], single_tree_hash);
                        target = new Tree(single_hmp);//target被重新赋值为一个新的只保存了单个Blob的树

                        tree_data.append(args[3]).append(single_tree_hash);//用指定的文件名+哈希值作为tree独有的字符串，用以生成tree的哈希值。
                    }
                }
            }

            StringBuilder commit_data = new StringBuilder(ID);//记录一次commit的独特数据(ID + author + committer + parent 拼接而成的字符串)，用于生成哈希值命名commit文件。
            commit_data.append(author).append(committer);
            //生成本次commit文件名(哈希值)。
            try{
                hash = EncoderStringSHA1(commit_data.toString());
            }
            catch(NoSuchAlgorithmException ex){
                System.out.println("commit哈希值生成失败。");
                return false;
            }

            File HEAD_file = new File(property + ".git" + File.separator + "HEAD.txt");
            //判断是否为第一次commit
            if(!HEAD_file.exists()){//如果为第一次commit，则不存在HEAD.txt文件。
                if(HEAD_file.createNewFile()) {//创建HEAD.txt文件。
                    System.out.println("[这是第一次commit。创建HEAD.txt文件成功。指向的前一次commit为null。]");
                }
                else{
                    System.out.println("这是第一次commit。创建HEAD.txt文件失败。");
                    return false;
                }
                parent = null;//前一次commit指向null

                //生成本次的commit对象。
                CommitObj my_commit = new CommitObj(ID, author, committer, message, parent);

                //输出本次的commit变动，均为添加变动。
                for(TreeEle e:target.content){
                    System.out.println("> 添加文件：" + e.hash + " " + e.file_name + " " + "Blob");
                }

                //创建本次commit文件，并将CommitObj对象写入。
                ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(property + ".git" + File.separator + "objects" + File.separator + hash));
                output.writeObject(my_commit);
                System.out.println("> 添加文件: " + hash + " " + "commit");
                output.close();
            }
            else{
                DataInputStream read = new DataInputStream(new FileInputStream(HEAD_file));//获取前一次commit的哈希值
                parent = read.readUTF();
                read.close();

                try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(index_file))) {
                    //反序列化读出HashMap进行操作。
                    accept = (HashMap<String, String>) input.readObject();
                }

                //如果本次commit与上一次的ID，author，committer均相同，则不生成新的commit。
                if(parent.equals(hash)) {
                    System.out.println("> 与上一次commit的ID，author，committer均相同，没有需要commit的内容。");
                    return true;
                }

                //获得前一次commit指向的tree
                CommitObj pre_commit;
                try(ObjectInputStream get_parent = new ObjectInputStream(new FileInputStream(objects + parent))){
                    pre_commit = (CommitObj)get_parent.readObject();
                }

                //将现在的index.txt与上次的tree进行对比，给出变化项
                Trace.check(pre_commit.ID, accept, objects);

                //为了判断暂存区新增了哪些文件，需要上一次tree指向的文件名集来操作。
                for (HashMap.Entry<String, String> entry : accept.entrySet()) {
                    System.out.println("> 添加文件: " + entry.getValue() + " " + entry.getKey() + " " + "Blob");
                }

                //生成本次的commit对象。
                CommitObj my_commit = new CommitObj(ID, author, committer, message, parent);

                //创建本次commit文件，并将CommitObj对象写入。
                ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(property + ".git" + File.separator + "objects" + File.separator + hash));
                output.writeObject(my_commit);
                System.out.println("> 添加文件: " + hash + " " + "commit");
                output.close();
            }

            //向HEAD.txt更新一条commit记录（哈希值）。
            DataOutputStream head = new DataOutputStream(new FileOutputStream(HEAD_file));
            head.writeUTF(hash);
            head.close();
        }
        catch(ArrayIndexOutOfBoundsException ex){
            System.out.print("您没有在commit命令中输入合适的参数。");
            System.out.println("格式为 commit + -m + \"你的备注信息\" + \".\"或\"单个文件名\"");
            System.out.println(ex.getMessage());
            return false;
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}
class Blob implements Serializable {
    String type;
    int size;
    String content;
    Blob (String info){
        type = "Blob";
        size = info.length();
        content = info;
    }
}