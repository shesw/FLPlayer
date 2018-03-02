package com.compassl.anji.flsts.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by Administrator on 2017/12/7.
 */
public class ScreenUtil {
    //获取屏幕相关参数
    public static DisplayMetrics getScreenSize(Context context){
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        display.getMetrics(metrics);
        return metrics;
    }

    //获取屏幕density
    public static float getDeviceDensity(Context context){
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        return metrics.density;
    }




}