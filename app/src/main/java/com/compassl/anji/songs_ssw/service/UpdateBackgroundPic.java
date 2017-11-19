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
import android.os.SystemClock;

import com.bumptech.glide.Glide;
import com.compassl.anji.songs_ssw.MainActivity;
import com.compassl.anji.songs_ssw.R;
import com.compassl.anji.songs_ssw.util.HttpUtil;

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
        loadNewSong();
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

    private void loadNewSong() {
        String url = "http://10.0.2.2:90/song_info.txt";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String savePath = getFilesDir().getAbsolutePath()+"/FLMusic/song_info.txt";
                byte[] buf = response.body().bytes();
                InputStream is = new FileInputStream(savePath);
                if (is.available() != buf.length){
                    Intent intent = new Intent("notification_button");
                    intent.putExtra("noti",10);
                    sendBroadcast(intent);
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
