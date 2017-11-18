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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    //根据string内容获得具体文件的下载地址
    public static String[] getWholeFilePath(Context context,int id){

        String line = "";
        try {
            InputStream is = new FileInputStream(context.getFilesDir().getAbsolutePath()+"/FLMusic/song_info.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            while (id-- >0){
                reader.readLine();
            }
            line = reader.readLine();
            is.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int m_index = line.indexOf("[m!")+3;
        int l_index = line.indexOf("[l!")+3;
        int b_index = line.indexOf("[b!")+3;

        String urlMp3 = line.substring(m_index,line.indexOf("]",m_index));
        String urlLyc = line.substring(l_index,line.indexOf("]",l_index));
        String urlBgs = line.substring(b_index,line.indexOf("]",b_index));

        return new String[]{urlMp3,urlLyc,urlBgs};

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