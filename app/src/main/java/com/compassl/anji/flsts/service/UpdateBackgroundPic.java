package com.compassl.anji.flsts.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.compassl.anji.songs_ssw.R;
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

public class UpdateBackgroundPic extends Service {

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
                loadBingPic();
                AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
                int anHour = 24 * 60 * 60 * 1000;
                long triggerAtTime = SystemClock.elapsedRealtime()+anHour;
                Intent intent1 = new Intent(UpdateBackgroundPic.this,UpdateBackgroundPic.class);
                PendingIntent pi = PendingIntent.getService(UpdateBackgroundPic.this,0,intent1,0);
                manager.cancel(pi);
                manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
            }
        });
        thread.start();
        return super.onStartCommand(intent,flags,startId);
    }

    //方法：从网上更新背景图片
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        final String backgroundSavePath = getFilesDir().getAbsolutePath()+"/FLMusic/backgroundPic/";
        File file = new File(backgroundSavePath);
        if (!file.exists()){
            file.mkdirs();
        }
        final SharedPreferences prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
        final int picture_id = prefs.getInt("id",-1);
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String savePath;
                int id_use;
                if (picture_id == 7){
                    id_use = 0;
                    prefs.edit().putInt("id",id_use).apply();
                    savePath = backgroundSavePath+"B"+id_use+".txt";
                }else {
                    id_use = picture_id+1;
                    savePath = backgroundSavePath+"B"+id_use+".txt";
                }
                OutputStream os = new FileOutputStream(savePath);
                byte[] buf = response.body().bytes();
                os.write(buf);
                os.flush();
                os.close();
                prefs.edit().putInt("id",id_use).apply();
            }
        });
    }
}
