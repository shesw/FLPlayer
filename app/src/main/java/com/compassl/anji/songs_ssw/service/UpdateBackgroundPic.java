package com.compassl.anji.songs_ssw.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.compassl.anji.songs_ssw.MainActivity;
import com.compassl.anji.songs_ssw.R;
import com.compassl.anji.songs_ssw.db.SongInfo;
import com.compassl.anji.songs_ssw.util.HttpUtil;
import com.compassl.anji.songs_ssw.util.InitialTool;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateBackgroundPic extends Service {
    private static final String url_song_info
            ="http://sinacloud.net/music-store/song_info.txt?KID=sina,2o3w9tlWumQRMwg2TQqi&Expires=1543597193&ssig=XTUNLGUxmA";
    private static final String url_song_info_1
            = "http://sinacloud.net/music-store/song_info_1.txt?KID=sina,2o3w9tlWumQRMwg2TQqi&Expires=1543597193&ssig=oLDCAlxrZ1";
    public UpdateBackgroundPic() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                hasNewSong();
                loadBingPic();
//                AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
//                int anHour = 8 * 60 * 60 * 1000;
//                long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
//                Intent intent1 = new Intent(UpdateBackgroundPic.this,UpdateBackgroundPic.class);
//                PendingIntent pi = PendingIntent.getService(UpdateBackgroundPic.this,0,intent1,0);
//                manager.cancel(pi);
//                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Intent intent1 = new Intent("notification_button");
        intent.putExtra("noti",10);
        sendBroadcast(intent1);
        stopSelf();
        return super.onStartCommand(intent,flags,startId);
    }

    private void hasNewSong(){
        String url = url_song_info_1;
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SharedPreferences prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
                String str = response.body().string();
                String str_1 = str.substring(str.indexOf("[c!")+3,str.indexOf("]"));
                count = Integer.parseInt(str_1);
                int count_this = prefs.getInt("song_count_show",-1);
                if (count != count_this){
                    prefs.edit().putInt("song_count",count).apply();
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
        //prefs.edit().putInt("song_count",count).apply();
        String url = url_song_info;
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //完成配置文件的下载
                String str = response.body().string();
                InitialTool.loadInfo(UpdateBackgroundPic.this,downloadPath,str);
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
                        }
                    });
                }
            }
        });
    }

    //方法：从网上更新背景图片
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SharedPreferences prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
                int picture_id = prefs.getInt("id",-1);
                if (picture_id == -1){
                    prefs.edit().putInt("id",0).apply();
                    String str = 0+"";
                    prefs.edit().putString(str, BitmapFactory.decodeResource(getResources(), R.drawable.background_pic_default).toString()).apply();
                }
                if (picture_id == 0 || picture_id == 7){
                    picture_id = 1;
                    prefs.edit().putInt("id",1).apply();
                    String bingPic = response.body().string();
                    String str = picture_id+"";
                    prefs.edit().putString(str,bingPic).apply();
                }else {
                    picture_id+=1;
                    prefs.edit().putInt("id",picture_id).apply();
                    String bingPic = response.body().string();
                    String str = picture_id+"";
                    prefs.edit().putString(str,bingPic).apply();
                }
            }
        });
    }
}
