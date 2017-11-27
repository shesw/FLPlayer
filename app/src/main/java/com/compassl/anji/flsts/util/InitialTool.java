package com.compassl.anji.flsts.util;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.compassl.anji.flsts.Song;
import com.compassl.anji.flsts.db.SongInfo;

import org.litepal.crud.DataSupport;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
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
        int total = prefs.getInt("song_count_show",1);
        for (int i = 1;i<=total;i++){
            List<SongInfo> name1 = DataSupport.select("song_name").where("song_id = ?",i+"").find(SongInfo.class);
            if (name1.size()==0){
                continue;
            }
            String name = name1.get(name1.size()-1).getSong_name();
            String i_str = i>9?i+"":"0"+i;
            String imgRes = context.getFilesDir().getAbsolutePath()+"/FLMusic/img/s"+i_str+".jpg";
            Log.d("imgPath", "id: "+imgRes);
            Song song = new Song(name,imgRes);
            songList.add(song);
        }
        return songList;
    }

    public static void loadInfo(Context context, String downloadPath,String string,int count){
        int song_count;
        try {
            BufferedReader bufferedReader;
            SharedPreferences prefs = context.getSharedPreferences("bingPic",Context.MODE_PRIVATE);
            //从asset加载
            if (string == null){
                bufferedReader = new BufferedReader(new InputStreamReader(context.getAssets().open("song_info.txt")));
                String song_count_str = bufferedReader.readLine();
                String song_count_str_dec = song_count_str.substring(song_count_str.indexOf("[c!")+3,
                        song_count_str.indexOf("]"));
                song_count = Integer.parseInt(song_count_str_dec);
                prefs.edit().putInt("song_count_show",song_count).apply();
                //判断有无图片文件夹，若没有则创建
                hasImg(downloadPath+"img/");
                for (int i = 1;i<=song_count;i++){
                    String index_str = i>9?i+"":"0"+i;
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
            }else {
                bufferedReader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(string.getBytes("UTF-8"))));
                song_count = count;
                //prefs.edit().putInt("song_count",song_count).apply();
                bufferedReader.readLine();
            }

            for (int i = 1;i<=song_count;i++){
                List<SongInfo> list = DataSupport.select("song_id").where("song_id=?",i+"").find(SongInfo.class);
                String str = bufferedReader.readLine();
                String[] str5 = TextHandle.handleInfo(str);
                if (list.size()==0){
                    SongInfo songInfo = new SongInfo(i,str5[0],str5[1],str5[2],str5[3],str5[4]);
                    songInfo.save();
                }else {
                    SongInfo songInfo = new SongInfo(str5[0],str5[1],str5[2],str5[3],str5[4]);
                    songInfo.updateAll("song_id=?",i+"");
                }
            }
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        SharedPreferences prefs = context.getSharedPreferences("bingPic",Context.MODE_PRIVATE);
//        prefs.edit().putInt("song_count",song_count).apply();
       // return song_count;
    }

    private static void hasImg(String path){
        File file = new File(path);
        if (!file.exists()){
            file.mkdirs();
        }
    }


}