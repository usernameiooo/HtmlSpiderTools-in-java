package com.spider.util;

import java.io.File;

public class FileUtil {
    /**删除文件，并且删除由此导致的空目录*/
    public static void delete(String pathname){
        File file=new File(pathname);
        if(file.exists()){
            file.delete();
        }
        File dir=file.getParentFile();
        while (true){
            if(dir==null)break;
            String[] list = dir.list();
            if(list==null||list.length==0){
                dir.delete();
                dir=dir.getParentFile();
            }else {
                break;
            }
        }
    }
}
