package com.compassl.anji.songs_ssw.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ProviderInfo;
import android.preference.PreferenceManager;
import android.util.Log;

import com.compassl.anji.songs_ssw.MainActivity;
import com.compassl.anji.songs_ssw.R;
import com.compassl.anji.songs_ssw.Song;
import com.compassl.anji.songs_ssw.db.SongInfo;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/28.
 */
public class InitialTool {

    public static List<Song> initSongChoose(Context context){
        SharedPreferences prefs = context.getSharedPreferences("bingPic",Context.MODE_PRIVATE);
        List<Song> songList = new ArrayList<>();
        int total = prefs.getInt("song_count",9);
        for (int i = 1;i<=total;i++){
            List<SongInfo> name1 = DataSupport.select("song_name").where("song_id = ?",i+"").find(SongInfo.class);
            String name = name1.get(0).getSong_name();
            String i_str = i>9?i+"":"0"+i;
            String imgRes = context.getFilesDir().getAbsolutePath()+"/FLMusic/img/s"+i_str+".jpg";
            Log.d("imgPath", "id: "+imgRes);
            Song song = new Song(name,imgRes);
            songList.add(song);
        }
        return songList;
    }

    public static void loadInfo(Context context, String downloadPath,String string){
        int song_count = 0;
        try {
            BufferedReader bufferedReader;
            if (string == null){
                bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("song_info.txt")));
            }
            else {
                bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(string.getBytes("UTF-8"))));
            }

            String song_count_str = bufferedReader.readLine();
            String song_count_str_dec = song_count_str.substring(song_count_str.indexOf("[c!")+3,
                    song_count_str.indexOf("]"));
            Log.d("IT", "str: "+song_count_str_dec);
            song_count = Integer.parseInt(song_count_str_dec);
            SharedPreferences prefs = context.getSharedPreferences("bingPic",Context.MODE_PRIVATE);
            prefs.edit().putInt("song_count",song_count).apply();
            for (int i = 1;i<=song_count;i++){
                String str = bufferedReader.readLine();
                String[] str4 = TextHandle.handleInfo(str);
                SongInfo songInfo = new SongInfo(i,i,str4[0],str4[1],str4[2],str4[3]);
                songInfo.save();
            }
            bufferedReader.close();

            //从asset加载
            if (string==null){
                //判断有无图片文件夹，若没有则创建
                hasImg(downloadPath+"img/");
                for (int i = 1;i<=song_count;i++){
                    String index_str = i>9?i+"":"0"+i;
                    //InputStream is1 = new FileInputStream("file:///android_asset/img/s"+index_str+".jpg");
                    InputStream is1 = context.getAssets().open("img/s"+index_str+".jpg");
                    byte[] buf1 = new byte[is1.available()];
                    is1.read(buf1);
                    is1.close();
                    String imgPath = downloadPath+"img/s"+index_str+".jpg";
                    OutputStream os1 = new FileOutputStream(imgPath);
                    os1.write(buf1);
                    os1.flush();
                    os1.close();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        SharedPreferences prefs = context.getSharedPreferences("bingPic",Context.MODE_PRIVATE);
        prefs.edit().putInt("song_count",song_count).apply();
       // return song_count;
    }

    private static void hasImg(String path){
        File file = new File(path);
        if (!file.exists()){
            boolean is = file.mkdirs();
            Log.d("aaa", is+"");
        }
    }


}