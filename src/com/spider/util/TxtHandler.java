package com.spider.util;

import java.io.*;

/**读取\写入txt文件的工具*/
public class TxtHandler {
    public static String read(String pathname){
        if(pathname==null||pathname.isEmpty())return null;
        File file=new File(pathname);
        if(file.exists()&&file.isFile()){
            try {
                BufferedReader reader=new BufferedReader(new FileReader(file));
                String line;
                StringBuilder text= new StringBuilder();
                while ((line=reader.readLine())!=null){
                    text.append(line).append("\n");
                }
                reader.close();
                return text.toString();
            }  catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
    /**
     * @param append 是否是在原内容后续写，为否则会擦除原内容*/
    public static boolean write(String pathname,String content,boolean append){
        File file=new File(pathname);
        try {
        if(file.exists()){
            if(file.isDirectory())return false;
        }else {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        BufferedWriter writer=new BufferedWriter(new FileWriter(file,append));
        writer.write(content);
        writer.flush();
        writer.close();
        return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
