package com.spider;

import com.spider.analyse.StringUtil;
import com.spider.image.DownloadManager;
import com.spider.image.DownloadManagerImpl;
import com.spider.image.ImageDownloader;
import com.spider.net.SourceCode;

import java.io.IOException;

public class Examples {
    public static void main(String[] args) {
        //new Examples().testStringUtil();
          new Examples().testSourceCode();
    }
   public void testStringUtil(){
       String src="01234565656";
       String str="56";
       System.out.println(StringUtil.findStrIn(src, str, 2));
       System.out.println(StringUtil.backwardFindStrIn(src, str, 1));
   }
   public void testImageDownloader(){
       //
       try {
           new ImageDownloader().download(
                   "https://www.baidu.com/img/flexible/logo/pc/result.png"
                    ,"D:\\1.jpg");
       } catch (IOException e) {
           e.printStackTrace();
       }
   }
   public void testImagDownloadManager(){
       //new 一个dm，设置工作路径
       DownloadManager downloadManager = new DownloadManagerImpl("D:\\imageDownload\\");
       //接收图片
       downloadManager.accept("https://www.baidu.com/img/flexible/logo/pc/result.png"
       ,"1.jpg");
       //设置完成后自动关闭，而不是继续监听。在程序得末尾调用该方法
       downloadManager.waitUntilFinish(100);
   }
   public void testSourceCode(){
           SourceCode sourceCode=new SourceCode(){
               protected void init(){
               //添加初始cookie
                  // addCookie("");
               }
           };
           StringBuffer source = sourceCode.getSourceCodeInGet(
                   "https://www.baidu.com/"
                   ,true
           );
           System.out.println(source);
           System.out.println(sourceCode.getSourceCodeInPost(
               "https://www.baidu.com/", ""));
       System.out.println(sourceCode.getHtmlWithAutoRetry("https://www.baidu.com/"));
   }
}
