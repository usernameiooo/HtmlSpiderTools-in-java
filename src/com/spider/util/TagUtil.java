package com.spider.util;


import java.util.ArrayList;
import java.util.List;

public class TagUtil {
    String src;
    public TagUtil(String src){
        this.src=src;
        srcArray=src.toCharArray();
        findStringAreas();
    }
    char[] srcArray;
    static class StringArea{
        int start;
        int end;
        public StringArea(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }
    List<StringArea> stringAreas=new ArrayList<>();
    private void findStringAreas(){
        int index=0;
        boolean insideString=false;
        StringArea temp = null;
        while (index< srcArray.length){
            if(srcArray[index]=='"'){
                if(index-1>=0&&srcArray[index-1]!='\\'){
                    insideString=!insideString;
                    if(insideString)temp= new StringArea(index, -1);
                    else {
                        temp.end=index+1;
                        stringAreas.add(temp);
                    }
                }
            }
            index++;
        }
    }
    /**找到一对括号的结束括号的位置*/
    public int findMatchPair(int start){
        int index=start+1;
        int left=1;
        while(index< srcArray.length){
            char c=srcArray[index];
            if(c=='<')left++;
            else if(c=='>'&&!isInsideString(index))left--;
            if(left==0)return index;
            index++;
        }
        return -1;
    }
    /**判断某一位置是否在字符串中（被引号包起来）*/
    private boolean isInsideString(int index){
        for(StringArea area:stringAreas){
            if(index>=area.start&&index< area.end)return true;
        }
        return false;
    }
    /**找到下一个不是空白符的位置
     * @param start 开始位置*/
    public int findNoSpace(int start){
        start++;
        while (start< srcArray.length){
            if(srcArray[start]!=' ')return start;
            start++;
        }
        return -1;
    }
    /**往回找到下一个不是空白符的位置
     * @param end 结束位置*/
    public int findBackNoSpace(int end){
        end--;
        while (end>=0){
            if(srcArray[end]!=' ')return end;
            end--;
        }
        return -1;
    }
    /**往后寻找目标字符
     * */
    public int findNext(char target,int start,boolean outsideString){
        if (start<0)return -1;
        int index=start;
        while(index< srcArray.length){
            char c=srcArray[index];
            if(c==target&&(!outsideString||!isInsideString(index)))return index;
            index++;
        }
        return -1;
    }
    /**去除标签，只留下标签外的文本
     * 如<body>hello</body> -> hello*/
    public static String removeTags(String tagSrc,boolean ifSplit){
        if(tagSrc==null||tagSrc.equals(""))return null;
        TagUtil stringUtil=new TagUtil(tagSrc);
        String result="";
        int tagStart=0,tagEnd=0;
        while (true) {
            tagStart = stringUtil.findNext('<', tagEnd, true);
            if(tagStart==-1){
                if(ifSplit)result+="\n";
                result+=tagSrc.substring(tagEnd+1);
                break;
            }else {
                if(tagEnd+1<tagStart){
                    if(ifSplit)result+="\n";
                    result+=tagSrc.substring(tagEnd+1,tagStart);
                }
            }
            tagEnd=stringUtil.findNext('>',tagStart,true);
            if(tagEnd==-1){
                if(ifSplit)result+="\n";
                result+=tagSrc.substring(tagStart);
                break;
            }
        }
        return result;
    }
}
