package com.spider.image;

import java.util.List;
import java.util.Map;

public interface DownloadManager {
    public void accept(String url,String pathname);
    public void accept(Map<String,String> images);
    public void accept(List<String> urls, String folder);
    public String getStatInfo();
    public void waitUntilFinish(int printInfoSpace);
}
