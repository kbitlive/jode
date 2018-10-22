package com.tools.payhelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class ConFigNet {
//    public static String socketip="47.52.145.70:11199";
    public static final String socketip="172.16.61.9:11199";
    public static  String notifyurl="http://172.16.61.9:11188/notify.php";
//    public static String notifyurl="http://47.52.145.70:11188/notify.php";
    public static final String signkey="123456789";
//    public static final String returnurl="http://47.52.145.70:11188/payresult.php";
    public static final String returnurl="http://172.16.61.9:11188/payresult.php";


    public void savedData(Context context, String value, String password){
        SharedPreferences sp = context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        sp.edit().putString("uname",value).commit();
        sp.edit().putString("pasword",password).commit();
    }
    public String getuname(Context context,String key){
        SharedPreferences sp =context.getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        String name = sp.getString(key, null);
        return name;
    }
    protected static final String PREFS_FILE = "gank_device_id.xml";
    protected static final String PREFS_DEVICE_ID = "gank_device_id";
    protected static String uuid;
    public synchronized static String getDevicesID(Context mContext)
    {
        if( uuid ==null ) {
            if( uuid == null) {
                final SharedPreferences prefs = mContext.getApplicationContext().getSharedPreferences( PREFS_FILE, Context.MODE_PRIVATE);
                final String id = prefs.getString(PREFS_DEVICE_ID, null );

                if (id != null) {
                    // Use the ids previously computed and stored in the prefs file
                    uuid = id;
                } else {

                    final String androidId = Settings.Secure.getString(mContext.getContentResolver(), Settings.Secure.ANDROID_ID);
                    // Use the Android ID unless it's broken, in which case fallback on deviceId,
                    // unless it's not available, then fallback on a random number which we store
                    // to a prefs file
                    try {
                        if (!"9774d56d682e549c".equals(androidId)) {
                            uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8")).toString();
                        } else {
                            final String deviceId = ((TelephonyManager) mContext.getSystemService( Context.TELEPHONY_SERVICE )).getDeviceId();
                            uuid = deviceId!=null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")).toString() : UUID.randomUUID().toString();
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }

                    // Write the value out to the prefs file
                    prefs.edit().putString(PREFS_DEVICE_ID, uuid).commit();
                }
            }
        }
        uuid=uuid.replaceAll("-","");
        return uuid;
    }

}
