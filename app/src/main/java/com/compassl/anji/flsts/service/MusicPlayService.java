package com.compassl.anji.flsts.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.compassl.anji.flsts.activity.MusicActivity;
import com.compassl.anji.flsts.bean.Song;
import com.compassl.anji.flsts.db.SongInfo;
import com.compassl.anji.flsts.util.InitialTool;
import com.compassl.anji.flsts.R;

import org.litepal.crud.DataSupport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MusicPlayService extends Service {
    public MusicPlayService() {
    }
    private int index=0;
    private int FROM_WHERE = 1;
    private static final int FROM_NET = 0;
    private static final int FROM_FILE = 1;

    private int SONG_ACCOUNT=0;
    private static final int MODE_LIST_LOOP = 1;
    private static final int MODE_SINGLE_LOOP = 2;
    private static final int MODE_RANDOM = 3;
    private static final int MODE_PLAY_BY_ORDER = 4;
    private static int MODE = MODE_LIST_LOOP;

    private String downloadPath;

    private Notification notification;
    private NotificationManager manager;
    private NotificationCompat.Builder builder;
    private NotiReceiver receiver;
    private IntentFilter filter;

    private List<Song> songList = new ArrayList<>();
    private static MediaPlayer mediaPlayer;


    //监听通知栏发出的广播
    class NotiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("noti",-1)){
                case -1:
                    startActivity(new Intent(context,MusicActivity.class));
                    break;
                //通知栏广播
                case 0: //播放上一首
                    changePre();
                    mediaPlayer.start();
                    break;
                case 1: //播放——暂停
                    changPP();
                    break;
                case 2: //播放下一首
                    changNext();
                    mediaPlayer.start();
                    break;
                case 3: //销毁
                    myBinder.listener.closeMusicActivity();
                    stopSelf();
                    break;
                //耳机监听广播
                case 9:
                    abortBroadcast();
                    changPP();
                    break;
                default:
                    break;
            }
        }
    }
    private void changNext() {
        if (index!=SONG_ACCOUNT){
            changeSong(++index);
        }else{
            changeSong((index=1));
        }
    }
    private void changPP() {
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            myBinder.listener.changePP("start");
        }else {
            mediaPlayer.start();
            myBinder.listener.changePP("pause");
        }
        createNotification();
    }
    private void changePre() {
        if (index!=1){
            changeSong(--index);
        }else {
            changeSong((index = SONG_ACCOUNT));
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("mmediaplayer", "onCreate: ");
        mediaPlayer = new MediaPlayer();
        downloadPath = getFilesDir().getAbsolutePath()+"/FLMusic/";
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
        Boolean isFirst = prefs.getBoolean("isFirst",true);
        if (isFirst){
            InitialTool.loadInfo(this,downloadPath,null,1);
            prefs.edit().putBoolean("isFirst",false).apply();
        }
        songList = InitialTool.initSongChoose(this);
        SONG_ACCOUNT = songList.size();
        //设置通知栏广播监听事件
        receiver = new NotiReceiver();
        filter = new IntentFilter();
        filter.addAction("notification_button");
        registerReceiver(receiver,filter);
        return super.onStartCommand(intent, flags, startId);
    }


    private void changeSong(int i){
        if (songList.size()<i){
            songList = InitialTool.initSongChoose(this);
            SONG_ACCOUNT = songList.size();
        }
        index = i;
        mediaPlayer.reset();
        if (MODE == MODE_RANDOM) {
            int temp = new Random().nextInt(SONG_ACCOUNT)+1;
            index = (temp==index)?temp+1:temp;
            index = (index>SONG_ACCOUNT)?1:index;
        }
        if (MODE == MODE_PLAY_BY_ORDER && index == 1){
            onDestroy();
        }
        boolean[] isDownloaded = isDownloadeded(index);
        if (isDownloaded[0]){
            String saveMp3 = downloadPath+songList.get(index-1).getName()+".mp3";
            //加载音乐
            try {
                mediaPlayer.setDataSource(saveMp3);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            FROM_WHERE = FROM_FILE;
            myBinder.listener.changeInfo_Ly_Bs(index,"file");
        }else if (isNetWorkAvailable()){
            FROM_WHERE = FROM_NET;
            try {
                List<SongInfo> list = DataSupport.select("urlMp3").where("song_id=?",index+"").find(SongInfo.class);
                String urlMp3 = list.get(list.size()-1).getUrlMp3();
                mediaPlayer.setDataSource(urlMp3);
                mediaPlayer.prepare();
                myBinder.listener.changeInfo_Ly_Bs(index,"net");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else {
            myBinder.listener.noNetWork(index);
            return;
        }
        //播放器的结束时监听
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                index = (index>=SONG_ACCOUNT)?0:index;
                changeSong(++index);
                mediaPlayer.start();
            }
        });
        mediaPlayer.start();
        createNotification();
    }

    //判断是否已经下载
    private boolean[] isDownloadeded(int id){
        String name = songList.get(id-1).getName();
        String saveMp3 = downloadPath+name+".mp3";
        String saveLyc = downloadPath+name+"_lyc.txt";
        String saveBgs = downloadPath+name+"_bgs.txt";
        File mp3File = new File(saveMp3);
        File lycFile = new File(saveLyc);
        File bgsFile = new File(saveBgs);
        return new boolean[]{mp3File.exists(),lycFile.exists(),bgsFile.exists()};
    }
    //判断网络是否可用
    private boolean isNetWorkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager!=null){
            NetworkInfo info = manager.getActiveNetworkInfo();
            if (info!=null && info.isAvailable() && info.getState()==NetworkInfo.State.CONNECTED){
                return true;
            }
        }
        return false;
    }

    //方法：通知栏
    private void createNotification(){
        Intent intent1 = new Intent(this,MusicActivity.class);
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getActivity(this,3,intent1,flag);
        builder = new NotificationCompat.Builder(this);
        builder.setWhen(System.currentTimeMillis())
                .setContentTitle("生生忘").setContentText(songList.get(index-1).getName())
                .setSmallIcon(R.mipmap.flsts)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.home))
                .setContentIntent(pi)
                .setContent(getRemoteView())
                .setOngoing(true);
        notification = builder.build();
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1,notification);
    }
    private RemoteViews getRemoteView(){
        final RemoteViews view = new RemoteViews(getPackageName(),R.layout.foreground_layout);
        view.setOnClickPendingIntent(R.layout.foreground_layout,getClickPendingIntent(-1));
        view.setOnClickPendingIntent(R.id.ib_fore_pre,getClickPendingIntent(0));
        view.setOnClickPendingIntent(R.id.ib_fore_p_p,getClickPendingIntent(1));
        view.setOnClickPendingIntent(R.id.ib_fore_next,getClickPendingIntent(2));
        view.setOnClickPendingIntent(R.id.fore_close,getClickPendingIntent(3));
        view.setTextViewText(R.id.tv_fore_song_name,songList.get(index-1).getName());
        view.setImageViewBitmap(R.id.fore_img,BitmapFactory.decodeFile(songList.get(index-1).getImgRes()));
        if (mediaPlayer.isPlaying()){
            view.setImageViewResource(R.id.ib_fore_p_p,R.drawable.pause);
        }else {
            view.setImageViewResource(R.id.ib_fore_p_p,R.drawable.play);
        }
        return view;
    }
    private PendingIntent getClickPendingIntent(int notificationPre) {
        Intent intent = new Intent("notification_button");
        intent.putExtra("noti",notificationPre);
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent clickIntent = PendingIntent.getBroadcast(this, notificationPre, intent, flag );
        return clickIntent;
    }

    private MyBinder myBinder = new MyBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }
    public interface OnRequestListener{
        void changeInfo_Ly_Bs(int i,String fileOrNet);
        void changePP(String startOrPause);
        void noNetWork(int id);
        void setL_B(int id, int FromWhere);
        void closeMusicActivity();
    }
    public class MyBinder extends Binder{
        private OnRequestListener listener;
        public boolean isPlaying(){
            return mediaPlayer.isPlaying();
        }
        public void start(){mediaPlayer.start();}
        public void ACreate(){
            if (index!=0){
                listener.setL_B(index,FROM_WHERE);
            }
        }
        public void setOnRequestListener(OnRequestListener listener){
            this.listener = listener;
        }
        public void seekTo(int i){
            mediaPlayer.seekTo(i);
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
        public void setMode(int mode){
            MODE = mode;
        }
        public int getIndex(){
            return index;
        };
        public void playPre(){
            if (index == 1){
                changSong((index = SONG_ACCOUNT));
            }else {
                changeSong(--index);
            }
            //mediaPlayer.start();
        }
        public void playNext(){
            if (index == SONG_ACCOUNT){
                changSong((index = 1));
            }else {
                changeSong(++index);
            }
            //mediaPlayer.start();
        }
        public void changP_P(){
            if (mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }else {
                mediaPlayer.start();
            }
        }
        public void changSong(int id){
            changeSong(id);
            //mediaPlayer.start();
        }
    }

    @Override
    public void onDestroy() {
        if (mediaPlayer!=null ) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
        }
   //     android.os.Debug.waitForDebugger();
        unregisterReceiver(receiver);
        manager.cancel(1);
        Log.d("mmediaplayer", "onDestroy: ");
        super.onDestroy();
    }
}
