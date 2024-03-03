import java.io.Serializable;
import java.sql.Timestamp;

public class CommitObj implements Serializable {
    String type;
    String ID;
    String author;
    String committer;
    String message;
    String parent;
    String date;
    CommitObj (String i, String a, String c, String m, String p){
        type = "CommitObj";
        ID = i;
        author = a;
        committer = c;
        message = m;
        parent = p;
        Timestamp ts = new Timestamp(System.currentTimeMillis());
        date = ts.toString();
    }
}
