package edu.bupt.Clat;

import org.json.JSONArray;
import org.json.JSONObject;

import android.util.Log;

public class Update {
	
	public static int newVerCode = 0;
    public static String newVerName = "";
    private static final String TAG = "Update";
    private static String verjson = null;
	
	public static boolean getServerVerCode() {
//		Log.d("update","upy1");
        try {
//        	Log.d("update","upy02");    
        	verjson = new String(NetworkTool.getContent());
//            Log.d("update","upy2"+verjson);
            JSONArray array = new JSONArray(verjson);
//            Log.d("update","upy2.1"+array.toString());
            if (array.length() > 0) {                	
                    JSONObject obj = array.getJSONObject(0);
//                    Log.d("update","upy3");
                    try {
                            newVerCode = Integer.parseInt(obj.getString("verCode"));
                            newVerName = obj.getString("verName");
                    } catch (Exception e) {
                            newVerCode = -1;
                            newVerName = "";
                            return false;
                    }
            }
        } catch (Exception e) {
                Log.e(TAG, e.getMessage());
                return false;
        }
        return true;

	}
	
	

}
