package cn.xlink.admin.myscaleview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;


public class UIUtils {

    private static int[] sScreenSize;


    /**
     * 获取屏幕宽度
     */
    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public static int getScreenWidth(Context context) {
        WindowManager wm = ((WindowManager) context.getSystemService(
                Context.WINDOW_SERVICE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            wm.getDefaultDisplay().getSize(size);
            return size.x;
        } else {
            Display d = wm.getDefaultDisplay();
            return d.getWidth();
        }
    }

    /**
     * dp转px
     *
     * @param dip
     * @return
     */
    public static int dip2px(Context context, float dip) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dip * scale + 0.5f);
//        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
//                getResources().getDisplayMetrics());
    }

    /**
     * px转换dip
     */

    public static int px2dip(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue
     * @param spValue （DisplayMetrics类中属性scaledDensity）
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static String toHour(long time) {
        long hour = time/(60*60*1000);
        long minute = (time - hour*60*60*1000)/(60*1000);
        long second = (time - hour*60*60*1000 - minute*60*1000)/1000;
        if(second >= 60 ) {
            second = second % 60;
            minute+=second/60;
        }
        if(minute >= 60) {
            minute = minute %60;
            hour += minute/60;
        }
        String sh = "";
        String sm = "";
        String ss = "";
        if(hour <10) {
            sh = "0" + String.valueOf(hour);
        }else {
            sh = String.valueOf(hour);
        }

        if(minute <10) {
            sm = "0" + String.valueOf(minute);
        }else {
            sm = String.valueOf(minute);
        }

        if(second <10) {
            ss = "0" + String.valueOf(second);
        }else {
            ss = String.valueOf(second);
        }
        return sh+":"+sm ;
    }

}
