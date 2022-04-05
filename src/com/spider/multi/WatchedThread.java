package com.spider.multi;

public abstract class WatchedThread<T> extends Thread{
    volatile boolean isOK=false;
    int timeOut=60000;
    long startTime;
    volatile boolean isGivenUp=false;
    T result;
    public WatchedThread(){
    }
    public boolean isOK() {
        return isOK;
    }
    @Override
    public synchronized void start() {
        super.start();
        startTime=System.currentTimeMillis();
    }
    public boolean isTimeOut(){
        return System.currentTimeMillis()-startTime>=timeOut;
    }
    public abstract T runTask();
    public abstract void callBack(T result);
    @Override
    public  void run(){
        T t = runTask();
        result=t;
        if(!isGivenUp) {
            callBack(t);
        }
        isOK=true;
    }
    private void  sleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**由其他类调用,造成其所在运行线程阻塞
     * @return 不超时返回true，超时返回false*/
    public boolean waitUntilOK(){
        while(!isOK){
            sleep(10);
            if(isTimeOut()){
                isGivenUp=true;
                this.interrupt();
                return false;
            }
        }
        return true;
    }
    public T getResult() {
        return result;
    }
}
