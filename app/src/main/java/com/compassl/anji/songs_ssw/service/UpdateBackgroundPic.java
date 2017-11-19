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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class UpdateBackgroundPic extends Service {
    public UpdateBackgroundPic() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        hasNewSong();
        loadBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
        Intent intent1 = new Intent(this,UpdateBackgroundPic.class);
        PendingIntent pi = PendingIntent.getService(this,0,intent1,0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent,flags,startId);
    }

    private void hasNewSong(){
        String url = "http://10.0.2.2:90/song_info_1.txt";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                SharedPreferences prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
                String str = response.body().string().substring(1,3);
                count = Integer.parseInt(str);
                int count_this = prefs.getInt("song_count",-1);
                if (count != count_this){
                    //prefs.edit().putInt("song_count",count).apply();
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
      //  SharedPreferences prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
     //   final int SONG_ACCOUNT = prefs.getInt("song_count",-1);
        final int SONG_ACCOUNT = count;
        String url = "http://10.0.2.2:90/song_info.txt";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //完成配置文件的下载
                InitialTool.loadInfo(UpdateBackgroundPic.this,downloadPath,response.body().string());
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
                    Log.d("MAAA", "onResponse: "+imgPath);
                    HttpUtil.sendOkHttpRequest("http://10.0.2.2:90/song_img/s" + i_str + ".jpg", new Callback() {
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
                                Intent intent = new Intent("notification_button");
                                intent.putExtra("noti",10);
                                sendBroadcast(intent);
                            }
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
                    prefs.edit().putString("pic"+1, BitmapFactory.decodeResource(getResources(), R.drawable.background_pic_default).toString()).apply();
                    return;
                }
                if (picture_id == 0 || picture_id == 7){
                    prefs.edit().putInt("id",1).apply();
                    picture_id = 1;
                }else {
                    prefs.edit().putInt("id",++picture_id).apply();
                }
                final String bingPic = response.body().string();
                prefs.edit().putString("pic"+picture_id,bingPic).apply();
            }
        });
    }
}
