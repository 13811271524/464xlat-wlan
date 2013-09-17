package edu.bupt.Clat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
//import java.net.Inet4Address;
import java.net.Inet4Address;
import java.net.Inet6Address;
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
	private static String WiFiIPv6Address = null;
	private static String WiFiIPv4Address = null;
	private static Integer WiFiIPv6SubnetLength = null;
	protected static String WiFiInterfaceName = null;
	public final static String ACTION_CONNECTIVITY_CHANGE = "edu.bupt.464xlat.ConnectionChanges";
	public final static String EXTRA_MESSAGE = "message";
	private static String OriginDefaultRoute = null;
	
	protected static NetworkInfo aInfo = null;
	
	private static void sendConnectivityChangeIntent(Context context, String message) {
		Intent intent = new Intent(ACTION_CONNECTIVITY_CHANGE);
		intent.putExtra(EXTRA_MESSAGE, message);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
	
	public static String getWiFiStatus() {
		return wifiStatus == null ? "??" : wifiStatus.toString();
	}
	
	public static String getWiFiIPv6Address() {
		return WiFiIPv6Address == null ? "" : WiFiIPv6Address;
	}
	
	public static String getWiFiIPv4Address() {
		return WiFiIPv4Address == null ? "" : WiFiIPv4Address;
	}
	
	// relies on WiFiInterfaceName, call after findIPv6Addresses()
	private static void findIPv4Addresses() {
/*		try {
			Enumeration<NetworkInterface> nilist = NetworkInterface.getNetworkInterfaces();
			while(nilist.hasMoreElements()) {
				NetworkInterface ni = nilist.nextElement();
				if(ni.getName().indexOf("rmnet") > -1) {  // TODO: LTE networks that have no v4 on the v6 interface
					Enumeration<InetAddress> Addresses = ni.getInetAddresses();
					while(Addresses.hasMoreElements()) {
						InetAddress address = Addresses.nextElement();
						if(address instanceof Inet4Address) {
							WiFiIPv4Address = address.getHostAddress();
							return;
						}
					}
				}
			}
		} catch (SocketException e) {
			Log.e("findIPv4Addresses", "getNetworkInterfaces failed = "+e.toString());
		}*/
		WiFiIPv4Address = getLocalIp4Address();
	}
	
	// gingerbread bug: no IPv6 from NetworkInterface
	private static void findIPv6Addresses() throws IOException {
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
					if(scope.equals("00") && interfaceName.indexOf("wlan") > -1) {
						found_interface = true;
						WiFiInterfaceName = interfaceName;
						WiFiIPv6Address = ipv6_match.group(1)+":"+ipv6_match.group(2)+":"+ipv6_match.group(3)+":"+ipv6_match.group(4)+
								":"+ipv6_match.group(5)+":"+ipv6_match.group(6)+":"+ipv6_match.group(7)+":"+ipv6_match.group(8);
						WiFiIPv6SubnetLength = len;
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
			WiFiInterfaceName = RunAsRoot.execCommand("getprop wifi.interface");
			WiFiIPv6Address = getLocalIpAddress();
			WiFiIPv6SubnetLength = null;
		}
	}
	
	public static void rescanNetworkStatus(Context context) throws IOException {		
		ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		
		if(cm != null) {
			aInfo = cm.getActiveNetworkInfo();
			if(aInfo == null || !aInfo.isAvailable()){
				Log.d("aInfo","upy");
				return;
			}
		}
		
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	    	if(ni.getTypeName().equalsIgnoreCase("wifi")||ni.getTypeName().equalsIgnoreCase("Wi-Fi")) {
	    		if(wifiStatus == null || !wifiStatus.equals(ni.getDetailedState())) {
    				findIPv6Addresses();
    				findIPv4Addresses();
	    			sendConnectivityChangeIntent(context, "[Conn] wifi = "+ni.getDetailedState().toString());
	    			wifiStatus = ni.getDetailedState();
	    			if(wifiStatus.toString().equalsIgnoreCase("CONNECTED")) {
	    				Log.d("rescan", "connected");
	    				// only start clat if we're on a V6-only network
	    				// del WiFiIPv4Address == null &&
	    				if( WiFiIPv6Address != null) {
	    					OriginDefaultRoute = new String(RunAsRoot.execCommand("ip route |grep default |grep "+WiFiInterfaceName));
	    					RunAsRoot.execCommand("ip route del "+OriginDefaultRoute);
	    					Clat.stopClatIfStarted(context);
	    					Clat.startClat(context,WiFiInterfaceName);
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
			try {
				rescanNetworkStatus(context);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(intent.getAction().equals("android.net.conn.TETHER_STATE_CHANGED")) {
			ArrayList<String> active = intent.getStringArrayListExtra("activeArray");
			for(String act : active) {
				if(Tethering.TetheringOnInterface(act)) {
					return;
				}
				if(Tethering.NoTethering()) {
					try {
						findIPv6Addresses();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					String errorMessage = Tethering.setupIPv6(context, act, WiFiIPv6Address, WiFiIPv6SubnetLength, WiFiInterfaceName);
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
	
	public static String getLocalIpAddress() throws IOException {
        InetAddress inetAddress = null;
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                .getNetworkInterfaces();
        outer: while (networkInterfaces.hasMoreElements()) {
            Enumeration<InetAddress> inetAds = networkInterfaces.nextElement()
                    .getInetAddresses();
            while (inetAds.hasMoreElements()) {
                inetAddress = inetAds.nextElement(); 
                // Check if it's ipv6 address and reserved address
                if (inetAddress instanceof Inet6Address && !isReservedAddr(inetAddress)) {
                    break outer;  
                }
                inetAddress = null; //In case when there is no IPv6 interface! 
            }
        }
 
        if (inetAddress != null) {
        	String ipAddr = inetAddress.getHostAddress();
        	// Filter network card No
        	int index = ipAddr.indexOf('%');
        	if (index > 0) {
        		ipAddr = ipAddr.substring(0, index);
        	}
 
        	return ipAddr;
        }
        else return null;
    } 
	
    public static String getLocalIp4Address() {       
    	try {       
    	    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
    	    		en.hasMoreElements();) {       
    	    	NetworkInterface intf = en.nextElement();       
    	        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
    	        		enumIpAddr.hasMoreElements();) {       
    	        	InetAddress inetAddress = enumIpAddr.nextElement();   
    	        	// Check if it's ipv4 address and reserved address
    	            if (inetAddress instanceof Inet4Address && !isReservedAddr(inetAddress)) {       
    	            	return inetAddress.getHostAddress().toString();       
    	            }       
    	        }       
    	     }       
    	 } 
    	catch (SocketException ex) {       
    		Log.e("WifiPreference IpAddress", ex.toString());       
    	}       
    	return null;       
	}
    
    private static boolean isReservedAddr(InetAddress inetAddr) {
        if (inetAddr.isAnyLocalAddress() || inetAddr.isLinkLocalAddress()
                || inetAddr.isLoopbackAddress()) {
            return true;
        }
 
        return false;
    }
}