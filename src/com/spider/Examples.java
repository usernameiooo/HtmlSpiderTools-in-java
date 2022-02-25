package com.spider;

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
