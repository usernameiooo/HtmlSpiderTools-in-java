package com.spider.util;

import java.util.ArrayList;
import java.util.List;

/**字典
 * 读取某个txt文件的内容，以空格或换行分割得到若干词，作为搜索的关键词*/
public class Dictionary {
    final List<String> words=new ArrayList<>();
    /**从文本文件读取*/
   public void readFrom(String pathname){
       String text = TxtHandler.read(pathname);
       if(text==null||text.isEmpty())return;
       String[] split = text.split(" |\n");
       for(String s:split){
           if(s==null||s.isEmpty())continue;
           if(!words.contains(s))
           words.add(s);
       }
   }
    public List<String> getWords() {
        return words;
    }
    public String getWordAt(int index){
       if(index<=0||index>=words.size())return null;
       return words.get(index);
    }
    /**保存到文本文件*/
    public void save(String pathname){
       StringBuilder content= new StringBuilder();
       for(int i=0;i<words.size();i++){
           content.append(words.get(i)).append(" ");
           if((i+1)%10==0) content.append("\n");
       }
       TxtHandler.write(pathname, content.toString(),false);
    }
}
