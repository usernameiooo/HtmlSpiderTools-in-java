package com.spider.image;


import com.spider.multi.WatchedThread;
import com.spider.util.FileUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class DownloadManagerInPool implements DownloadManager{
    String name="DM";
    int retryTime=1;
    /**@param baseDir 以文件分隔符作为结尾
     * */
    public DownloadManagerInPool(String baseDir, int coreThreads, int maxThreads) {
        if(!baseDir.endsWith("/")&&!baseDir.endsWith("\\"))
            baseDir=baseDir+"\\";
        this.baseDir = baseDir;
        pool=new ThreadPoolExecutor(coreThreads,maxThreads,60, TimeUnit.SECONDS,tasks,factory);
    }
    public DownloadManagerInPool(String baseDir){
        this(baseDir,20,50);
    }
    public void setName(String name) {
        this.name = name;
    }

    public void setRetryTime(int retryTime) {
        this.retryTime = retryTime;
    }

    BlockingQueue<Runnable> tasks=new LinkedBlockingQueue<>();
    ThreadFactory factory=new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r);
        }
    };
    ThreadPoolExecutor pool;
    String baseDir;//下载的根目录
    /**@param pathname 不能以文件路径分隔符开头
     * */
    public void accept(String url,String pathname){
        pathname=pathname.trim();
        if(baseDir!=null)pathname=baseDir+pathname;
        pathname=formatPathName(pathname);
        // System.out.println(url+"       ->"+pathname);
        ImageDownloadRunnable runnable=new ImageDownloadRunnable(url, pathname);
        runnable.setMaxTryTime(retryTime);
        pool.execute(runnable);
    }

    @Override
    public void accept(Map<String, String> images) {
        if(images==null)return;
        Set<String> strings = images.keySet();
        for(String str:strings){
            String name=images.get(str);
            accept(str,name);
        }
    }

    /**接收一系列下载任务，下载到同一文件夹内，文件依次编号*/
    public void accept(List<String> urls, String folder){
        if(urls==null||urls.size()==0)return;
        if(folder==null||folder.isEmpty())return;
        folder=folder.trim();
        if(!folder.endsWith("\\")&&!folder.endsWith("/"))folder=folder+"\\";
        int index=1;
        for(String url:urls){
            accept(url,folder+index+getSuffix(url));
            index++;
        }
    }
    /**获得下载的图片的文件后缀*/
    public String getSuffix(String url){
        int dot = url.lastIndexOf(".");
        if(dot==-1)return ".jpg";
        int separate = url.lastIndexOf("/");
        if(dot>separate)return url.substring(dot);
        return ".jpg";
    }
    int err;
    int failed;
    public String getStatInfo(){
        return "\33[42;1m"+name+"> "+pool.getCompletedTaskCount()+"/"+pool.getTaskCount()
                +" "+(pool.getTaskCount()-pool.getCompletedTaskCount()) + "-"+pool.getActiveCount()
                +" err "+err
                +" failed "+failed
                +"\n\33[0m";
    }
    public synchronized void notifyErr(){
        err++;
    }
    public synchronized void notifyFailed(){failed++;}
    public String getBaseDir() {
        return baseDir;
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
            pathname=pathname.substring(0,2)+
                    (pathname.substring(2).replace(":",""));
        }
        return pathname;
    }
    public void shutDown(){
        pool.shutdown();
    }
    boolean showDebug=false;

    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }
    public void waitUntilFinish(int printInfoSpace){
        int sumTime=0;
        while (pool.getActiveCount()!=0){
            try {
                Thread.sleep(100);
                if(printInfoSpace>0&&(sumTime+=100)>=printInfoSpace){
                    System.out.print(getStatInfo());
                    sumTime-=printInfoSpace;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    class ImageDownloadRunnable implements Runnable{
        String url;
        String pathname;
        int tryTime=0;
        int maxTryTime=1;

        public void setMaxTryTime(int maxTryTime) {
            this.maxTryTime = maxTryTime;
        }

        public ImageDownloadRunnable(String url, String pathname) {
            this.url = url;
            this.pathname = pathname;
        }
        private void runInWatchedThread(){
            ImageDownloader imageDownloader=new ImageDownloader();
            WatchedThread<Boolean> thread=new WatchedThread<Boolean>() {
                @Override
                public Boolean runTask() {
                    try {
                        tryTime++;
                        imageDownloader.download(url,pathname);
                        System.out.println("图片 "+url+" 下载成功 "+pathname +" 尝试次数="+tryTime);
                        return true;
                    } catch (Exception e) {
                        if(showDebug)e.printStackTrace();
                        // System.err.println("图片 "+url+" 下载失败 "+pathname);
                        FileUtil.delete(pathname);
                        return false;
                    }
                }
                @Override
                public void callBack(Boolean result) {

                }
            };
            thread.start();
            boolean inTime = thread.waitUntilOK();
            if(!inTime||!thread.getResult()){
                notifyErr();
                if(tryTime<maxTryTime){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runInWatchedThread();
                }
                else {
                    notifyFailed();
                    System.err.println("图片 "+url+" 下载失败 "+pathname);
                }
            }
        }
        @Override
        public void run() {
            runInWatchedThread();
        }
    }
}
