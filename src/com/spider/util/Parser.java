package com.spider.util;



import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**转换器*/
public class Parser {
    public boolean parseBoolean(String src,boolean def){
        try{
            return Boolean.parseBoolean(src);
        }catch (Exception e){
            return def;
        }
    }
    private boolean isHex(char c){
        return c>='0'&&c<='9'||c >= 'a' && c <= 'f' || c >= 'A' && c <= 'F';
    }
    private boolean isLetter(char c){
        return c>='a'&&c<='z'||c>='A'&&c<='Z';
    }
    private boolean isNumber(char c){
        return c>='0'&&c<='9'||c=='.';
    }
    final char[] sizeUnits={'K','k','M','m','G','g','T','t','E','e'};
    private boolean isSizeUnit(char c){
        for (char u:sizeUnits){
            if(c==u)return true;
        }
        return false;
    }
    public String parseSize(String src){
        try {
        src=src.replace(" ","");
        int numberStart=-1;
        String size="";
        for(int i=0;i<src.length();i++){
            char c=src.charAt(i);
            if(isNumber(c)){
                if(numberStart==-1)numberStart=i;
            }else if(isSizeUnit(c)){
                if(numberStart!=-1)size=src.substring(numberStart,i+1);
            }else if((c=='B'||c=='b')&&!size.equals("")){
                size+=c;break;
            }else {
                numberStart=-1;
            }
        }
        return size.equals("")?"0KB":size;
        }catch (Exception e){
            return "0KB";
        }
    }
    public String getString(String src){
        if(src==null)return null;
        return src.trim();
    }
    static final List<SimpleDateFormat> dateFormats=new ArrayList<>();
    public static void addDateFormat(String format){
        try {
        SimpleDateFormat smf=new SimpleDateFormat(format,Locale.UK);
        dateFormats.add(smf);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    static {
        /*日期格式*/
        addDateFormat("yyyy/MM/dd HH:mm:ss");
        addDateFormat("yyyy/MM/dd HH:mm");
        addDateFormat("yyyy/MM/dd");
        addDateFormat("yy/MM/dd HH:mm");
        addDateFormat("yy/MM/dd HH:mm:ss");
        addDateFormat("yyyy-MM-dd HH:mm:ss");
        addDateFormat("yyyy-MM-dd HH:mm");
        addDateFormat("yyyy-MM-dd");
        addDateFormat("E, dd MMM yyyy HH:mm:ss Z");
    }
    public Timestamp parseTime(String src){
        if(src==null)return new Timestamp(0);
        src=src.trim();
        if(src.isEmpty())return new Timestamp(0);
        for(SimpleDateFormat smf:dateFormats){
            try {
                Date date=smf.parse(src);
                return new Timestamp(date.getTime());
            } catch (ParseException e) {}
        }
        return new Timestamp(0);
    }
}
