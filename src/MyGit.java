import java.io.*;
import static java.lang.System.exit;

public class MyGit {
    public static void main(String[] args) throws ArrayIndexOutOfBoundsException {
        try{
            switch (args[0]) {
                case "init" -> {
                    if (Processor.init()) System.out.println("***** init命令执行！");
                    else System.out.println("***** init命令没有正常执行。创建失败或者已创建。");
                }
                case "add" -> {
                    if (Processor.add(args)) System.out.println("***** add命令执行！");
                    else System.out.println("***** add命令没有正常执行。");
                }
                case "commit" -> {
                    if (Processor.commit(args)) System.out.println("***** commit命令执行！");
                    else System.out.println("***** commit命令没有正常执行。");
                }
                case "rm" -> {
                    if (Reviewer.rm(args)) System.out.println("***** rm命令执行！");
                    else System.out.println("***** rm命令没有正常执行。");
                }
                case "log" -> {
                    if (Reviewer.log()) System.out.println("***** log命令执行！");
                    else System.out.println("***** log命令没有正常执行。");
                }
                case "reset" -> {
                    if (Reviewer.reset(args)) System.out.println("***** reset命令执行！");
                    else System.out.println("***** reset命令没有正常执行。");
                }
                case "push" -> {
                    if (Sender.send(args[0])) System.out.println("***** push命令执行！");
                    else System.out.println("***** push命令没有正常执行。");
                }
                case "pull" -> {
                    if (Sender.send(args[0])) System.out.println("***** pull命令执行！");
                    else System.out.println("***** pull命令没有正常执行。");
                }
                case "detach" -> {
                    if (Sender.send(args[0])) System.out.println("***** detach命令执行！");
                    else System.out.println("***** detach命令没有正常执行。");
                }
                case "clear" -> {
                    File delete_path = new File(".git");
                    if (delete_path.delete()) System.out.println("delete命令执行！.git文件夹已删除。");
                }
                case "help" -> {
                    System.out.printf("%-30s%-50s \n", "名称与用法","释意");
                    System.out.printf("%-30s%-50s \n", " init ","在工作目录创建一个.git仓库并生成objects文件夹");
                    System.out.printf("%-30s%-50s \n", " add [.]","将工作目录的全部文件记录到暂存区");
                    System.out.printf("%-30s%-50s \n", " add [文件名]","将工作目录的特定一个文件记录到暂存区");
                    System.out.printf("%-30s%-50s \n", " commit [-m][备注信息][.]","将工作目录的全部文件保存到仓库");
                    System.out.printf("%-30s%-50s \n", " rm [文件名]","从暂存区和工作目录同时删除指定的文件");
                    System.out.printf("%-30s%-50s \n", " rm [--cached][文件名]","从暂存区删除指定的文件");
                    System.out.printf("%-30s%-50s \n", " log ","从最近一次开始列出所有commit记录");
                    System.out.printf("%-30s%-50s \n", " reset [--soft][ commitID]","将HEAD头文件内容更改为指定的commitID");
                    System.out.printf("%-30s%-50s \n", " reset [--mixed][ commitID]","在soft的基础上，将暂存区内容还原为指定commit指向的内容");
                    System.out.printf("%-30s%-50s \n", " reset [--hard][ commitID]","在mixed的基础上，将工作区文件还原为指定commit指向的内容");
                    System.out.printf("%-30s%-50s \n", " pull ","从远程仓库拉取最近一次备份，将本地工作区还原为备份");
                    System.out.printf("%-30s%-50s \n", " push ","将工作区全部内容更新到远程仓库");
                    System.out.printf("%-30s%-50s \n", " detach ","让远程仓库服务器停止侦听");
                    System.out.printf("%-30s%-50s \n", " help ","输出提示信息");
                }
                default -> System.out.println("您输入的命令有误，请重新输入！");
            }
        }
        catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("***** 您没有输入任何内容或者您没有输入适当数量的参数。");
            System.out.println("***** 输入 help 可以查看操作手册。");
            System.out.println(ex.getMessage());
            exit(0);
        }
        catch (IOException | ClassNotFoundException ex){
            System.out.println(ex.getMessage());
            exit(0);
        }
    }
}

