package com.spider.image;

import javax.net.ssl.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class ImageDownloader {
    public boolean isEmpty(String s){
        if(s==null||s.equals(""))return true;
        return false;
    }
    public void download(String imageUrl,String pathName) throws IOException {
        if(isEmpty(imageUrl)||isEmpty(pathName))return;
        File file=new File(pathName);
        File dir = file.getParentFile();
        if(!dir.exists()) dir.mkdirs();
        //System.out.println(file.getAbsolutePath());
        if(!file.exists())file.createNewFile();
        downImage1(imageUrl,file);

    }
    public void downImage1(String imgSrc,File file) throws IOException {
        URL url = new URL(imgSrc);
        URLConnection uc = url.openConnection();
        setHeader((HttpURLConnection) uc);
        String location = uc.getHeaderField("Location");
        if(location!=null){
            downImage1(location,file);
            return;
        }
        BufferedOutputStream bOutput=new BufferedOutputStream(new FileOutputStream(file));
        BufferedInputStream bInput=new BufferedInputStream(uc.getInputStream());
        byte[] bytes=new byte[1024];
        int length=0;
        while ((length=bInput.read(bytes))!=-1) {
            bOutput.write(bytes,0,length);
        }
        bOutput.flush();
        bOutput.close();
        bInput.close();
    }
    private void setHeader(HttpURLConnection conn) throws ProtocolException {
        conn.setRequestProperty("user-agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        conn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        conn.setRequestProperty("accept-language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        //conn.setRequestProperty("accept-encoding", "gzip, deflate, br");
        conn.setRequestProperty("cache-control","max-age=0");
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("upgrade-insecure-requests", "1");
        conn.setRequestMethod("GET");
        //	conn.setSSLSocketFactory(getSSLFactory());
        conn.setInstanceFollowRedirects(true);
        conn.setDoOutput(true);
        conn.setDoInput(true);
        // conn.setInstanceFollowRedirects(false);
        if(conn instanceof HttpsURLConnection){
            ((HttpsURLConnection)conn).setSSLSocketFactory(getSSLFactory());
        }
    }
    public SSLSocketFactory getSSLFactory(){
        try {
            SSLContext sslContext=SSLContext.getInstance("SSL");
            TrustManager[] tm={new X509TrustManager(){
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            //初始化
            sslContext.init(null, tm, new java.security.SecureRandom());
            //获取SSLSocketFactory对象
            SSLSocketFactory ssf=sslContext.getSocketFactory();
            return ssf;
        }catch ( NoSuchAlgorithmException | KeyManagementException e){

        }
        return null;
    }
}
