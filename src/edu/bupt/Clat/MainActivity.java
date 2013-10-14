package edu.bupt.Clat;

//import org.update.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
//import java.util.ArrayList; 
//import java.util.HashMap; 
//import java.util.Iterator; 
//import java.util.List; 
//import java.util.Map; 
import java.util.Date;

import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
//import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
//import android.app.ListActivity;
import android.app.ProgressDialog;
//import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
//import android.content.IntentFilter;
//import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
//import android.widget.ImageView;
//import android.widget.SimpleAdapter;
//import android.widget.ListView;
//import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slipbtn.SlipButton;
import org.slipbtn.SlipButton.OnChangedListener;
//import org.update.UpdateActivity;




public class MainActivity extends Activity {
	TextView ClatAddr, WiFiStatus, LastMessage, BinaryStatus, IPv4Address, IPv6Address, ClatStatus, Stdout, Stderr;
	TextView ClatSwitch, date_TextView;
	
//	private String DefaultRoute = null;
//	private String wifiinfo;
//	protected static String OriginDefaultRoute;
	protected static String ClatSubfix = null;
	protected static String WiFiMacAddr = null;
	protected static String ClatIPv6Addr = null;
	
	public static int flag = 0;

	private SlipButton mSlipButton = null;
	
//	public static String update_path = "/data/local/tmp/";
	public static String update_path = Environment.getExternalStorageDirectory().toString();
	
	public ProgressDialog pBar;
//	private Handler handler = new Handler();
	
/*	private void UpdateText() {
//		ClatAddr.setText(Tethering.InterfaceName());
		wifiinfo = new String(ConnectivityReceiver.getWiFiStatus());
		
		if(ConnectivityReceiver.getWiFiStatus().equalsIgnoreCase("CONNECTED")){
			wifiinfo = new String("已连接");
		}
		
		ClatAddr.setText(ClatIPv6Addr);
		WiFiStatus.setText(wifiinfo);
		IPv6Address.setText(ConnectivityReceiver.getWiFiIPv6Address());
		IPv4Address.setText(ConnectivityReceiver.getWiFiIPv4Address());
		ClatStatus.setText(Clat.getClatInterface());
	}*/
	
/*	private BroadcastReceiver mConnectionChanges = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(ConnectivityReceiver.ACTION_CONNECTIVITY_CHANGE)) {
				String message = intent.getStringExtra("message");
			    LastMessage.setText(message);
//			    UpdateText();
			} else if(intent.getAction().equals(InstallBinary.ACTION_INSTALL_BINARY)) {
//				String message = intent.getStringExtra("message");
//				BinaryStatus.setText(message);
			} else if(intent.getAction().equals(RunAsRoot.ACTION_ROOTSCRIPT_DONE)) {
				String StageName = intent.getStringExtra(RunAsRoot.EXTRA_STAGE_NAME);
//				Stdout.setText(RunAsRoot.get_stdout(StageName));
//				Stderr.setText(RunAsRoot.get_stderr(StageName));
				LastMessage.setText("Stage Script "+StageName+" completed");
			}
		}
	};*/
		
//	@SuppressLint(R.string.newapi)
	@Override
    public void onCreate(Bundle savedInstanceState) {    	
    	
 //   	int flag = 0;
    	
        super.onCreate(savedInstanceState);    
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题栏
        
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
        .detectDiskReads()
        .detectDiskWrites()
        .detectAll()   // or .detectAll() for all detectable problems
        .penaltyLog()
        .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
        .detectLeakedSqlLiteObjects()
        .detectLeakedClosableObjects()
        .penaltyLog()
        .penaltyDeath()
        .build());
        
//        setContentView(R.layout.activity_main);                
        setContentView(R.layout.activity_main);
       
		InstallBinary install = new InstallBinary(this);
		install.go();	
		
//		OriginDefaultRoute = null;
		
		Log.d("mac","upy01"+WiFiMacAddr);
				
		WiFiMacAddr = getLocalMacAddress();
		Log.d("mac","upy02"+WiFiMacAddr+"upy03"+ClatSubfix);
		if(WiFiMacAddr != null)
			ClatSubfix = MacToIPv6(WiFiMacAddr);
		
		Log.d("mac","upy04"+ClatSubfix);
		
		File system_bin_su = new File("/system/bin/su");
		if(!system_bin_su.exists()) {
			flag = 1;
			File system_xbin_su = new File("/system/xbin/su");
			if(!system_xbin_su.exists()) {
				LastMessage.setText("No /system/bin/su or /system/xbin/su found");
				flag = 2;
			}			
		}
				
		try {
			ConnectivityReceiver.rescanNetworkStatus(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		if(Clat.ClatRunning())
		
		Log.d("mac","upy05"+ClatIPv6Addr);
/*		
		try {
			DefaultRoute = RunAsRoot.execCommand("ip route |grep default |grep "+ConnectivityReceiver.WiFiInterfaceName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
/*		Log.d("mac","upy06"+DefaultRoute+"upy07"+OriginRoute);
		if(DefaultRoute != null)
			OriginRoute = new String(DefaultRoute);
				
		Log.d("mac","upy08"+DefaultRoute+"upy09"+OriginRoute);
		Log.d("mac","upy10"+ConnectivityReceiver.aInfo+"upy11"+ClatIPv6Addr);
		Log.d("mac","upy10.1"+ConnectivityReceiver.getWiFiIPv6Address()+"upy11.1"+ClatSubfix);
 */			
		if(ConnectivityReceiver.getWiFiIPv6Address() != null && ClatSubfix != null){			
			ClatIPv6Addr = ConnectivityReceiver.getWiFiIPv6Address().substring(0, 20)+ClatSubfix.substring(2);
		}
		else {			
			ClatIPv6Addr = "无";
		}
 		
		Log.d("mac","upy12"+ClatIPv6Addr);
 		
/*		IntentFilter messageFilter = new IntentFilter();
		messageFilter.addAction(ConnectivityReceiver.ACTION_CONNECTIVITY_CHANGE);
		messageFilter.addAction(InstallBinary.ACTION_INSTALL_BINARY);
		messageFilter.addAction(RunAsRoot.ACTION_ROOTSCRIPT_DONE);
		LocalBroadcastManager.getInstance(this).registerReceiver(mConnectionChanges, messageFilter);*/
		
/*		final ImageView Logo = (ImageView)findViewById(R.id.logo);
		
		Log.d("list","upy0");
		
		List<Map<String,Object>> Items = new ArrayList<Map<String,Object>>();
		Map<String,Object> Item = new HashMap<String,Object>();
		Item.put("Text", R.id.ClatSwitch);
		Item.put("Switch", R.id.on);
		Items.add(Item);
		
		Log.d("list","upy1");
		
		SimpleAdapter siAdapter = new SimpleAdapter(this,Items,R.layout.launch,
				new String[]{"Text","Switch"}, new int[]{R.id.ClatSwitch,R.id.on});
		
		ListView list = (ListView)findViewById(R.id.mylist);
		list.setAdapter(siAdapter);*/
		
		Log.d("list","upy2");
		
//		LastMessage = (TextView) findViewById(R.id.LastMessage);
		ClatSwitch = (TextView) findViewById(R.id.ClatSwitch);
		date_TextView = (TextView) findViewById(R.id.date);
        date_TextView.setText(getDate());
		
/*		ClatAddr = (TextView) findViewById(R.id.ClatAddr);
		WiFiStatus = (TextView) findViewById(R.id.WIFIStatus);
		
		BinaryStatus = (TextView) findViewById(R.id.BinaryStatus);
		IPv6Address = (TextView) findViewById(R.id.IPv6Address);
		IPv4Address = (TextView) findViewById(R.id.IPv4Address);
		ClatStatus = (TextView) findViewById(R.id.ClatStatus);
		Stdout = (TextView) findViewById(R.id.Stdout);
		Stderr = (TextView) findViewById(R.id.Stderr);*/
		
//		LastMessage.setText("");
//		BinaryStatus.setText("");
		
//		UpdateText();
		
//		findView();
//		mSlipButton.SetOnChangedListener(this);
		
		findView();
		Log.d("main_find","upy3");
		setListener();
		Log.d("main_set","upy4");

/*		File system_bin_su = new File("/system/bin/su");
		if(!system_bin_su.exists()) {
			File system_xbin_su = new File("/system/xbin/su");
			if(!system_xbin_su.exists()) {
				LastMessage.setText("No /system/bin/su or /system/xbin/su found");
				return;
			}			
		}*/
		
/*		if(flag == 2) {
			LastMessage.setText("No /system/bin/su or /system/xbin/su found");
			return;
		}*/		
		
/*		File clatd_conf_copied = new File(InstallBinary.DATA_DIR+"clatd_conf_copied");
		if(!clatd_conf_copied.exists()) {
			Intent firstRun = new Intent(this, RunAsRoot.class);
			firstRun.putExtra(RunAsRoot.EXTRA_STAGE_NAME, "Copy_clatd.conf");
			try {
				firstRun.putExtra(RunAsRoot.EXTRA_SCRIPT_CONTENTS, 
						"#!/system/bin/sh\n" + 
						"echo `date` clatd.conf copy >>/data/misc/clatd.log\n" +
						"cat "+InstallBinary.DATA_DIR+"clatd.conf >/data/misc/clatd.conf\n" +
						"echo ipv6_host_id "+ClatSubfix+" >>/data/misc/clatd.conf\n" +
						"chmod 644 /data/misc/clatd.conf\n" +
						"touch "+InstallBinary.DATA_DIR+"clatd_conf_copied\n" +
						"ip -6 neigh add proxy "+ClatIPv6Addr+" dev "+RunAsRoot.execCommand("getprop wifi.interface")+"\n"
						);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			startService(firstRun);
			LastMessage.setText("copied clatd.conf");
		}*/
		
//		Log.d("mainUpdate","upyAA");
		
		if(Clat.ClatRunning() && ConnectivityReceiver.getWiFiIPv6Address() != null && ClatIPv6Addr != null
				&& !ClatIPv6Addr.equals("无")){
			if(Update.getServerVerCode()) {
				Log.d("mainUpdate","upyAB");
				int vercode = Config.getVerCode(this);
				Log.d("mainUpdate","upyAC"+vercode);
				if(Update.newVerCode > vercode) {
					Log.d("mainUpdate","upyAD");
					doNewVersionUpdate();
					Log.d("mainUpdate","upyAE");
				}
				else {
					Log.d("mainUpdate","upyAF");
//					notNewVersionShow();
					Log.d("mainUpdate","upyAG");
				}
			}
		}	

    }
    
    @Override
	protected void onDestroy() {
//		LocalBroadcastManager.getInstance(this).unregisterReceiver(mConnectionChanges);
		super.onDestroy();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
	
    
    public String getLocalMacAddress() {    
    	
        WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);    
        WifiInfo info = wifi.getConnectionInfo();    
        
        return info.getMacAddress();    
    }  
    
    public String MacToIPv6(String MacAddr){
    	
    	String v6id;    	
    	v6id = "::"+MacAddr.substring(0, 2)+MacAddr.substring(3, 8)+MacAddr.substring(9, 14)+MacAddr.substring(15, 17)+":464";
    	
    	return v6id;
    }
    
    private void setListener()
    {
    	mSlipButton.SetOnChangedListener(new OnChangedListener()
        {
            
        	@Override
			public void OnChanged(boolean CheckState) throws IOException {
        		if (ClatIPv6Addr == null || ClatIPv6Addr.equals("无")) {
        			Toast.makeText(getBaseContext(),"请确保Wi-Fi网络正常并具有IPv6地址" , Toast.LENGTH_SHORT).show();
        			return;
        		}
        		if (CheckState) {
        			Log.d("Route","upyA1");
        			if (!Clat.ClatRunning()) {	
//            			Clat.stopClatIfStarted(getBaseContext());	
        				Log.d("Route","upyA2");
//            			RunAsRoot.execCommand("ip route del "+OriginRoute); 
//            			Log.d("Route","upy"+OriginRoute+"1");
            			Clat.startClat(getBaseContext(),ConnectivityReceiver.WiFiInterfaceName);
            			RunAsRoot.execCommand("ip route add 0.0.0.0/1 via 192.168.255.1 dev clat4");
            			RunAsRoot.execCommand("ip route add 128.0.0.0/1 via 192.168.255.1 dev clat4");
            			Toast.makeText(getBaseContext(),"CLAT开启成功" , Toast.LENGTH_SHORT).show();
        			}
        			else {
        				RunAsRoot.execCommand("ip route add 0.0.0.0/1 via 192.168.255.1 dev clat4");
            			RunAsRoot.execCommand("ip route add 128.0.0.0/1 via 192.168.255.1 dev clat4");
        				Toast.makeText(getBaseContext(),"CLAT已经开启" , Toast.LENGTH_SHORT).show();
        			}
        	    } else {        			
        			Clat.stopClatIfStarted(getBaseContext());
//        			RunAsRoot.execCommand("ip route add "+OriginRoute);
//        			Log.d("Route","upy"+OriginRoute+"2");
        			Toast.makeText(getBaseContext(),"已关闭CLAT" , Toast.LENGTH_SHORT).show();
        		}
        	}
        });
    }
    
    private void findView()
    {
    	mSlipButton = (SlipButton) findViewById(R.id.on);
//        btn = (Button) findViewById(R.id.ringagain);
    	
    	if(Clat.ClatRunning()) {
    		Log.d("findView", "upy0");
    		mSlipButton.setCheck(true);
    	}
    	else {
    		Log.d("findView", "upy1");
    		mSlipButton.setCheck(false);
    	}
    }   
    
    private void doNewVersionUpdate() {
//        int verCode = Config.getVerCode(this);
        String verName = Config.getVerName(this);
        StringBuffer sb = new StringBuffer();
        sb.append("当前版本:");
        sb.append(verName);
//        sb.append(" Code:");
//        sb.append(verCode);
        sb.append(", 发现新版本:");
        sb.append(Update.newVerName);
//        sb.append(" Code:");
//        sb.append(Update.newVerCode);
        sb.append(", 是否更新?");
        Dialog dialog = new AlertDialog.Builder(this)
                        .setTitle("软件更新")
                        .setMessage(sb.toString())
                        // 设置内容
                        .setPositiveButton("更新",// 设置确定按钮
                                        new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                int which) {
                                                        pBar = new ProgressDialog(MainActivity.this);
                                                        pBar.setTitle("正在下载");
                                                        pBar.setMessage("请稍候...");
                                                        pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                                        Log.d("upd","upy0");
                                                        downFile(Config.UPDATE_SERVER
                                                                        + Config.UPDATE_APKNAME);
                                                        Log.d("upd","upy1");
                                                }

                                        })
                        .setNegativeButton("暂不更新",
                                        new DialogInterface.OnClickListener() {
                                                @Override
												public void onClick(DialogInterface dialog,
                                                                int whichButton) {
                                                        // 点击"取消"按钮之后退出程序
                                                      //  finish();                                                        
                                                }
                                        }).create();// 创建
        // 显示对话框
        dialog.show();        

    }
    
    void downFile(final String url) {
        pBar.show();
//        new Thread() {
//                @Override
//				public void run() {
                        HttpClient client = new DefaultHttpClient();
                        HttpGet get = new HttpGet(url);
                        HttpResponse response;
                        Log.d("down","upy0");
                        try {
                                response = client.execute(get);
                                HttpEntity entity = response.getEntity();
                                long length = entity.getContentLength();
                                InputStream is = entity.getContent();
                                FileOutputStream fileOutputStream = null;
                                Log.d("down","upy1"+Environment.getExternalStorageDirectory().toString());
                                if (is != null) {

                                        File file = new File(
                                        		update_path,
                                                        Config.UPDATE_SAVENAME);
                                        fileOutputStream = new FileOutputStream(file);                                
                                        
                                        byte[] buf = new byte[1024];
                                        int ch = -1;
//                                               int count = 0;
                                        while ((ch = is.read(buf)) != -1) {
                                                fileOutputStream.write(buf, 0, ch);
//                                                count += ch;
                                                if (length > 0) {
                                                }
                                        }

                                }
//                                Log.d("down","upy2");
                                fileOutputStream.flush();
                                if (fileOutputStream != null) {
                                        fileOutputStream.close();
                                }
                                Log.d("down","upy3");
//                                down();
                                try {
									InstallBinary.chmod("755", update_path+Config.UPDATE_SAVENAME);
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
                                pBar.cancel();
                                Log.d("down","upy4");
                                update();
                                Log.d("down","upy5");
                        } catch (ClientProtocolException e) {
                        	Log.d("down","upy6"+e.toString());
                                e.printStackTrace();
                        } catch (IOException e) {
                        	Log.d("down","upy7"+e.toString());
                                e.printStackTrace();
                        }
 //               }

//        }.start();

	}
    
/*    void down() {
        handler.post(new Runnable() {
                @Override
				public void run() {
                        pBar.cancel();
                        update();
                }
        });


    }*/
    
    void update() {
    	
    	Log.d("mainup","upy0");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(update_path, Config.UPDATE_SAVENAME)),
                        "application/vnd.android.package-archive");
        startActivity(intent);
        Log.d("mainup","upy1");

    }
    
/*    private void notNewVersionShow() {
 //       int verCode = Config.getVerCode(this);
        String verName = Config.getVerName(this);
        StringBuffer sb = new StringBuffer();
        sb.append("当前版本:");
        sb.append(verName);
 //       sb.append(" Code:");
 //       sb.append(verCode);
        sb.append(",\n已是最新版,无需更新!");
        Dialog dialog = new AlertDialog.Builder(this)
                        .setTitle("软件更新").setMessage(sb.toString())// 设置内容
                        .setPositiveButton("确定",// 设置确定按钮
                                        new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                int which) {
                                                 //       finish();
                                                }

                                        }).create();// 创建
        // 显示对话框
        dialog.show();
       }*/
    
    private String getDate(){
    	Date date = new Date();
    	Calendar c = Calendar.getInstance();
    	c.setTime(date);
    	String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
    	int w = c.get(Calendar.DAY_OF_WEEK) - 1 ;
    	if (w < 0) {
			w = 0;
		}
    	String mDate = c.get(Calendar.YEAR)+"年" + c.get(Calendar.MONTH) + "月" + c.get(Calendar.DATE) + "日  " + weekDays[w];
    	return mDate;
    }

}