package edu.bupt.Clat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

//import android.os.Looper;
import android.util.Log;

public class NetworkTool extends Thread {
	
	private static StringBuilder sb = null;
	
	/**
	* 获取网址内容
	* @param url
	* @return
	* @throws Exception
	*/
	public static String getContent() throws Exception{
		
//		new Thread(){
//			@Override
//			public void run(){  		
				
//				Looper.prepare(); 
				
				sb = new StringBuilder();
			    
			    HttpClient client = new DefaultHttpClient();
			    HttpParams httpParams = client.getParams();
			    //设置网络超时参数
			    HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
			    HttpConnectionParams.setSoTimeout(httpParams, 5000);
			    HttpResponse response = null;
			    HttpEntity entity = null;
			    Log.d("NTool","upy0");
				try {
					response = client.execute(new HttpGet(Config.UPDATE_SERVER
					        + Config.UPDATE_VERJSON));
				} catch (ClientProtocolException e) {
					Log.d("NTool","upy1"+e.toString());
					e.printStackTrace();
				} catch (IOException e) {
					Log.d("NTool","upy2"+e.toString());
					e.printStackTrace();
				}
				Log.d("NTool","upy3");
			    entity = response.getEntity();
			    Log.d("NTool","upy4");
			    if (entity != null) {
			        BufferedReader reader = null;
					try {
						reader = new BufferedReader(new InputStreamReader(entity.getContent(), "UTF-8"), 8192);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					Log.d("NTool","upy5"+sb.toString());
			        
			        String line = null;
			        try {
						while ((line = reader.readLine())!= null){
						    sb.append(line + "\n");
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			        Log.d("NTool","upy6"+sb.toString());
			        try {
						reader.close();
					} catch (IOException e) {
						Log.d("NTool","upy6.1"+sb.toString());
						Log.d("NTool","upy6.2"+e.toString());
						e.printStackTrace();
					}
			    }
			    Log.d("NTool","upy6.3"+sb.toString());
//			    Looper.loop(); 
			    Log.d("NTool","upy6.4"+sb.toString());
//			}
			
//		}.start();
		
		Log.d("NTool","upy7"+sb.toString());
	    
	    return sb.toString();
	} 
}