import java.io.*;
import java.util.*;

class TreeEle implements Serializable, Comparable<TreeEle> {
    String file_name;
    String hash;
    String type;
    TreeEle (String name, String info){
        type = "TreeEle";
        file_name = name;
        hash = info;
    }

    @Override
    public int compareTo(TreeEle o) {
        return file_name.compareTo(o.file_name);
    }
}

public class Tree implements Serializable {
    String type;
    ArrayList<TreeEle> content = new ArrayList<>();
    Tree(HashMap<String, String> hmp){
        type = "Tree";
        for (HashMap.Entry<String, String> entry : hmp.entrySet()) {
            TreeEle ele = new TreeEle(entry.getKey(), entry.getValue());
            content.add(ele);
        }
        content.sort(null);
    }
    Tree(){

    }
}
