package com.spider.multi;

import com.sun.istack.internal.NotNull;

public abstract class AutoRetry<T>{
    int retryTimes=3;//尝试的最大次数
    boolean retryOnNullResult=true;
    int retrySpace=5000;//重试的时间间隔
    protected WatchedThread<T> thread;
    public T runForUsableResult(){
        boolean shouldRetry=false;
        int time=0;
        do {
            thread=getNewThread();
            if(thread==null)return null;
            thread.start();
            boolean b=thread.waitUntilOK();//是否不超时
            time++;
            shouldRetry=(!b||(retryOnNullResult&&thread.getResult()==null))&&time<retryTimes;
            if(shouldRetry)sleep(retrySpace);
        }while (shouldRetry);
        return thread.getResult();
    }
    /**产生可运行的线程*/
    public abstract @NotNull WatchedThread<T> getNewThread();
    private void sleep(int time){
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public void setRetryTimes(int retryTimes) {
        this.retryTimes = retryTimes;
    }

    public boolean isRetryOnNullResult() {
        return retryOnNullResult;
    }

    public void setRetryOnNullResult(boolean retryOnNullResult) {
        this.retryOnNullResult = retryOnNullResult;
    }

    public int getRetrySpace() {
        return retrySpace;
    }

    public void setRetrySpace(int retrySpace) {
        this.retrySpace = retrySpace;
    }
}
