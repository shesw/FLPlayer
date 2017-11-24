package com.compassl.anji.flsts;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/11/23.
 */
public class ActivityCollector {

    private static List<Activity> activities = new ArrayList<>();
    public static void addActivity(Activity activity){
        activities.add(activity);
    }
    public static void removeActivity(Activity activity){
        activities.remove(activity);
    }

    public static List<Activity> getActivityList(){
        return activities;
    }



}