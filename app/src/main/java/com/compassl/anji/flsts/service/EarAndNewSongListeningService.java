package com.compassl.anji.flsts.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaButtonReceiver;
import android.util.Log;

import com.compassl.anji.flsts.db.SongInfo;
import com.compassl.anji.flsts.util.HttpUtil;
import com.compassl.anji.flsts.util.InitialTool;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class EarAndNewSongListeningService extends Service {

    private static final String url_song_info
            ="http://sinacloud.net/music-store/song_info.txt?KID=sina,2o3w9tlWumQRMwg2TQqi&Expires=1543597193&ssig=XTUNLGUxmA";
    private static final String url_song_info_1
            = "http://sinacloud.net/music-store/song_info_1.txt?KID=sina,2o3w9tlWumQRMwg2TQqi&Expires=1543597193&ssig=oLDCAlxrZ1";

    private AudioManager audioManager;

    @Override
    public void onCreate() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
    }

    public EarAndNewSongListeningService() {
    }


    public interface OnUpdateFinishListener{
        void updateUI();
    }

    public static class UBPBinder extends Binder {
        private static OnUpdateFinishListener listener;
        public void setOnUpdateFinishListener(OnUpdateFinishListener listener){
            UBPBinder.listener = listener;
        }
    }
    private UBPBinder mBinder = new UBPBinder();


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread thread_new_song = new Thread(new Runnable() {
            @Override
            public void run() {
                hasNewSong();
            }
        });
        thread_new_song.start();
        try {
            thread_new_song.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    private SharedPreferences prefs;

    private void hasNewSong(){
        String url = url_song_info_1;
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
                String str = response.body().string();
                String str_1 = str.substring(str.indexOf("[c!")+3,str.indexOf("]"));
                count = Integer.parseInt(str_1);
                int count_this = prefs.getInt("song_count_show",-1);
                if (count != count_this){
                    loadNewSong();
                }
            }
        });
    }
    private int count;
    //private SharedPreferences prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
    //更新歌曲列表
    private void loadNewSong() {
        final String downloadPath = getFilesDir().getAbsolutePath()+"/FLMusic/";
        File file = new File(downloadPath);
        if (!file.exists()){
            file.mkdirs();
        }
        final int SONG_ACCOUNT = count;
        String url = url_song_info;
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //完成配置文件的下载
                String str = response.body().string();
                InitialTool.loadInfo(EarAndNewSongListeningService.this,downloadPath,str,count);
                //完成图片文件下载
                for (int i = 1;i<=SONG_ACCOUNT;i++){
                    final String i_str = i>9?""+i:"0"+i;
                    final String imgPath = downloadPath+"img/s"+i_str+".jpg";
                    Log.d("imgPath", "dp: "+imgPath);
                    File file = new File(imgPath);
                    if (file.exists()){
                        continue;
                    }
                    final int ii = i;
                    List<SongInfo> infos = DataSupport.select("urlImg")
                            .where("song_id=?",i+"")
                            .find(SongInfo.class);
                    String urlImg = infos.get(0).getUrlImg();
                    HttpUtil.sendOkHttpRequest(urlImg, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            byte[] buf1 = response.body().bytes();
                            OutputStream os1 = new FileOutputStream(imgPath);
                            os1.write(buf1);
                            os1.flush();
                            os1.close();
                            if (ii == SONG_ACCOUNT){
                                EarAndNewSongListeningService.UBPBinder.listener.updateUI();
                                prefs.edit().putInt("song_count",count).apply();
                            }
                        }
                    });
                }
            }
        });
    }




}
