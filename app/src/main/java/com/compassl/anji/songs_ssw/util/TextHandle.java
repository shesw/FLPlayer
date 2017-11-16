package com.compassl.anji.songs_ssw.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.util.Size;

import com.compassl.anji.songs_ssw.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2017/11/2.
 */
public class TextHandle {


    //传入what样例：song_mp3_01
    public static String getSongInfoUrl(Context context,String what) throws IOException {
        String returnInfo;
        String filePath = "song_info_"+what.substring(what.length()-2,what.length())+".txt";
        InputStream is = context.getAssets().open(filePath);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        String str = new String(buffer);
        int res = str.indexOf(what);
        if ( res != -1 ){
            returnInfo = str.substring(str.indexOf("http",res),str.indexOf("</>",res));
        }else{
            return null;
        }
        return returnInfo;
    }

    public static String getLrcInfo(String allLrc){
        //Matcher m = Pattern.compile("\\[(\\d{1,2}):(\\d{1,2}).(\\d{1,2})\\]").matcher(allLrc);
        if ("".equals(allLrc)){return "";}
        Matcher m = Pattern.compile("\\[(\\d*):(\\d*).(\\d*)\\]").matcher(allLrc);
        int i;
        if (m.find()){
            i= m.start();
        }else {
            return "无歌词信息";
        }
        String str = allLrc.substring(0,i-1);
        return "\r\n\r\n\r\n"+str.replace("[ti:","歌曲：").replace("[ar:","作曲：").replace("[al:","专辑：").replace("[by:","作词：")
                .replaceAll("]","");
    }

}