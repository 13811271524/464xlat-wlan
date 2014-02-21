package edu.bupt.Clat;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class RunAsRoot extends IntentService {
	public static final String EXTRA_SCRIPT_CONTENTS = "ScriptContents";
	public static final String EXTRA_STAGE_NAME = "StageName";
	private static HashMap<String, String> shellOutput = new HashMap<String, String>();
	public static final String ACTION_ROOTSCRIPT_DONE = "edu.bupt.464xlat.RootScriptDone";
	
	public RunAsRoot() {
		super("RunAsRoot");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String ScriptContents = intent.getExtras().getString(EXTRA_SCRIPT_CONTENTS);
		String StageName = intent.getExtras().getString(EXTRA_STAGE_NAME);
		try {
			run_script(ScriptContents, StageName);
		} catch (Exception e) {
			Log.e("RunAsRoot", StageName+": "+e.toString());
		}
	}
	
	class SaveOutput implements Runnable {
		private StringBuffer mData;
		private InputStream mFd;
		private String mDebugName;
		
		public SaveOutput(InputStream fd, String debugname) {
			super();
			mData = new StringBuffer();
			mFd = fd;
			mDebugName = debugname;
		}
		
		@Override
		public void run() {
			Log.d("SaveOutput/"+mDebugName, "starting");
			int length;
			do {
				byte[] buffer = new byte[256];
				try {
				  length = mFd.read(buffer);
				} catch(IOException e) {
					Log.d("SaveOutput/"+mDebugName, "ended via exception");
					return;
				}
				if(length > 0) {
				  mData.append(new String(buffer));
				}
			} while(length > 0);			
			Log.d("SaveOutput/"+mDebugName, "ended");
		}
		
		public String getData() {
			return mData.toString();
		}
	}
	
	private void run_script(String ScriptContents, String StageName) throws Exception {
		File RootShellScript = new File(InstallBinary.BIN_DIR, "tether.sh");
		FileOutputStream RootShellScript_out = new FileOutputStream(RootShellScript.getPath(), false);
		RootShellScript_out.write(ScriptContents.getBytes());
		RootShellScript_out.close();
		
		InstallBinary.chmod("755",RootShellScript.getPath());		
		
		Process process;
/*		if(MainActivity.flag == 0) {
			process = Runtime.getRuntime().exec("/system/bin/su -c "+RootShellScript.getPath());
		}
		else {
			process = Runtime.getRuntime().exec("/system/xbin/su -c "+RootShellScript.getPath());
		}
 */
//		process = Runtime.getRuntime().exec("su -c "+RootShellScript.getPath());
		process = Runtime.getRuntime().exec("/system/xbin/su -c "+RootShellScript.getPath());
		
		Log.d("root","run root");
		
		InputStream stdout = process.getInputStream();
		InputStream stderr = process.getErrorStream();
		SaveOutput stdout_run = new SaveOutput(stdout, StageName+"_stdout");
		SaveOutput stderr_run = new SaveOutput(stderr, StageName+"_stderr");
		Thread stdout_th = new Thread(stdout_run);
		Thread stderr_th = new Thread(stderr_run);
		stdout_th.run();
		stderr_th.run();
		stdout_th.join();
		stderr_th.join();
		
		String stdout_data = stdout_run.getData();
		String stderr_data = stderr_run.getData();
        if(process.waitFor() > 0) {
        	stderr_data = stderr_data + "shell exit status != 0\n";
        } else {
        	stderr_data = stderr_data + "exited OK\n";
        }
        
        synchronized (shellOutput) {
            shellOutput.put(StageName+"_stdout", stdout_data);
            shellOutput.put(StageName+"_stderr", stderr_data);			
		}
        
		Intent intent = new Intent(ACTION_ROOTSCRIPT_DONE);
		intent.putExtra(EXTRA_STAGE_NAME, StageName);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
	}
	
	public static String get_stdout(String StageName) {
		synchronized (shellOutput) {
			return shellOutput.get(StageName+"_stdout");
		}
	}
	
	public static String get_stderr(String StageName) {
		synchronized (shellOutput) {
			return shellOutput.get(StageName+"_stderr");
		}
	}
	
	/**
     * Execute linux commands with root permission
     * 
     * @return result
     */
    public static String execCommand(String cmd) throws IOException {
    	Runtime runtime = Runtime.getRuntime();
		Process proc = runtime.exec("su");  //Process proc has got root privilege
		DataOutputStream os = new DataOutputStream(proc.getOutputStream());
		os.writeBytes(cmd + "\n");  //Use "\n" to flag the end of command
		os.writeBytes("exit\n");
		os.flush();

        //Use InputStreamReader to obtain console output
        InputStream is = proc.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(is);
        BufferedReader inputReader = new BufferedReader(inputStreamReader);
        
        //Use InputStreamReader to obtain console error message
        InputStream es = proc.getErrorStream();
        InputStreamReader errorStreamReader = new InputStreamReader(es);
        BufferedReader errorReader = new BufferedReader(errorStreamReader);
        
        // read the ls output
        String line = "";
        StringBuilder sb = new StringBuilder(line);
        while ((line = inputReader.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }
        while ((line = errorReader.readLine()) != null) {
            sb.append(line);
            sb.append('\n');
        }
        
        //exec() returns immediately rather than wait until the command is finished,
        //so we use waitFor() to make sure that it returns after the command has been finished.
        try {
            if (proc.waitFor() != 0) {
                System.err.println("exit value = " + proc.exitValue());
            } 
        }
        catch (InterruptedException e) {  
            System.err.println(e);
        }
        Log.v("execCommand", sb.toString()); 
        return sb.toString();
    } 
}