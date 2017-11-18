package com.compassl.anji.songs_ssw.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.compassl.anji.songs_ssw.MainActivity;
import com.compassl.anji.songs_ssw.util.HttpUtil;
import com.compassl.anji.songs_ssw.util.TextHandle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DownloadMusic extends Service {
    private static final String TAG = "DownloadMusic";
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int index = intent.getIntExtra("id",-1);
        String name = intent.getStringExtra("name");
        if (index==-1){
            return super.onStartCommand(intent,flags,startId);
        }
        //下载地址
        String[] str3 = TextHandle.getWholeFilePath(DownloadMusic.this,index);
        download(index,name,str3);
        return super.onStartCommand(intent, flags, startId);
    }

    private void download(int index,String name ,String[] str3) {
        final String urlMp3 = str3[0];
        final String urlLyc = str3[1];
        final String urlBgs = str3[2];
        //存储地址
        //String downloadSavePath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/FLMusic";
        String downloadSavePath = getFilesDir().getAbsolutePath()+"/FLMusic/"+name;
        File file = new File(downloadSavePath);
        boolean isSuc = false;
        if (!file.exists()){
            isSuc = file.mkdir();
        }
        Log.d(TAG, "downloadSavePath: "+downloadSavePath);
        Log.d(TAG, "make file " + isSuc);
        final String saveMp3 = downloadSavePath+".mp3";
        final String saveLyc = downloadSavePath+"_lyc.txt";
        final String saveBgs = downloadSavePath+"_bgs.txt";

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(urlMp3).build();
                //下载mp3
                try {
                    Response response = client.newCall(request).execute();
                    byte[] content = response.body().bytes();
                    OutputStream os = new FileOutputStream(saveMp3);
                    os.write(content);
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //下载歌词
                Request request1 = new Request.Builder().url(urlLyc).build();
                try {
                    Response response = client.newCall(request1).execute();
                    byte[] content = response.body().bytes();
                    OutputStream os = new FileOutputStream(saveLyc);
                    os.write(content);
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //下载文案
                Request request2 = new Request.Builder().url(urlBgs).build();
                try {
                    Response response = client.newCall(request2).execute();
                    byte[] content = response.body().bytes();
                    OutputStream os = new FileOutputStream(saveBgs);
                    os.write(content);
                    os.flush();
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Intent intent1 = new Intent("notification_button");
        intent1.putExtra("noti",9);
        intent1.putExtra("index",index);
        sendBroadcast(intent1);
        stopSelf();

    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}