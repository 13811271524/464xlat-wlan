package org.drown.FourSixFourXlat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Scanner;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class Clat {
	private static String ClatInterface = null;
	private static boolean hasClatdState = false;
	private static File ClatState = new File(InstallBinary.DATA_DIR, "clat.state");
	
	public static String getClatInterface() {
		InitFromDisk();
		return (ClatInterface == null) ? "" : ClatInterface;
	}
	
	public static boolean ClatRunning() {
		InitFromDisk();
		return ClatInterface != null;
	}
	
	private static void WriteClatState(String interfaceName) {
		try {
			FileOutputStream ClatState_out = new FileOutputStream(ClatState.getPath(), false);
			String state = interfaceName+"\n";
			ClatState_out.write(state.getBytes());
			ClatState_out.close();
		} catch (Exception e) {
			Log.e("Clatd/writestate", e.toString());
		}
	}
	
	private static void ReadClatState() {
		if(!ClatState.exists()) {
			return;
		}
		try {
			Scanner ClatState_in = new Scanner(ClatState);
			if(ClatState_in.hasNextLine()) {
				ClatInterface = ClatState_in.nextLine();
			}
			hasClatdState = true;
			ClatState_in.close();
		} catch (Exception e) {
			Log.e("Tethering/readstate", e.toString());
		}
	}
	
	public static void InitFromDisk() {
		if(!hasClatdState) {
			ReadClatState();
		}
	}
	
	public static void startClat(Context context, String interfaceName) {
		InitFromDisk();
		if(ClatInterface != null) {
			Log.e("startClat", "Clat was already started");
		}
		
		ClatInterface = interfaceName;
		hasClatdState = true;
		
		WriteClatState(interfaceName);
		
		Log.d("startClat", "Clat is starting on "+interfaceName);
		
		StringBuffer Script = new StringBuffer();
		Script.append("#!/system/bin/sh\n");
		Script.append(InstallBinary.BIN_DIR+"clatd -i "+interfaceName+" >/dev/null 2>&1 &\n");
		Script.append("CLATPID=$!\n");
		Script.append("echo $CLATPID >"+InstallBinary.DATA_DIR+"clatd.pid\n");
		Script.append("echo started clat, pid = $CLATPID\n");
		
		Intent startClat = new Intent(context, RunAsRoot.class);
		startClat.putExtra(RunAsRoot.EXTRA_STAGE_NAME, "start_clat");
		startClat.putExtra(RunAsRoot.EXTRA_SCRIPT_CONTENTS, Script.toString());
		context.startService(startClat);
	}
	
	public static void stopClatIfStarted(Context context) {
		InitFromDisk();
		ClatState.delete();
		if(ClatInterface != null) {
			stopClat(context);
		}
	}
	
	public static void stopClat(Context context) {
		ClatInterface = null;
		
		Log.d("stopClat", "Clat is stopping");
		
		StringBuffer Script = new StringBuffer();
		Script.append("#!/system/bin/sh\n");
		Script.append("CLATPID=`cat "+InstallBinary.DATA_DIR+"clatd.pid`\n");
		Script.append("echo killing pid $CLATPID\n");
		Script.append("kill $CLATPID\n");
		Script.append("rm "+InstallBinary.DATA_DIR+"clatd.pid\n");
		
		Intent stopClat = new Intent(context, RunAsRoot.class);
		stopClat.putExtra(RunAsRoot.EXTRA_STAGE_NAME, "stop_clat");
		stopClat.putExtra(RunAsRoot.EXTRA_SCRIPT_CONTENTS, Script.toString());
		context.startService(stopClat);
	}
}