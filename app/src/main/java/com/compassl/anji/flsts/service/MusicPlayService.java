package com.compassl.anji.flsts.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

import java.io.IOException;

public class MusicPlayService extends Service {
    private static MediaPlayer mediaPlayer = new MediaPlayer();
    public MusicPlayService() {
    }




    public interface OnRequestListener{



    }
    public static class MyBinder extends Binder{
        private static OnRequestListener listener;

        public static void setOnRequestListener(OnRequestListener listener){
            MyBinder.listener = listener;
        }

        public void reset(){
            mediaPlayer.reset();
        }

        public void setDataSource(String url){
            try {
                mediaPlayer.setDataSource(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void prepare(){
            try {
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void seekTo(int i){
            mediaPlayer.seekTo(i);
        }

        public boolean isPlaying(){
            return mediaPlayer.isPlaying();
        }

        public void start(){
            mediaPlayer.start();
        }

        public void pause(){
            mediaPlayer.pause();
        }

        public int getDuration(){
            return mediaPlayer.getDuration();
        }

        public int getCurrentPosition(){
            return mediaPlayer.getCurrentPosition();
        }


        public void setLooping(boolean b){
            mediaPlayer.setLooping(b);
        }

        public void setOnCompletionListener(MediaPlayer.OnCompletionListener listener){
            mediaPlayer.setOnCompletionListener(listener);
        }

//        public void stop(){
//            mediaPlayer.stop();
//        }
//
//        public void release(){
//            mediaPlayer.release();
//        }

    }

    @Override
    public void onDestroy() {
        if (mediaPlayer!=null ) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
        super.onDestroy();
    }

    private MyBinder myBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }



}
