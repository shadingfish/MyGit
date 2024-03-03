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
