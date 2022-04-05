package com.spider.net;

import com.spider.multi.AutoRetry;
import com.spider.multi.WatchedThread;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
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
	private void getHeaders(HttpURLConnection coon){
		Map<String, List<String>> headerFields = coon.getHeaderFields();
		Set<String> strings = headerFields.keySet();
		for(String key:strings){
			System.out.println(key+"="+headerFields.get(key));
		}
	}
	private void getRequestProperties(HttpURLConnection coon){
		Map<String, List<String>> requestProperties = coon.getRequestProperties();
		Set<String> strings = requestProperties.keySet();
		for(String key:strings){
			System.out.println(key+"="+requestProperties.get(key));
		}
	}
	boolean canJump;
	public StringBuffer getSourceCodeInGet(String url,boolean canJump) {
		try {
			HttpURLConnection httpUrlCon;
			httpUrlCon = getConnection(url);
			/*System.out.println("request=================");
			getRequestProperties(httpUrlCon);*/
			httpUrlCon.setDoOutput(false);
			httpUrlCon.setRequestMethod("GET");
			handleHeader(httpUrlCon);
			//System.out.println("response================");
			//getHeaders(httpUrlCon);
			int responseCode=httpUrlCon.getResponseCode();
			switch (responseCode){
				case 302: case 301:
					if(!canJump)return null;
					System.out.println("跳转");
					//跳转
					String newUrl=httpUrlCon.getHeaderField("Location");
					if(newUrl==null)newUrl=httpUrlCon.getHeaderField("location");
					newUrl=getFullUrl(url,newUrl);
					if(newUrl==null)return null;
					return getSourceCodeInGet(newUrl,canJump);
				case 404: case 403:return null;
				case 200: return getContent(httpUrlCon);
			}
		} catch (IOException e) {
			System.err.println(url);
			e.printStackTrace();
			//userAgent=(userAgent+1)%userAgents.length;
			//出现其他异常则返回
		}
		return null;
	}
	public String getFullUrl(String sourceUrl,String path){
		if(path.startsWith("http://")||path.startsWith("https://"))return path;
		try {
			int protocolIndex = sourceUrl.indexOf("//")+2;
			int siteIndex=sourceUrl.indexOf("/",protocolIndex);
			String site;
			if(siteIndex!=-1)site=sourceUrl.substring(0,siteIndex);
			else site=sourceUrl+"/";
			return site+path;
		}catch (Exception e){}
		return null;
	}
	public StringBuffer getSourceCodeInPost(String url,String data){
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
			switch (responseCode){
				case 302: case 301:
					//跳转
					String newUrl=httpUrlCon.getHeaderField("location");
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
	public StringBuffer getContent(HttpURLConnection connection) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), charset));
		String line;
		StringBuffer sourceCode=new StringBuffer();
		while ((line = reader.readLine()) != null) {
			sourceCode.append(line).append("\n");
		}
		reader.close();
		//if(tryTimes!=0) System.out.println("重试"+(tryTimes+1)+"次后找到文件");
		this.sourceCode=sourceCode;
		return sourceCode;
	}
	private void handleHeader(HttpURLConnection connection){
		addCookie(connection.getHeaderField("Set-Cookie"));

	}
	int userAgent=2;
	static String[] userAgents={"Chrome/71.0.3578.98 Safari/537.36",
			"Mozilla/4.0 compatible; MSIE 5.0;Windows NT;",
			"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36"};
	Proxy proxy=null;

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	public HttpURLConnection getConnection(String urlStr) {
		URL url;
		HttpURLConnection conn = null;
		try {
			url = new URL(urlStr);
			if(proxy==null)
				conn = (HttpURLConnection) url.openConnection();
			else conn=(HttpURLConnection) url.openConnection(proxy);
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
			conn.setReadTimeout(60*1000);
			conn.setConnectTimeout(60*1000);
			if(conn instanceof HttpsURLConnection){
				((HttpsURLConnection)conn).setSSLSocketFactory(getSSLFactory());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		setRequestHeader(conn,initHeader);
		return conn;
	}
	String initHeader=null;
	public void setRequestHeader(HttpURLConnection connection,String text){
		if(text==null)return;
		String[] lines = text.split("\n");
		for(String line:lines){
			if(line.startsWith(":"))line=line.substring(1);
			String[] split = line.split(":");
			if(split.length!=2)continue;
			connection.setRequestProperty(split[0].trim(),split[1].trim());
		}
	}

	public void setInitHeader(String initHeader) {
		this.initHeader = initHeader;
	}

	Map<String,String>cookieMap=new HashMap<>();
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
	public String getCookieString(){
		StringBuilder cookie= new StringBuilder();
		for(String key: cookieMap.keySet()){
			cookie.append(key).append("=").append(cookieMap.get(key)).append(";");
		}
		//System.out.println(cookie.toString());
		return cookie.toString();
	}
	private void init() {
		/*addCookie("Hm_lvt_d9357b0c463079ac0ee50ad30fd5f4d8=1629534247; fg=b; px=0; xs=2; so=0; ASPSESSIONIDQWTTSSBQ=OJPDNADABMFEFLKGPDFBPCHC; Hm_lpvt_d9357b0c463079ac0ee50ad30fd5f4d8=1629549425");
	    addCookie("_pk_ref.47.cafb=%5B%22%22%2C%22%22%2C1629709581%2C%22https%3A%2F%2Fclgou.cyou%2F%22%5D; _pk_id.47.cafb=60053296966e5672.1629709581.; _pk_ses.47.cafb=1; _ga=GA1.1.1896189009.1629709581; _ga_17KQ2CV4SK=GS1.1.1629709581.1.1.1629709672.0; session=eyJfZnJlc2giOmZhbHNlLCJfcGVybWFuZW50Ijp0cnVlLCJjc3JmX3Rva2VuIjp7IiBiIjoiT1dZNFl6QTVOV0U0TlRSaVpqQTFOamcxT0RNeU5UWTJOV1EwTm1RM01EWTBPVFptWkdRMVpRPT0ifX0.YSNlbA.g-X49LSa9nqdfp6IQUxkcNTVo1I");
	   addCookie("csrf_token=ImM5N2E5MDViY2Q3YTFmYWM5MzFiOTIzOGU0NTdkZWQ4ZWJiNTc3NzEi.YSSUvA.DyX7iWM7JHb3L2qtBN6Z74xnhnA");
	    addCookie("_pk_ref.44.19d5=%5B%22%22%2C%22%22%2C1629709592%2C%22https%3A%2F%2Fcilidi.cyou%2F%22%5D; _pk_id.44.19d5=d79a058e451098f4.1629709592.; _ga=GA1.1.447330394.1629709593; _ga_SG9D46XQHX=GS1.1.1629787324.2.0.1629787324.0; session=eyJfZnJlc2giOmZhbHNlLCJfcGVybWFuZW50Ijp0cnVlLCJjc3JmX3Rva2VuIjp7IiBiIjoiWXprM1lUa3dOV0pqWkRkaE1XWmhZemt6TVdJNU1qTTRaVFExTjJSbFpEaGxZbUkxTnpjM01RPT0ifX0.YSSXbQ.Is7MzpuWEuasZKn672JNyALNckU");
	   */
		/*addCookie("session=eyJfZnJlc2giOmZhbHNlLCJfcGVybWFuZW50Ijp0cnVlLCJjc3JmX3Rva2VuIjp7IiBiIjoiWXprM1lUa3dOV0pqWkRkaE1XWmhZemt6TVdJNU1qTTRaVFExTjJSbFpEaGxZbUkxTnpjM01RPT0ifX0.YSSbqA.TCLdKD-2D6aOOQMQWMQKwLzA9vI; Expires=Tue, 24-Aug-2021 08:11:36 GMT; HttpOnly; Path=/");
		 */
	}
	public void clearCookies(){
		cookieMap.clear();
	}
	public String getHtmlWithAutoRetry(String url,int time){
		AutoRetry<String> autoRetry=new AutoRetry<String>() {
			@Override
			public WatchedThread<String> getNewThread() {
				return new WatchedThread<String>() {
					@Override
					public void callBack(String result) {
					}
					@Override
					public String runTask() {
						StringBuffer sourceCodeInGet = getSourceCodeInGet(url,true);
						if(sourceCodeInGet==null)return null;
						return sourceCodeInGet.toString();
					}
				};
			}
		};
		autoRetry.setRetrySpace(retrySpace);
		autoRetry.setRetryTimes(time);
		return autoRetry.runForUsableResult();
	}
	int retryTime=3;
	int retrySpace=10000;
	public String getHtmlWithAutoRetry(String url){
		return getHtmlWithAutoRetry(url,retryTime);
	}

	public void setRetrySpace(int retrySpace) {
		this.retrySpace = retrySpace;
	}

	public void setRetryTime(int retryTime) {
		this.retryTime = retryTime;
	}
	public boolean setProxy(String proxy){
		int qutoIndex=proxy.indexOf(":");
		if(qutoIndex==-1)return false;
		String ip=proxy.substring(0,qutoIndex);
		String port_=proxy.substring(qutoIndex+1);
		int port;
		try {
			port = Integer.parseInt(port_);
		}catch (Exception e){
			return false;
		}
		setProxy(new Proxy(Proxy.Type.HTTP,
				new InetSocketAddress(ip,port)));
		return true;
	}
}
