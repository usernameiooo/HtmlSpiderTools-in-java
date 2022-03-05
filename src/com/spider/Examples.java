package com.spider;

import com.spider.image.DownloadManager;
import com.spider.image.ImageDownloader;
import com.spider.net.SourceCode;

import java.io.IOException;

public class Examples {
   public void testStringUtil(){
   //

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
       DownloadManager downloadManager = new DownloadManager("D:\\imageDownload\\");
       //接收图片
       downloadManager.accept("https://www.baidu.com/img/flexible/logo/pc/result.png"
       ,"1.jpg");
       //设置完成后自动关闭，而不是继续监听。在程序得末尾调用该方法
       downloadManager.setShutDownOnFinish();
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
   }
}
