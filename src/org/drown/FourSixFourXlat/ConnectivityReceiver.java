package org.drown.FourSixFourXlat;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
//import android.net.wifi;

public class ConnectivityReceiver extends BroadcastReceiver {
	private static DetailedState wifiStatus = null;
	private static String WIFIIPv6Address = null;
	private static String WIFIIPv4Address = null;
	private static Integer WIFIIPv6SubnetLength = null;
	private static String WIFIInterfaceName = null;
	public final static String ACTION_CONNECTIVITY_CHANGE = "org.drown.464xlat.ConnectionChanges";
	public final static String EXTRA_MESSAGE = "message";
	
	private static void sendConnectivityChangeIntent(Context context, String message) {
		Intent intent = new Intent(ACTION_CONNECTIVITY_CHANGE);
		intent.putExtra(EXTRA_MESSAGE, message);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
	
	public static String getWIFIStatus() {
		return wifiStatus == null ? "??" : wifiStatus.toString();
	}
	
	public static String getWIFIIPv6Address() {
		return WIFIIPv6Address == null ? "" : WIFIIPv6Address;
	}
	
	public static String getWIFIIPv4Address() {
		return WIFIIPv4Address == null ? "" : WIFIIPv4Address;
	}
	
	// relies on WIFIInterfaceName, call after findIPv6Addresses()
	private static void findIPv4Addresses() {
		try {
			Enumeration<NetworkInterface> nilist = NetworkInterface.getNetworkInterfaces();
			while(nilist.hasMoreElements()) {
				NetworkInterface ni = nilist.nextElement();
				if(ni.getName().indexOf("rmnet") > -1) {  // TODO: LTE networks that have no v4 on the v6 interface
					Enumeration<InetAddress> Addresses = ni.getInetAddresses();
					while(Addresses.hasMoreElements()) {
						InetAddress address = Addresses.nextElement();
						if(address instanceof Inet4Address) {
							WIFIIPv4Address = address.getHostAddress();
							return;
						}
					}
				}
			}
		} catch (SocketException e) {
			Log.e("findIPv4Addresses", "getNetworkInterfaces failed = "+e.toString());
		}
		WIFIIPv4Address = null;
	}
	
	// gingerbread bug: no IPv6 from NetworkInterface
	private static void findIPv6Addresses() {
		File inet6_file = new File("/proc/net/if_inet6");
		Boolean found_interface = false;
		try {
			Scanner inet6_interfaces = new Scanner(inet6_file);

			while(inet6_interfaces.hasNextLine()) {
				String ifline = inet6_interfaces.nextLine();
				Pattern ipv6_pattern = Pattern.compile("^([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4})([0-9a-f]{4}) [0-9a-f]{2} ([0-9a-f]{2}) ([0-9a-f]{2}) [0-9a-f]{2} +([a-z0-9_.]+)$");
				Matcher ipv6_match = ipv6_pattern.matcher(ifline);
				if(ipv6_match.find()) {
					Integer len = Integer.parseInt(ipv6_match.group(9), 16);
					String scope = ipv6_match.group(10);
					String interfaceName = ipv6_match.group(11);
					if(scope.equals("00") && interfaceName.indexOf("rmnet") > -1) {
						found_interface = true;
						WIFIInterfaceName = interfaceName;
						WIFIIPv6Address = ipv6_match.group(1)+":"+ipv6_match.group(2)+":"+ipv6_match.group(3)+":"+ipv6_match.group(4)+
								":"+ipv6_match.group(5)+":"+ipv6_match.group(6)+":"+ipv6_match.group(7)+":"+ipv6_match.group(8);
						WIFIIPv6SubnetLength = len;
					}
				} else {
				  Log.d("findIPv6Addresses", "not matched ifline = "+ifline);
				}
			}
			inet6_interfaces.close();
		} catch (FileNotFoundException e) {
			Log.d("findIPv6Addresses", "failed: "+e.toString());
		}
		if(!found_interface) {
			WIFIInterfaceName = null;
			WIFIIPv6Address = null;
			WIFIIPv6SubnetLength = null;
		}
	}
	
	public static void rescanNetworkStatus(Context context) {
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	    	if(ni.getTypeName().equals("wifi")) {
	    		if(wifiStatus == null || !wifiStatus.equals(ni.getDetailedState())) {
    				findIPv6Addresses();
    				findIPv4Addresses();
	    			sendConnectivityChangeIntent(context, "[Conn] wifi = "+ni.getDetailedState().toString());
	    			wifiStatus = ni.getDetailedState();
	    			if(wifiStatus.toString().equals("CONNECTED")) {
	    				Log.d("rescan", "connected");
	    				// only start clat if we're on a V6-only network
	    				if(WIFIIPv4Address == null && WIFIIPv6Address != null) {
	    					Clat.startClat(context,WIFIInterfaceName);
	    				}
	    			} else {
	    				Log.d("rescan", "other state "+wifiStatus.toString());
	    				Tethering.teardownIfUp(context);
	    				Clat.stopClatIfStarted(context);
	    			}
	    			return;
	    		}
	    	}
	    }	
	}	
	 
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent.getAction().equals("android.net.conn.CONNECTIVITY_CHANGE")) {
			rescanNetworkStatus(context);
		} else if(intent.getAction().equals("android.net.conn.TETHER_STATE_CHANGED")) {
			ArrayList<String> active = intent.getStringArrayListExtra("activeArray");
			for(String act : active) {
				if(Tethering.TetheringOnInterface(act)) {
					return;
				}
				if(Tethering.NoTethering()) {
					findIPv6Addresses();
					String errorMessage = Tethering.setupIPv6(context, act, WIFIIPv6Address, WIFIIPv6SubnetLength, WIFIInterfaceName);
					if(errorMessage != null) {
						sendConnectivityChangeIntent(context, errorMessage);
					} else {
						sendConnectivityChangeIntent(context, "IPv6 tethering setup");
					}
					return;
				}
			}
			Tethering.teardownIfUp(context);
		}
	}
}