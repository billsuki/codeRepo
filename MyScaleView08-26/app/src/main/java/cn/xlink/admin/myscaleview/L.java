package cn.xlink.admin.myscaleview;

import android.util.Log;

public class L {

    private static boolean showLog = BuildConfig.DEBUG;
    private static String TAG = "DEBUG" ;

    public static void i(String tag,String i) {
        if (showLog) {
            if (tag!=null && i!=null)
            Log.i(tag,i ) ;
        }
    }

    public static void d(String tag,String d) {
        if (showLog) {
            if (tag!=null && d!=null)
            Log.d(tag,d) ;
        }
    }

    public static void e(String tag,String e) {
        if (showLog) {
            if (tag!=null && e!=null)
            Log.e(tag,e) ;
        }
    }

    public static void v(String tag,String v) {
        if (showLog) {
            if (tag!=null && v!=null)
            Log.v(tag,v) ;
        }
    }


    public static void i(String i) {
        if (showLog) {
            Log.i(TAG,i ) ;
        }
    }

    public static void d(String d) {
        if (showLog) {
            Log.d(TAG,d) ;
        }
    }

    public static void e(String e) {
        if (showLog) {
            Log.e(TAG,e) ;
        }
    }

    public static void v(String v) {
        if (showLog) {
            Log.v(TAG,v) ;
        }
    }
}
