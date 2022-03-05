package com.spider.net;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SourceCode {
	StringBuffer sourceCode = null;

	public SourceCode() {
	    init();
	}

	/**通过get方法获取页面文本内容
	 * @param url 页面链接
	 * @param canJump 是否可以重定向*/
	public StringBuffer getSourceCodeInGet(String url,boolean canJump) {
		try {
			HttpURLConnection httpUrlCon;
			httpUrlCon = getConnection(url);
			handleHeader(httpUrlCon);
			//getHeaders(httpUrlCon);
			int responseCode=httpUrlCon.getResponseCode();
			switch (responseCode){
				case 302: case 301:
					if(!canJump)return null;
					System.out.println("跳转");
					//跳转
					String newUrl=httpUrlCon.getHeaderField("Location");
			        if(newUrl==null)newUrl=httpUrlCon.getHeaderField("Location");
					return getSourceCodeInGet(newUrl,canJump);
				case 404: case 403:
					System.err.println("403或404错误");
					return null;
				case 200: return getContent(httpUrlCon);
			}
		} catch (IOException e) {
			e.printStackTrace();
			userAgent=(userAgent+1)%userAgents.length;
		    //出现其他异常则返回
		}
		return null;
	}
	/**发送POST请求
	 * @param url 请求的url
	 * @param data 携带的表单数据的格式为name=value&name=value...,如同GET请求url中？后参数串
	 * */
	public StringBuffer getSourceCodeInPost(String url,String data){
		StringBuffer sourceCode = new StringBuffer();
		HttpURLConnection httpUrlCon;
		httpUrlCon = getConnection(url);
		PrintWriter printWriter = null;
		//关键代码 application/x-www-form-urlencoded
		try {
			httpUrlCon.setRequestMethod("POST");
			httpUrlCon.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
			//输出表单数据
			printWriter = new PrintWriter(httpUrlCon.getOutputStream());
			printWriter.write(data);
			printWriter.flush();
		    printWriter.close();
		    //处理响应头
            handleHeader(httpUrlCon);
			int responseCode=httpUrlCon.getResponseCode();
		//	System.out.println(responseCode);
			switch (responseCode){
				case 302: case 301:
					//跳转
					String newUrl=httpUrlCon.getHeaderField("Location");
					if(newUrl==null)newUrl=httpUrlCon.getHeaderField("Location");
					getSourceCodeInGet(newUrl,true);
					break;
				case 404: case 403:return null;
				case 200: return getContent(httpUrlCon);
			}
		    //获取响应内容
			//return getContent(httpUrlCon);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	Charset charset=StandardCharsets.UTF_8;
	/**读取响应的正文内容*/
    public StringBuffer getContent(HttpURLConnection connection) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset));
		String line;
		StringBuffer sourceCode=new StringBuffer();
		while ((line = reader.readLine()) != null) {
			sourceCode.append(line).append("\n");
		}
		reader.close();
		this.sourceCode=sourceCode;
		return sourceCode;
	}
	/**对响应头的处理*/
	private void handleHeader(HttpURLConnection connection){
		addCookie(connection.getHeaderField("Set-Cookie"));
	}
	int userAgent=2;
	/**客户端列表*/
	static String[] userAgents={"Chrome/71.0.3578.98 Safari/537.36",
			"Mozilla/4.0 compatible; MSIE 5.0;Windows NT;",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36"};
	/**打开url，获取连接，设置请求头*/
	public HttpsURLConnection getConnection(String urlStr) {
		URL url;
		HttpsURLConnection conn = null;
		try {
			url = new URL(urlStr);
			conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestProperty("user-agent",userAgents[userAgent]);
			conn.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
			conn.setRequestProperty("accept-language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			//conn.setRequestProperty("accept-encoding", "gzip, deflate, br");
			conn.setRequestProperty("cache-control","max-age=0");
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("cookie",getCookieString());
			conn.setRequestProperty("upgrade-insecure-requests", "1");
			conn.setRequestMethod("GET");
		//	conn.setSSLSocketFactory(getSSLFactory());
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setInstanceFollowRedirects(false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return conn;
	}
	/**Cookie列表*/
	Map<String,String>cookieMap=new HashMap<>();
	/**添加cookie到cookie列表
	 * @param set_cookie 从响应头获取的set_cookie字段,或浏览器请求头发送的cookie字段*/
	public void addCookie(String set_cookie){
		if(set_cookie==null)return;
		 String[] cookies=set_cookie.split(";");
	     for(String c:cookies){
	     	if(c.contains("=")) {
				String[] split = c.split("=");
				if(!split[0].trim().equals("path"))
			    cookieMap.put(split[0].trim(),split[1].trim());
	     	}
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
	private String getCookieString(){
		StringBuilder cookie= new StringBuilder();
		for(String key: cookieMap.keySet()){
			cookie.append(key).append("=").append(cookieMap.get(key)).append(";");
		}
		return cookie.toString();
	}
	/**打印响应头，用于失败时分析*/
	private void printHeaders(HttpURLConnection coon){
		Map<String, List<String>> headerFields = coon.getHeaderFields();
		Set<String> strings = headerFields.keySet();
		System.out.println("header==================begin");
		for(String key:strings){
			System.out.println(key+"="+headerFields.get(key));
		}
		System.out.println("header==================end");
	}
	protected void init() {

	}
}
