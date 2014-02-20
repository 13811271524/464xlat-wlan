package edu.bupt.Clat;

import java.io.File;
import java.io.IOException;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import org.slipbtn.SlipButton;
import org.slipbtn.SlipButton.OnChangedListener;


public class MainActivity extends Activity {
	TextView ClatAddr, WiFiStatus, LastMessage, BinaryStatus, IPv4Address, IPv6Address, ClatStatus, Stdout, Stderr;
	
//	private String DefaultRoute = null;
	private String wifiinfo;
	protected String OriginRoute;
	protected static String ClatSubfix = null;
	protected static String WiFiMacAddr = null;
	protected static String ClatIPv6Addr = null;
	public static int flag = 0;

	private SlipButton mSlipButton = null;
	
	private void UpdateText() {
//		ClatAddr.setText(Tethering.InterfaceName());
		wifiinfo = new String(ConnectivityReceiver.getWiFiStatus());
		
		if(ConnectivityReceiver.getWiFiStatus().equalsIgnoreCase("CONNECTED")){
			wifiinfo = new String("已连接");
		}
		
		ClatAddr.setText(ClatIPv6Addr);
		WiFiStatus.setText(wifiinfo);
		IPv6Address.setText(ConnectivityReceiver.getWiFiIPv6Address());
		IPv4Address.setText("192.168.255.1");
		ClatStatus.setText(Clat.getClatInterface());
	}
	
	private BroadcastReceiver mConnectionChanges = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(ConnectivityReceiver.ACTION_CONNECTIVITY_CHANGE)) {
				String message = intent.getStringExtra("message");
			    LastMessage.setText(message);
			    UpdateText();
			} else if(intent.getAction().equals(InstallBinary.ACTION_INSTALL_BINARY)) {
				String message = intent.getStringExtra("message");
				BinaryStatus.setText(message);
			} else if(intent.getAction().equals(RunAsRoot.ACTION_ROOTSCRIPT_DONE)) {
				String StageName = intent.getStringExtra(RunAsRoot.EXTRA_STAGE_NAME);
				Stdout.setText(RunAsRoot.get_stdout(StageName));
				Stderr.setText(RunAsRoot.get_stderr(StageName));
				LastMessage.setText("Stage Script "+StageName+" completed");
			}
		}
	};
		
    @Override
    public void onCreate(Bundle savedInstanceState) {    	
    	
        super.onCreate(savedInstanceState);    
        setContentView(R.layout.activity_main);           
       
		InstallBinary install = new InstallBinary(this);
		install.go();	
		
/*		try {
			RunAsRoot.execCommand("setprop dhcp.wlan0.dns1 2001:250:f004:f001::130");
			RunAsRoot.execCommand("setprop dhcp.wlan0.dns2 8.8.8.8");
			RunAsRoot.execCommand("setprop net.dns1 2001:250:f004:f001::130");
			RunAsRoot.execCommand("setprop net.dns2 8.8.8.8");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		

		Log.d("mac","upy01"+WiFiMacAddr);
 */			
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
//				return;
			}			
		}
				
		try {
			ConnectivityReceiver.rescanNetworkStatus(this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
/*		try {
			RunAsRoot.execCommand("setprop dhcp.wlan0.dns1 2001:250:f004:f001::130");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
/*		Log.d("mac","upy05"+DefaultRoute);
		
		try {
			DefaultRoute = RunAsRoot.execCommand("ip route |grep default |grep "+ConnectivityReceiver.WiFiInterfaceName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("mac","upy06"+DefaultRoute+"upy07"+OriginRoute);
		if(DefaultRoute != null)
			OriginRoute = new String(DefaultRoute);
				
		Log.d("mac","upy08"+DefaultRoute+"upy09"+OriginRoute);*/
		Log.d("mac","upy10"+ConnectivityReceiver.aInfo+"upy11"+ClatIPv6Addr);
			
		if(ConnectivityReceiver.getWiFiIPv6Address().length() > 20 && ClatSubfix != null){			
			ClatIPv6Addr = ConnectivityReceiver.getWiFiIPv6Address().substring(0, 20)+ClatSubfix.substring(2);
		}
		else {			
			ClatIPv6Addr = "无";
		}
		
		Log.d("mac","upy12"+ClatIPv6Addr);
		
		
		IntentFilter messageFilter = new IntentFilter();
		messageFilter.addAction(ConnectivityReceiver.ACTION_CONNECTIVITY_CHANGE);
		messageFilter.addAction(InstallBinary.ACTION_INSTALL_BINARY);
		messageFilter.addAction(RunAsRoot.ACTION_ROOTSCRIPT_DONE);
		LocalBroadcastManager.getInstance(this).registerReceiver(mConnectionChanges, messageFilter);
		
		try {
			RunAsRoot.execCommand("ip route add 0.0.0.0/1 via 192.168.255.1 dev clat4");
			RunAsRoot.execCommand("ip route add 128.0.0.0/1 via 192.168.255.1 dev clat4");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		
		ClatAddr = (TextView) findViewById(R.id.ClatAddr);
		WiFiStatus = (TextView) findViewById(R.id.WIFIStatus);
		LastMessage = (TextView) findViewById(R.id.LastMessage);
		BinaryStatus = (TextView) findViewById(R.id.BinaryStatus);
		IPv6Address = (TextView) findViewById(R.id.IPv6Address);
		IPv4Address = (TextView) findViewById(R.id.IPv4Address);
		ClatStatus = (TextView) findViewById(R.id.ClatStatus);
		Stdout = (TextView) findViewById(R.id.Stdout);
		Stderr = (TextView) findViewById(R.id.Stderr);
		
		LastMessage.setText("");
		BinaryStatus.setText("");
		UpdateText();
		
//		findView();
//		mSlipButton.SetOnChangedListener(this);
		
		findView();
		setListener();

		
		
		File clatd_conf_copied = new File(InstallBinary.DATA_DIR+"clatd_conf_copied");
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
		}
    }
    
    @Override
	protected void onDestroy() {
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mConnectionChanges);
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
            
        	public void OnChanged(boolean CheckState) throws IOException {
        		if (ClatIPv6Addr == null || ClatIPv6Addr.equals("无")) {
        			Toast.makeText(getBaseContext(),"请确保Wi-Fi连接正常并具有IPv6地址" , Toast.LENGTH_SHORT).show();
        			return;
        		}
        		if (CheckState) {
        			if (!Clat.ClatRunning()) {        				
//            			Clat.stopClatIfStarted(getBaseContext());		
            			 
            			//Log.d("Route","upy"+OriginRoute+"1");
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
        			//RunAsRoot.execCommand("ip route add "+OriginRoute);
        			//Log.d("Route","upy"+OriginRoute+"2");
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

}