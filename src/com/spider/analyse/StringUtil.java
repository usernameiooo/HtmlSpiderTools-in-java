package com.spider.analyse;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
            src = src.substring(src.indexOf(end) + end.length());
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

}
