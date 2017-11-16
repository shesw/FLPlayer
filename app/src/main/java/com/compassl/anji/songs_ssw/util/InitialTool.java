package com.compassl.anji.songs_ssw.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ProviderInfo;
import android.preference.PreferenceManager;

import com.compassl.anji.songs_ssw.MainActivity;
import com.compassl.anji.songs_ssw.R;
import com.compassl.anji.songs_ssw.Song;
import com.compassl.anji.songs_ssw.db.SongInfo;

import org.litepal.LitePal;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/10/28.
 */
public class InitialTool {

    public static List<Song> initSongChoose(Context context){
        List<Song> songList = new ArrayList<>();
        try {
                InputStream songInfo = context.getAssets().open("song_info.txt");
                byte[] buffer = new byte[songInfo.available()];
                songInfo.read(buffer);
                String info = new String(buffer);
                String total_string = info.substring(info.indexOf("[c!")+3,info.indexOf("]"));
                int total = Integer.parseInt(total_string);
                for (int i = 1;i<=total;i++){
                        int index_id = info.indexOf("[i!"+(i>9?i:"0"+i)+"]");
                        int index_name = info.indexOf("[n!",index_id)+3;
                        int index_imgRes = info.indexOf("[p!",index_id)+3;
                        String name = info.substring(index_name,info.indexOf("]",index_name));
                        String imgRes = info.substring(index_imgRes,info.indexOf("]",index_imgRes));
                        Song song = new Song(name,imgRes);
                        songList.add(song);
                }
        } catch (IOException e) {
                e.printStackTrace();
        }
        return songList;
    }


}