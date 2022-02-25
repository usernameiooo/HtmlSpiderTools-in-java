package com.spider.image;


import com.spider.util.FileUtil;

import java.io.File;
import java.util.*;

/**多线程下载图片的管理器
 * 用accept()方法接收要下载的图片*/
public class DownloadManager {
    String name="downloadManager";

    public void setName(String name) {
        this.name = name;
    }
    Queue<Task> tasks=new LinkedList<Task>();
    int accept=0;//接收的任务数
    int waiting=0;//等待中的任务数
    int running=0;//进行中的任务数
    int finish=0;//完成的任务数
    int failed=0;//失败的任务数
    String baseDir;//下载的根目录
    int threadCount=10;
    public static int MaxAliveTime=60000;//任务超时时间
    /**任务*/
    public class Task{
        String url;
        String pathname;
        public Task(String url, String pathname) {
            this.url = url;
            this.pathname = pathname;
        }
    }
    /**执行任务的线程*/
    public class DownloadThread extends Thread{
        int id;
        long startTime;
        Task task;
        boolean giveUp=false;
        public DownloadThread(int id) {this.id=id; }
        public void setTask(Task task) {
            this.task = task;
        }
        @Override
        public void run() {
            this.startTime=System.currentTimeMillis();//线程开始
            ImageDownloader imageDownloader=new ImageDownloader();
            try {
                imageDownloader.download(task.url,task.pathname);
                System.out.println("图片 "+task.url+" 下载成功 "+task.pathname);
            } catch (Exception e) {
                System.out.println("图片 "+task.url+" 下载失败");
                FileUtil.delete(task.pathname);
                failed++;
            }
            if(!giveUp){
            threads[id]=null;//在线程池标记已完成，对应位置置空
            finish++;
            running--;}
        }
    }
    /**线程池*/
    DownloadThread[] threads;
    /**从线程池取一个空位，放入一个新线程并返回*/
    public DownloadThread getFreeThread(){
        for(int i=0;i<threads.length;i++){
            if(threads[i]==null){
                DownloadThread thread=new DownloadThread(i);
                threads[i]=thread;
                return thread;
            }
        }
        return null;
    }
    /**任务分配线程。从队列中取任务，新建任务线程来完成*/
    Thread taskThread =new Thread(){
        @Override
        public void run() {
            while (true){
              if(!tasks.isEmpty()){
                  Task task=tasks.poll();
                  if(task==null)continue;
                DownloadThread t=getFreeThread();
               if(t!=null){
                t.setTask(task);
                t.start();
                waiting--;
                running++;
               }else {
                tasks.offer(task);
                   try {
                       Thread.sleep(100);
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               }
              }else {
                  try {
                      Thread.sleep(500);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }
              if(shutdownOnFinish&&hasFinish())break;
            }
        }
    };

    /**监控线程。监控每个任务线程是否超时，清理超时任务*/
    Thread watchThread=new Thread(){
        @Override
        public void run() {
            while (true){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                long curr=System.currentTimeMillis();
                for (int i=0;i<threads.length;i++){
                     DownloadThread thread=threads[i];
                    if (thread!=null&&curr-thread.startTime>MaxAliveTime){
                    threads[i].giveUp =true;
                        threads[i]=null;
                    running--;
                    failed++;}
                }
                System.out.println("\33[42;1m"+name+"> "+getStatInfo()+"\n\33[0m");
                //结束该线程
                if(shutdownOnFinish&&hasFinish())break;
            }
        }
    };
    private void init(){
        if(baseDir!=null&&!baseDir.equals("")){
            File dir=new File(baseDir);
            if(!dir.exists())dir.mkdirs();
        }
        threads=new DownloadThread[threadCount];
        taskThread.start();
        watchThread.start();
    }
    public DownloadManager(String baseDir){
        this.baseDir=baseDir;
        init();
    }
    public DownloadManager(String baseDir,int threadCount){
        this.baseDir=baseDir;
        this.threadCount=threadCount;
        init();
    }
    Set<String> acceptedUrls=new HashSet<>();//已经接收的下载链接
    /**接收一个任务，若不重复则直接放进队列
     * @param pathname 相对于@link baseDir的目录*/
    public void accept(String url,String pathname){
       if(!acceptedUrls.contains(url)){
        if(baseDir!=null)pathname=baseDir+pathname;
        pathname=formatPathName(pathname);
          // System.out.println(url+"       ->"+pathname);
        Task task=new Task(url,pathname);
        tasks.offer(task);
        acceptedUrls.add(url);
        accept++;
        waiting++;
       }
    }
    enum NameMode{
        UrlAsName,NumberAsName
    }
    NameMode nameMode=NameMode.UrlAsName;
    /**未指明图片的文件名时的命名方式*/
    public void setNameMode(NameMode nameMode) {
        this.nameMode = nameMode;
    }

    int code=0;
    public void accept(Map<String,String> images){
        if(images==null)return;
        Set<String> strings = images.keySet();
        for(String str:strings){
            String name=images.get(str);
            if(name==null||name.equals("")){
            if(nameMode==NameMode.NumberAsName){
                name=code+".jpg";
                code++;
            }else {
                name=getPathNameFromUrl(str);
            }
            }
            accept(str,name);
        }
    }
    /**接收一系列下载任务，下载到同一文件夹内，文件依次编号*/
    public void accept(List<String> urls,String folder){
        if(urls==null||urls.size()==0)return;
        if(folder==null||folder.isEmpty())return;
        folder=folder.trim();
        if(!folder.endsWith("\\"))folder=folder+"\\";
        int index=1;
        for(String url:urls){
            accept(url,folder+index+".jpg");
            index++;
        }
    }
    public final static String[] NoIncludeInURLFile={
            "?","、","*","\"","<",">","\\|","\\",":"
    };
    /**从url生成文件路径*/
    public static String getPathNameFromUrl(String url){
        String patName=url.replace("//","/");
        for(String str:NoIncludeInURLFile){
            patName=patName.replace(str,"");
        }
        patName=patName.replace(":","");
        return patName;
    }
    public final static String[] NoIncludeInPath={
            "?","、","*","\"","<",">","\\|","/"
    };
    /**去除路径中的特殊字符*/
    public static String formatPathName(String pathname){
        pathname=pathname.replace("\\\\","\\");
        for(String str:NoIncludeInPath){
            pathname=pathname.replace(str,"");
        }
        int spanIndex=pathname.lastIndexOf(":");
        //如果有非盘符后的冒号，去掉
        if(spanIndex!=-1&&spanIndex!=1){
            pathname=pathname.substring(0,spanIndex+1)+
                    pathname.substring(spanIndex+1).replace(":","");
        }
        return pathname;
    }
    /**获取统计信息*/
    public String getStatInfo(){
        return "total:"+accept+" waiting:"+waiting+" running:"
                +running+" finish:"+finish+" failed:"+failed;
    }
    boolean shutdownOnFinish=false;
    /**设置在任务结束时关闭
     *
     * */
    public void setShutDownOnFinish(){
        shutdownOnFinish=true;
    }
    /**是否已经结束了接收的所有任务*/
    public boolean hasFinish(){
        return running<=0&&waiting<=0;
    }
}

