package com.compassl.anji.songs_ssw.util;

/**
 * Created by Administrator on 2017/11/1.
 */
public class MathUtil {

    public static String getDisplayTime(int time) {
        int min = time/60000;
        int sec = time%60000/1000;
        String min_string;
        String sec_string;
        if (min/10<1){
            min_string = "0"+min;
        }else {
            min_string = ""+min;
        }
        if (sec/10<1){
            sec_string = "0"+sec;
        }else {
            sec_string = ""+sec;
        }
        return min_string+":"+sec_string;
    }

}