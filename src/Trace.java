import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class Trace {
    public static void browseDir(String relative_pathname, HashMap<String,String> accept, String objects_path, ArrayList<String> filename_set) throws IOException {
        String property = System.getProperty("user.dir");
        File dir = new File(property + File.separator + relative_pathname);//文件夹的路径。
        File[] files = dir.listFiles();//文件夹下的文件集
        for (File f : requireNonNull(files)) {
            //如果读取到了文件（非文件夹），则可以尝试写入index中的Hashmap
            if (f.isFile()) {
                String filename = relative_pathname + f.getName(); //获取文件名
                filename_set.add(filename);
                //如果扫描到的文件是class文件，则跳过
                if (filename.contains(".class")) {
                    System.out.println("> 跳过class文件：" + filename);
                    continue;
                }
                //如果扫描到的资源文件，开始写入键值对：“文件名” - “文件内容生成的前40位哈希值”。
                try (FileInputStream is = new FileInputStream(filename)) {
                    try {
                        byte[] bytes = is.readAllBytes();
                        String hash = Processor.EncoderByteSHA1(bytes);
                        if(accept.containsKey(filename)){
                            //文件名与内容均无变化则不填入哈希图。
                            if(accept.get(filename).equals(hash)){
                                continue;
                            }
                            else{//文件名不变，内容更新。
                                //将键值对放入哈希图。
                                accept.put(filename, hash);
                                System.out.println("> 更新文件：" + hash + " " + filename);
                                //Processor.text.add("> 更新文件：" + hash + " " + filename);
                            }
                        }
                        else{//暂存区没有这个文件，将键值对放入哈希图。
                            accept.put(filename, hash);
                            System.out.println("> 添加文件：" + hash + " " + filename);
                            //Processor.text.add("> 添加文件：" + hash + " " + filename);
                        }

                        //同步在objects文件夹建立blob。
                        String blob_content = new String(bytes, StandardCharsets.UTF_8);
                        Blob one_blob = new Blob(blob_content);
                        File blob_file = new File(objects_path + hash);
                        try (ObjectOutputStream write_blob = new ObjectOutputStream(new FileOutputStream(blob_file))) {
                            write_blob.writeObject(one_blob);
                        }
                    } catch (NoSuchAlgorithmException ex) {
                        System.out.println(ex.getMessage());
                        System.exit(0);
                    }
                }
            }
            if(f.isDirectory() && !(f.getName()).equals(".git")){
                String filename = f.getName(); //获取文件夹名
                browseDir( relative_pathname + filename + File.separator, accept, objects_path, filename_set);//递归遍历文件夹
            }
        }
    }
    public static String generateTree(int input, String dir, Set <String> list, HashMap<String, String> check, String objects_path, Iterator <String> it, boolean rerun, String rerun_file)
    throws ClassNotFoundException{
        String separator = "[/\\\\]";
        //输入内容为当前相对路径字符串中应有的斜杠数；当前相对路径；index中HashMap的键集list；index中的HashMap；objects路径；指向list的迭代器；rerun指示值；rerun文件名
        StringBuilder ID = new StringBuilder();
        Tree trace_tree = new Tree();
        boolean flag = false;
        int slash_count = input;

        // 创建 Pattern 对象，获取当前文件夹下的文件
        String pattern = dir +  ".*";//dir为相对路径前缀
        pattern = pattern.replace(File.separator, separator);//正则表达式里的一个斜杠要用两个斜杠表示

        String file_name;
        while(it.hasNext()) {//处理当前层次文件夹下所有的文件 ———— 即文件名里的斜杠数与当前目录相同
            if(rerun){//初始rerun为false，设置rerun变量的原因是如果迭代器it遍历到了最后一个文件，那么it.hasNext()为假，但是这时it指向的文件是没有被处理的，所以需要使用rerun，让其能在下一个代码块被处理。
                //具体的含义为，如果：
                //1）it不指向最后一个，rerun为真，那么这是第一次从上一层的文件夹递归而来到了这一层文件处理的循环，传下来的rerun_file必是一个可能是路径名或者文件名。
                //例如我们这一层为A/B/，上一层为A/，上一层检测到了文件名A/B/5.txt，则会将目录"A/B/"提取出来进入下一层处理(即这一层)，同时把A/B/5.txt作为file_name传下来，rerun设为真，因此我们可以获取到file_name并处理。
                //2) it不指向最后一个，rerun为假，那么这是在遍历根工作目录的文件。
                file_name = rerun_file;
                rerun = false;
            }
            else file_name = it.next();//获取文件名，如果it为初始状态则为获取第一个文件名
            int count = file_name.split(separator).length - 1;//获取文件名中/的个数
            if (count == slash_count && file_name.matches(pattern)) {
                flag = true;
                //如果文件名中斜杠的个数与输入的个数相同，且文件名中包含有输入的相对路径前缀，则说明这是一个本层的文件。
                //但如果遍历到非本层，而是更深层次的文件，则它的文件名中斜杠数会大于本层文件。例如 A/是第一层文件夹, A/b.txt是第一层文件夹里的文件，
                // 他的斜杠数与A/相同；而A/B/5.txt表示的是第二层文件夹里的一个文件，他的斜杠数多了一个。
                ID.append(file_name);//将文件名加入到当前的ID字符串上，最后用于生成根树的哈希值。
                ID.append(check.get(file_name));//将文件名对应的哈希值加到当前的ID字符串上，最后用于生成根树的哈希值。
                trace_tree.content.add(new TreeEle(file_name, check.get(file_name)));//用文件名和文件哈希值生成一个TreeEle，填入Tree中
                it.remove();//文件名列表list里已经处理过的文件直接删去。
            }
        }//继续进行while循环，把所有某一层的文件全部添加完，原理是某一层的文件的文件名里的斜杠数相同，例如我的list里有: A/1.txt, A/2.txt, A/B/c.txt, A/3.txt。
        //则我使用while循环的情况下，最终被处理的只有A/1.txt, A/2.txt, A/3.txt。

        if(rerun){//3) it指向最后一个，rerun为真，这时通过这个判断处理最后那一个被剩下的list里的元素。
            file_name = rerun_file;
            int count = file_name.split(separator).length - 1;
            if (count == slash_count && file_name.matches(pattern)) {
                flag = true;
                ID.append(file_name);
                ID.append(check.get(file_name));
                trace_tree.content.add(new TreeEle(file_name, check.get(file_name)));
                it.remove();
            }
        }

        //处理完某一层的文件后，重置迭代器it，继续处理同层的子文件夹。例如我的list里有:A/1.txt, A/B/C/4.txt, A/2.txt, A/B/3.txt。
        //则A/1.txt,A/2.txt在上一个while循环里已经处理完，我这一层只剩下A/B/3.txt，A/B/C/4.txt这种子文件夹里的文件没处理。
        it = list.iterator();
        int len;

        do{//处理list里所有的非本层文件。
            len = list.size();//当前list里文件名的个数
            while(it.hasNext()){
                file_name = it.next();//因为it重置了，所以要用next指向第一个元素
                int count = file_name.split(separator).length - 1;//获取文件名中/的个数
                if(count == slash_count + 1){
                    //当前目录的直接子文件夹文件文件名中的斜杠数恰比当前目录的斜杠数多一，例如 A/B/5.txt 恰比 A/1.txt 多了一个斜杠。
                    //由此提取出目录 A/B/ 进行递归处理。调用generateTree传入 本层斜杠数+1，文件名列表list，HashMAp check，objects路径
                    //将rerun设为1并传入，A/B/5.txt作为file_name传入，则 A/B/ 层的文件处理方法会对传入的file_name（在下一递归层被命名为rerun_file)，进行处理
                    String path = file_name.substring(0, file_name.lastIndexOf(File.separator) + 1);
                    ID.append(path);

                    rerun = true;
                    String child_tree_hash = generateTree(slash_count + 1, path, list, check, objects_path,it,rerun,file_name);
                    //generateTree函数的返回值是一个字符串，如果你通过根树调用，则最终返回的是根树的哈希值，每层递归会返回一个子树的哈希值，而根树在处理时会将其下所有子树的哈希值拼接到一起
                    //生成一个唯一属于自己的哈希值。

                    it = list.iterator();//重置迭代器
                    ID.append(child_tree_hash);//本层树的用于生成自身哈希值的字符串ID增加上一颗子树的哈希值。
                    trace_tree.content.add(new TreeEle(path, child_tree_hash));
                }
            }
            slash_count++;//如果退出了递归，说明某一层的文件已经遍历完，遍历下一层，考察的斜杠数+1
        }while(!(len == list.size()));//只要list中的元素没有遍历完，就一直循环

        //后面主要是生成本层树哈希值，在objects中添加Tree文件的过程，本层的Tree指向本层的文件和下层的Tree。
        if(flag){
            String hash;
            try{
                hash = Processor.EncoderStringSHA1(ID.toString());
            }
            catch(NoSuchAlgorithmException ex){
                System.out.println("Tree的哈希值生成失败");
                return "fail to generated Tree";
            }

            trace_tree.content.sort(null);

            File new_tree = new File(objects_path + hash);
            if(new_tree.exists()){
                return hash;
            }
            else{
                if(Reviewer.writeTo(trace_tree, objects_path + hash)) {
                    System.out.println("> 添加文件：" + hash + " Tree " + dir);
                    return hash;
                }
                else {
                    System.out.println("生成树失败。");
                    return "fail to generated Tree";
                }
            }

        }
        return "";//这一层没有文件，返回空字符串
    }
    public static void check(String tree_hash, HashMap<String, String> ref, String objects_path){
        Tree pre_tree;//tree_hash为上一次commit指向根树的哈希值，ref为当前index文件里的HashMap。objects_path为objects文件夹路径名
        try(ObjectInputStream get_tree = new ObjectInputStream(new FileInputStream(objects_path + tree_hash))){
            pre_tree = (Tree)get_tree.readObject();//获取上一次commit指向的根树
            for(TreeEle e : pre_tree.content){//遍历上次根树中保存的文件名集，与本次的HashMap比较，判断是更新文件还是删除了文件，并对应输出。
                if(ref.containsKey(e.file_name)){//index中只保存了纯文件名，如果pre_tree中的某个文件名与此，匹配了则进入此分支
                    if(!ref.get(e.file_name).equals(e.hash)){
                        System.out.println("> 更新文件: " + ref.get(e.file_name) + " " + e.file_name + " " + "Blob");
                    }
                    ref.remove(e.file_name);
                }
                else{//index中文件名不与pre_tree匹配，有两种可能，1）pre_Tree中的那个纯文件在index中被删去了  2）pre_tree指向的不是纯文件名，而是一个目录/文件夹名
                    if(e.file_name.endsWith(File.separator)){//根树中存储的TreeEle.file_name，如果以/结尾，则代表这不是一个纯文件，而是一个子树的树根名，或者称为一个子文件夹名
                        //因此向下递归处理
                        check(e.hash, ref, objects_path);
                    }
                    else{//1）pre_Tree中的那个纯文件在index中被删去了
                        System.out.println("> 删除文件: " + e.file_name + " " + "Blob");
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
    public static void restoreIndex(String tree_ID, HashMap<String, String> accept, String objects_path) {

        Tree cur_tree = (Tree) Reviewer.readFrom(objects_path + tree_ID);

        assert cur_tree != null;
        for(TreeEle e:cur_tree.content){
            if(e.file_name.endsWith(File.separator)){
                restoreIndex(e.hash, accept, objects_path);
            }
            else{
                accept.put(e.file_name, e.hash);
            }
        }
    }
    public static void restoreWork(String tree_ID, HashMap<String, String> accept, String property, String objects, ArrayList<String> filename_set) throws IOException {

        Tree cur_tree = (Tree) Reviewer.readFrom(objects + tree_ID);

        assert cur_tree != null;
        for(TreeEle e:cur_tree.content){
            if(e.file_name.endsWith(File.separator)){
                File dir = new File(property + File.separator + e.file_name.substring(0, e.file_name.length()-1));
                dir.mkdirs();
                restoreWork(e.hash, accept, property, objects, filename_set);
            }
        }

        for(TreeEle e:cur_tree.content){
            if(!e.file_name.endsWith(File.separator)){
                File f = new File(property + e.file_name);
                accept.put(e.file_name, e.hash);
                filename_set.add(e.file_name);
                if(!f.exists()) f.createNewFile();
                    /*
                    从TreeEle储存的哈希值中找到对应的Blob，读出content后将content写入刚刚生成的文件。
                    为了方便演示和验收效果，我们在设计Blob类的时候把content设计为了String。如果是实际应用的话可能应该设计为字节数组好一些。
                    同样地，由于事先了解所有Blob的内容都是可读的String，我们在从Blob读取内容还原工作区文件时使用了
                     write_file.write(cur_blob.content.getBytes(StandardCharsets.UTF_8));这样的写法，目的是便于验收。
                     如果是一般性质的文件，则不用加.getBytes(StandardCharsets.UTF_8)这一部分。
                     */
                try(FileOutputStream write_file = new FileOutputStream(f)){
                    Blob cur_blob = (Blob)Reviewer.readFrom(objects + e.hash);
                    write_file.write(requireNonNull(cur_blob).content.getBytes(StandardCharsets.UTF_8));
                    //write_file.writeUTF(cur_blob.content);
                }
                System.out.println("> 更新或生成文件 " + e.file_name);
            }
        }
    }
    public static void deleteDirFiles(String property, String relative_path, ArrayList<String> filename_set){//这里的首个property需要加斜杠。
        String path_name = property.substring(0, property.length() - 1);
        File dir = new File(path_name);//消去结尾的斜杠。
        File[] files = dir.listFiles();

        //删除工作目录与目标commit的index中不对应的多余文件。
        for (File f : requireNonNull(files)) {
            //如果读取到了文件（非文件夹），则可以尝试写入index中的Hashmap
            String filename = relative_path + f.getName(); //获取文件名
            if (f.isFile()) {
                //如果扫描到刚生成/修改的文件，则跳过
                if (filename_set.contains(filename)) {
                    continue;
                }
                //如果扫描到的多余的文件，则删除
                if(f.delete()) System.out.println("> 删除文件 " + filename);
            }
        }

        for (File f : requireNonNull(files)) {
            //如果读取到了文件（非文件夹），则可以尝试写入index中的Hashmap
            String filename = f.getName(); //获取文件名
            if(f.isDirectory() && !filename.endsWith(".git")){
                deleteDirFiles(property + filename + File.separator, relative_path + filename + File.separator, filename_set);//递归
               //删除空文件夹
                File[] layers = f.listFiles();
                if(layers == null || layers.length == 0) {
                    if(f.delete()) System.out.println("> 删除文件夹 " + relative_path + filename);
                }
            }
        }
    }
    public static void senderDelete(String property, String relative_path){//这里的首个property需要加斜杠。
        String path_name = property.substring(0, property.length() - 1);
        File dir = new File(path_name);//消去结尾的斜杠。
        File[] files = dir.listFiles();

        for (File f : requireNonNull(files)) {
            //如果读取到了文件（非文件夹），则删除
            if (f.isFile()) {
                f.delete();
            }
        }

        for (File f : requireNonNull(files)) {
            //如果读取到了文件夹，则递归深入检查
            String filename = f.getName(); //获取文件夹名
            if(f.isDirectory()){
                senderDelete(property + filename + File.separator, relative_path + filename + File.separator);//递归
                //删除空文件夹
                File[] layers = f.listFiles();
                if(layers == null || layers.length == 0) {
                    f.delete();
                }
            }
        }
    }
    public static void sendDir(String relative_pathname, DataOutputStream out) throws IOException {
        String property = System.getProperty("user.dir");
        File dir = new File(property + File.separator + relative_pathname);//文件夹的路径。
        File[] files = dir.listFiles();//文件夹下的文件集
        long length;
        for (File f : requireNonNull(files)) {
            //如果读取到了文件，则将文件长度，文件名，文件内容字节组一齐发送出去
            if (f.isFile()) {
                String filename = relative_pathname + f.getName(); //获取文件名
                try (FileInputStream send_content = new FileInputStream(f)){
                    //System.out.println("找到了客户端要发送的文件，已发送文件。");

                    length = f.length();//写入文件长度，传给客户端让客户端能判断及时结束读取。
                    out.writeLong(length);
                    out.flush();

                    out.writeUTF(filename);//写入相对文件名，用于接收方生成文件。
                    out.flush();

                    out.write(send_content.readAllBytes());//写入文件。
                    out.flush();
                }
            }
            if(f.isDirectory()){
                String filename = f.getName(); //获取文件夹名
                sendDir( relative_pathname + filename + File.separator, out);//递归遍历文件夹
            }
        }
    }
    public static void receiveDir(DataInputStream in) throws IOException {
        String property = System.getProperty("user.dir");
        long length;
        String name;
        String path;

        while(true){
            length = in.readLong();
            if(length == -1) break;

            name = in.readUTF();
            path = property + File.separator + name;//工作目录文件夹的路径。

            File f = new File(path);
            if(!f.exists()){
                try{
                    if(f.createNewFile())
                        System.out.println("> 添加或更新文件 " + name);
                }
                catch(Exception ex){
                    File layer = new File(path.substring(0,path.lastIndexOf(File.separator)));
                    if(layer.mkdirs())
                        System.out.println("> 添加或更新目录 " + path.substring(0,path.lastIndexOf(File.separator)));
                    if(f.createNewFile())
                        System.out.println("> 添加或更新文件 " + name);
                }
            }

            FileOutputStream file = new FileOutputStream(f);
            byte[] buf = new byte[1024];
            int one_read_bytes;

            if (length > 1024) {
                while ((length > 1024) && ((one_read_bytes = in.read(buf, 0, 1024)) != -1)) {
                    file.write(buf, 0, one_read_bytes);
                    file.flush();
                    length = length - one_read_bytes;
                }
            }
            one_read_bytes = in.read(buf, 0, (int)length);
            file.write(buf, 0, one_read_bytes);
            file.flush();
            file.close();
        }
    }
}
