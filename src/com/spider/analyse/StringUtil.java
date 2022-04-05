package com.spider.analyse;

import java.util.ArrayList;
import java.util.List;

public class StringUtil {

    public static String getBetween(String src, String start, String end) {
        int i = src.indexOf(start);
        if (i == -1) return null;
        int j = src.indexOf(end, i + start.length());
        if (j == -1) return null;
        return src.substring(i + start.length(), j);
    }

    public static List<String> getSpiltList(String src, String start, String end) {
        ArrayList<String> list = new ArrayList<>();
        while (src != null && !src.equals("")) {
            String cut = getBetween(src, start, end);
            if (cut == null) return list;
            list.add(cut);
            src = src.substring(src.indexOf(end,src.indexOf(start)+start.length()) + end.length());
        }
        return list;
    }

    public static String getAfter(String src, String spilt) {
        int i = src.indexOf(spilt);
        if (i == -1) return null;
        return src.substring(i + spilt.length());
    }

    public static String getBefore(String src, String spilt) {
        int i = src.indexOf(spilt);
        if (i == -1) return null;
        return src.substring(0, i);
    }
    public static boolean isEmpty(String src){
        return src==null||src.isEmpty();
    }
    public static boolean isEmpty(String... src){
        if(src==null)return true;
        for(String s:src){
            if(!s.isEmpty())return false;
        }
        return true;
    }
    /**逆转字符串*/
    public static String reverse(String src){
        if(isEmpty(src))return src;
        char[] chars = src.toCharArray();
        int l=chars.length;
        for(int i=0;i< l/2;i++){
            char temp=chars[i];
            chars[i]=chars[l-i-1];
            chars[l-i-1]=temp;
        }
        return new String(chars);
    }
    /**找到第几次出现的匹配串的下标
     * @param src 在src中寻找
     * @param str 需要寻找的字符串
     * @param time 需要寻找的str在src中是第几次出现
     * */
    public static int findStrIn(String src,String str,int time){
        if(time<=0)return -1;
        int index=-str.length();
        for(int i=1;i<=time;i++){
            index=src.indexOf(str,index+str.length());
            if(index==-1)return -1;
        }
        return index;
    }
    /**从后往前找到第几次出现的匹配串的下标
     * @param src 在src中寻找
     * @param str 需要寻找的字符串
     * @param time 需要寻找的str在src中是倒数第几次出现
     * */
    public static int backwardFindStrIn(String src,String str,int time){
        String rSrc = reverse(src);
        String rStr=reverse(str);
        //rStr在反转字符串rSrc中的下标等于原str的结尾到原src最后一个字符的距离
        int toEnd = findStrIn(rSrc, rStr, time);
        if(toEnd==-1)return -1;
        //src.length()-1-toEnd-(str.length()-1);
        return src.length()-toEnd-str.length();
    }
}
