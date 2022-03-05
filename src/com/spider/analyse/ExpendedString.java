package com.spider.analyse;

import java.util.List;

public class ExpendedString {
    public ExpendedString(String src){
        this.src=src;
    }
    String src;
    public ExpendedString getBetween(String start, String end) {
        return new ExpendedString(StringUtil.getBetween(src,start,end));
    }
    public List<String> getSpiltListValues(String start, String end) {
        return StringUtil.getSpiltList(src,start,end);
    }

    public ExpendedString getAfter(String spilt) {
       return new ExpendedString(StringUtil.getAfter(src, spilt));
    }

    public ExpendedString getBefore( String spilt) {
        return new ExpendedString(StringUtil.getBefore(src,spilt));
    }

    public String toString() {
        return src;
    }
    public String getValue() {
        return src;
    }

}
