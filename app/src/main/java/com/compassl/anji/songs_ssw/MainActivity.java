package com.compassl.anji.songs_ssw;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.compassl.anji.songs_ssw.service.DownloadMusic;
import com.compassl.anji.songs_ssw.service.UpdateBackgroundPic;
import com.compassl.anji.songs_ssw.util.HttpUtil;
import com.compassl.anji.songs_ssw.util.InitialTool;
import com.compassl.anji.songs_ssw.util.TextHandle;
import com.compassl.anji.songs_ssw.util.MathUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.ElementType;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.wcy.lrcview.LrcView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,RvAdapter.OnItemClickListenerRV{
    private static final String TAG = "MA";

    private int SONG_ACCOUNT=0;
    private static final int MODE_LIST_LOOP = 1;
    private static final int MODE_SINGLE_LOOP = 2;
    private static final int MODE_RANDOM = 3;
    private static final int MODE_PLAY_BY_ORDER = 4;
    private static final int CURRENT_INFO = 0;
    private static final int CURRENT_LY = 1;
    private static final int CURRENT_BS = 2;
    private static int currentPage=CURRENT_LY;
    private static int MODE = MODE_LIST_LOOP;

    private int index=1;

    private ViewFlipper vf_ly_bs;
    private ImageButton bt_previous;
    private ImageButton bt_play_pause;
    private ImageButton bt_next;
    private ImageButton bt_mode;
    private ImageButton bt_download;
    private FloatingActionButton fbt_home;
    private DrawerLayout drawerLayout;
    private MyTextView tv_bs;
    private MyTextView tv_song_info;
    private List<Song> songList = new ArrayList<>();
    private RvAdapter adapter;
    private RecyclerView rv;
    private SeekBar sb_song_play_progress;
    private TextView tv_display_time_current;
    private TextView tv_display_time_total;
    private ImageView iv_background;
    private ImageButton ib_refresh_list;
    private MyLrcView lv_ly;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private NotiReceiver receiver;
    private IntentFilter filter;
    private Notification notification;
    private NotificationManager manager;
    private NotificationCompat.Builder builder;
    private AudioManager audioManager;
    //private EarReceiver receiver_ear;
    private String downloadPath;

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {

        }
    };

//    public EarReceiver earReceiver = new EarReceiver(){
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            changPP();
//        }
//    };

    private static final int SEEK_BAR_UPDATE = 1;
    private static final int GET_LY = 2;
    private static final int GET_BS = 3;
    private static final int GET_NEW_SONG = 4;
    //Handler 类，处理子线程发出的请求
    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case SEEK_BAR_UPDATE:
                    try {
                        sb_song_play_progress.setMax(mediaPlayer.getDuration());
                        sb_song_play_progress.setProgress(mediaPlayer.getCurrentPosition());
                    }catch (Exception e ){e.printStackTrace();}
                    lv_ly.updateTime(mediaPlayer.getCurrentPosition());
                    break;
                case GET_LY:
                    String lyc_content = message.getData().getString("lyc");
                    lv_ly.loadLrc(lyc_content);
                    tv_song_info.setText(TextHandle.getLrcInfo(lyc_content));
                    break;
                case GET_BS:
                    tv_bs.setText(message.getData().getString("bgs"));
                    break;
                case GET_NEW_SONG:
                    initRecyclerView();
                    ib_refresh_list.setImageResource(R.drawable.refresh_list);
                    Toast.makeText(MainActivity.this,"ok",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
            tv_display_time_current.setText(MathUtil.getDisplayTime(mediaPlayer.getCurrentPosition()));
        }
    };


    //监听通知栏发出的广播
    class NotiReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getIntExtra("noti",-1)){
                case 0:
                    playPre();
                    break;
                case 1:
                    changPP();
                    break;
                case 2:
                    playNext();
                    break;
                case 3:
                    onDestroy();
                    break;
                case 9:
                    int index_downloaded = intent.getIntExtra("index",-1);
                    if (index_downloaded!=-1){
                        Toast.makeText(MainActivity.this,"music "+songList.get(index_downloaded).getName()+" downloaded",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case 10:
                    //Toast.makeText(MainActivity.this,"new song",Toast.LENGTH_SHORT).show();
                    ib_refresh_list.setImageResource(R.drawable.refresh_list_new);
                    break;
                default:
                    break;
            }
        }
    }



    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置融合通知栏
        if (Build.VERSION.SDK_INT>=21){
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_main);

        //加载各类控件
        vf_ly_bs = (ViewFlipper) findViewById(R.id.vf_ly_bs);
        bt_previous = (ImageButton) findViewById(R.id.bt_previous);
        bt_play_pause = (ImageButton) findViewById(R.id.bt_play_and_pause);
        bt_next = (ImageButton) findViewById(R.id.bt_next);
        bt_mode = (ImageButton) findViewById(R.id.bt_mode);
        bt_download = (ImageButton) findViewById(R.id.bt_download);
        fbt_home = (FloatingActionButton) findViewById(R.id.fbt_menu);
        drawerLayout = (DrawerLayout) findViewById(R.id.dl_choose_song);
        tv_bs = (MyTextView) findViewById(R.id.tv_bacground_story_view);
        tv_song_info = (MyTextView) findViewById(R.id.tv_song_info);
        rv = (RecyclerView) findViewById(R.id.rv_for_choose);
        lv_ly = (MyLrcView) findViewById(R.id.lv_ly);
        tv_display_time_total = (TextView) findViewById(R.id.tv_display_time_total);
        tv_display_time_current = (TextView) findViewById(R.id.tv_display_time_current);
        sb_song_play_progress = (SeekBar) findViewById(R.id.sb_song_progress);
        iv_background = (ImageView) findViewById(R.id.iv_background);
        ib_refresh_list = (ImageButton) findViewById(R.id.ib_refresh_songList);

        //设置背景图案
        SharedPreferences prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
        int pic_id = prefs.getInt("id",-1);
        if (pic_id!=-1){
            String bingPic = prefs.getString("pic"+pic_id,null);
            if (bingPic != null){
                Glide.with(this).load(bingPic).into(iv_background);
            }
        }

        //设置背景图片的刷新
        final SwipeRefreshLayout layout_fresh = (SwipeRefreshLayout) findViewById(R.id.layout_fresh);
        layout_fresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                SharedPreferences prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
                                int pic_id = prefs.getInt("id",-1);
                                if (pic_id!=-1){
                                    Random random = new Random();
                                    int id_use = random.nextInt(8);
                                    String bingPic = prefs.getString("pic"+id_use,null);
                                    while (bingPic == null && id_use>1){
                                        bingPic = prefs.getString("pic"+(--id_use),null);
                                    }
                                    Glide.with(MainActivity.this).load(bingPic).into(iv_background);
                                }
                                //启动更新背景图片服务
                                Intent intent = new Intent(MainActivity.this, UpdateBackgroundPic.class);
                                startService(intent);
                                layout_fresh.setRefreshing(false);
                            }
                        });
                    }
                }).start();
            }
        });

        //设置通知栏广播监听事件
        receiver = new NotiReceiver();
        filter = new IntentFilter();
        filter.addAction("notification_button");
        registerReceiver(receiver,filter);

        //设置耳机监听
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        ComponentName name = new ComponentName(getPackageName(),EarReceiver.class.getName());
        int result = audioManager.requestAudioFocus(onAudioFocusChangeListener,AudioManager.STREAM_MUSIC,AudioManager.AUDIOFOCUS_GAIN);
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            audioManager.registerMediaButtonEventReceiver(name);
        }
//        receiver_ear = new EarReceiver();
//        receiver_ear.setmEarListener(new EarReceiver.EarListener() {
//            @Override
//            public void onReceiveEar() {
//                changPP();
//            }
//        });
//        IntentFilter filter_ear = new IntentFilter("android.intent.action.MEDIA_BUTTON");
//        registerReceiver(receiver_ear,filter_ear);

        //为按钮设置监听事件
        bt_previous.setOnClickListener(this);
        bt_play_pause.setOnClickListener(this);
        bt_next.setOnClickListener(this);
        bt_mode.setOnClickListener(this);
        bt_download.setOnClickListener(this);
        fbt_home.setOnClickListener(this);
        ib_refresh_list.setOnClickListener(this);

        vf_ly_bs.setDisplayedChild(CURRENT_LY);
        //为歌曲信息的TextView设置属性
        tv_song_info.getPaint().setFakeBoldText(true);
        tv_song_info.setOntouchListenerM(new MyTextView.onTouchListenerM() {
            @Override
            public void onTouch(String direction) {
                if ("previous".equals(direction) && currentPage != CURRENT_INFO){
                    changeToPrevious();
                }else if ("next".equals(direction) && currentPage != CURRENT_BS){
                    changToNext();
                }
            }
            @Override
            public void onTouch(int Y) {tv_song_info.scrollBy(0,Y);}
            @Override
            public void resetY() {tv_song_info.scrollTo(0, 0);}
            @Override
            public void setToBottom(int Y) {tv_song_info.scrollTo(0,Y);}
        });
        //歌词控件界面的属性设置
        lv_ly.setLabel("no lyrics");
        lv_ly.setOnPlayClickListener(new LrcView.OnPlayClickListener() {
            @Override
            public boolean onPlayClick(long time) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                if (!mediaPlayer.isPlaying()){
                    mediaPlayer.start();
                }
                return true;
            }
        });
        lv_ly.setmOnTouchListenerM(new MyLrcView.OnTouchListenerM() {
            @Override
            public void onTouchEventM(String direction) {
                if ("previous".equals(direction) && currentPage != CURRENT_INFO){
                    changeToPrevious();
                }
                if ("next".equals(direction) && currentPage != CURRENT_BS){
                    changToNext();
                }
            }
        });
        //为背景文案的TextView设置属性
        tv_bs.getPaint().setFakeBoldText(true);
        tv_bs.setOntouchListenerM(new MyTextView.onTouchListenerM() {
            @Override
            public void onTouch(String direction) {
                if ("previous".equals(direction) && currentPage != CURRENT_INFO){
                    changeToPrevious();
                }else if ("next".equals(direction) && currentPage != CURRENT_BS){
                    changToNext();
                }
            }
            @Override
            public void onTouch(int Y) {
                tv_bs.scrollBy(0,Y);
            }
            @Override
            public void resetY() {
                tv_bs.scrollTo(0, 0);
            }
            @Override
            public void setToBottom(int Y) {
                tv_bs.scrollTo(0,Y);
            }

        });

        //为显示歌曲总时间和当前进度的TextView设置属性，使文字加粗
        tv_display_time_current.getPaint().setFakeBoldText(true);
        tv_display_time_total.getPaint().setFakeBoldText(true);

        //歌曲进度显示条设置拖动监听器和子线程操作
        sb_song_play_progress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser){
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        Thread.sleep(100);
                    }catch (Exception e){e.printStackTrace();}
                    Message message = new Message();
                    message.what = SEEK_BAR_UPDATE;
                    handler.sendMessage(message);
                }
            }
        }).start();

        //音乐下载地址
        downloadPath = getFilesDir().getAbsolutePath()+"/FLMusic/";
        //判断有无配置文件，若没有，则创建
        if (!hasSongInfoText()){
            loadINfo();
        }

        //加载拉动菜单中recyclerview布局的适配器
        initRecyclerView();

        //切换歌曲后，由于是第一次打开，歌曲不进行自动播放，而在
        //changSong(int index)方法中的末尾会把该按钮设为暂停图案，故在此处需要手动设置播放图案。
        bt_play_pause.setImageResource(R.drawable.play);
        //切换到第一首歌的播放界面
        if (SONG_ACCOUNT>0) {
            changeSong(1);
            createNotification();
        }

    }

    private void loadINfo() {
        try {
            InputStream is = getAssets().open("song_info.txt");
            byte[] buf = new byte[is.available()];
            is.read(buf);
            String filePath = downloadPath+"song_info.txt";
            OutputStream os = new FileOutputStream(filePath);
            os.write(buf);
            os.flush();
            is.close();
            os.close();
            //判断有无图片文件夹，若没有则创建
            hasImg(downloadPath+"img");
            for (int i = 1;i<=SONG_ACCOUNT;i++){
                String index_str = i>9?i+"":"0"+i;
                InputStream is1 = new FileInputStream("file:///android_asset/img/s"+index_str+".jpg");
                byte[] buf1 = new byte[is1.available()];
                is1.read(buf1);
                is1.close();
                String imgPath = downloadPath+"/img/s"+index_str+".jpg";
                OutputStream os1 = new FileOutputStream(imgPath);
                os1.write(buf1);
                os1.flush();
                os1.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private boolean hasSongInfoText() {
        File file = new File(downloadPath);
        if (!file.exists()){
            file.mkdir();
        }
        File file1 = new File(downloadPath+"song_info.txt");
        return file1.exists();
    }
    private void hasImg(String path){
        File file = new File(path);
        if (!file.exists()){
            file.mkdir();
        }
    }

    //方法：加载RecyclerView
    private void initRecyclerView() {
        songList=InitialTool.initSongChoose(MainActivity.this);
        SONG_ACCOUNT = songList.size();
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL);
        rv.setLayoutManager(manager);
        rv.setHasFixedSize(true);
        adapter = new RvAdapter(songList);
        rv.setAdapter(adapter);
        adapter.setOnItemClickListenerRV(this);
        //是否已经下载完成的数组初始化
    }
    //方法：转换歌曲
    private void changeSong(int i) {
        mediaPlayer.reset();
        index=i;
        if (MODE == MODE_RANDOM) {
            int temp = new Random().nextInt(SONG_ACCOUNT)+1;
            index = (temp==index)?temp+1:temp;
            index = (index>SONG_ACCOUNT)?1:index;
        }
        if (MODE == MODE_PLAY_BY_ORDER && index == 1){
            onDestroy();
        }
        //加载音乐、歌词和文案
        if (!setMLB()){
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
        //显示歌词
        vf_ly_bs.setDisplayedChild(CURRENT_LY);
        currentPage = CURRENT_LY;
    }

    private boolean setMLB() {
        boolean[] isDownloaded = isDownloaded(index);
        String name = "";
        boolean isN = false;
        String[] str3 = new String[]{"","",""};
        if (!(isDownloaded[0]&&isDownloaded[1]&&isDownloaded[2])){
            str3 = TextHandle.getWholeFilePath(MainActivity.this,index);
            isN = isNetWorkAvailable();
        }else {
            name = songList.get(index-1).getName();
        }
        //加载音乐
        if (isDownloaded[0]){
            String saveMp3 = downloadPath+name+".mp3";
            try {
                mediaPlayer.setDataSource(saveMp3);
                mediaPlayer.prepare();
                tv_display_time_total.setText(MathUtil.getDisplayTime(mediaPlayer.getDuration()));
                if (mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    bt_play_pause.setImageResource(R.drawable.pause);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (isN){
            try {
                mediaPlayer.setDataSource(str3[0]);
                mediaPlayer.prepare();
                tv_display_time_total.setText(MathUtil.getDisplayTime(mediaPlayer.getDuration()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            return false;
        }
        //加载歌词和信息
        if (isDownloaded[1]){
            String saveLyc = downloadPath+name+"_lyc.txt";
            try {
                InputStream is = new FileInputStream(saveLyc);
                byte[] buffer = new byte[is.available()];
                is.read(buffer);
                String lrc_content = new String(buffer);
                is.close();
                //装载歌词
                lv_ly.loadLrc(lrc_content);
                //歌词中的歌曲信息
                String info = TextHandle.getLrcInfo(lrc_content);
                tv_song_info.setText(info);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if (isN){
            HttpUtil.sendOkHttpRequest(str3[1], new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Message message = new Message();
                    message.what = GET_LY;
                    Bundle data = new Bundle();
                    data.putString("lyc",response.body().string());
                    message.setData(data);
                    handler.sendMessage(message);
                }
            });
        }else {
            lv_ly.loadLrc("无网络连接，暂无歌词");
            tv_song_info.setText("无网络连接，暂无信息");
        }
        //装载文案
        if(isDownloaded[2]){
            String saveBgs = downloadPath+name+"_bgs.txt";
            try {
                InputStream is1 = new FileInputStream(saveBgs);
                byte[] buf = new byte[is1.available()];
                is1.read(buf);
                String bgs = new String(buf);
                tv_bs.setText(bgs);
                is1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if(isN){
            HttpUtil.sendOkHttpRequest(str3[2], new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Message message = new Message();
                    message.what = GET_BS;
                    Bundle data = new Bundle();
                    data.putString("bgs",response.body().string());
                    message.setData(data);
                    handler.sendMessage(message);
                }
            });
        }else {
            tv_bs.setText("没网，暂无");
        }
        return true;
    }

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
        Intent intent1 = new Intent(this,MainActivity.class);
        int flag = PendingIntent.FLAG_UPDATE_CURRENT;
        PendingIntent pi = PendingIntent.getActivity(this,3,intent1,flag);
        builder = new NotificationCompat.Builder(this);
        builder.setWhen(System.currentTimeMillis())
                .setContentTitle("生生忘").setContentText(songList.get(index-1).getName())
                .setSmallIcon(R.mipmap.ic_launcher)
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

    private void changeToPrevious() {
        vf_ly_bs.setInAnimation(this,R.anim.push_right_in);
        vf_ly_bs.setOutAnimation(this,R.anim.push_right_out);
        vf_ly_bs.showPrevious();
        switch (currentPage){
            case CURRENT_INFO :
                currentPage = CURRENT_BS;
                break;
            case CURRENT_LY :
               currentPage = CURRENT_INFO;
                break;
            case CURRENT_BS :
                currentPage = CURRENT_LY;
                break;
            default:
                break;
        }
    }
    private void changToNext() {
        vf_ly_bs.setInAnimation(this,R.anim.push_left_in);
        vf_ly_bs.setOutAnimation(this,R.anim.push_left_out);
        vf_ly_bs.showNext();
        switch (currentPage){
            case CURRENT_INFO :
                currentPage = CURRENT_LY;
                break;
            case CURRENT_LY :
                currentPage = CURRENT_BS;
                break;
            case CURRENT_BS :
                currentPage = CURRENT_INFO;
                break;
            default:
                break;
        }
    }


    //button 监听事件
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_previous:
                playPre();
                break;
            case R.id.bt_play_and_pause:
                changPP();
                break;
            case R.id.bt_next:
                playNext();
                break;
            case R.id.bt_mode:
                if (MODE==MODE_LIST_LOOP){
                    MODE = MODE_SINGLE_LOOP;
                    mediaPlayer.setLooping(true);
                    bt_mode.setImageResource(R.drawable.single);
                    Toast.makeText(MainActivity.this,"单曲循环",Toast.LENGTH_SHORT).show();
                }else if (MODE == MODE_SINGLE_LOOP){
                    MODE = MODE_RANDOM;
                    mediaPlayer.setLooping(false);
                    bt_mode.setImageResource(R.drawable.random_play);
                    Toast.makeText(MainActivity.this,"随机播放",Toast.LENGTH_SHORT).show();
                }else if (MODE == MODE_RANDOM){
                    MODE = MODE_PLAY_BY_ORDER;
                    mediaPlayer.setLooping(false);
                    bt_mode.setImageResource(R.drawable.play_by_order);
                    Toast.makeText(MainActivity.this,"顺序播放",Toast.LENGTH_SHORT).show();
                }else if(MODE == MODE_PLAY_BY_ORDER){
                    MODE = MODE_LIST_LOOP;
                    mediaPlayer.setLooping(false);
                    bt_mode.setImageResource(R.drawable.allplay);
                    Toast.makeText(MainActivity.this,"列表循环",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_download:
                boolean isn = isNetWorkAvailable();
                boolean isd = isDownloaded(index)[0];
                if (isn && !isd){
                    Intent intent = new Intent(MainActivity.this, DownloadMusic.class);
                    intent.putExtra("id",index);
                    intent.putExtra("name",songList.get(index-1).getName());
                    startService(intent);
                }else if (isd){
                    Toast.makeText(MainActivity.this,"already downloaded",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this,"no network",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.fbt_menu:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.ib_refresh_songList:
                loadNewSong();
                break;
            default:
                break;
        }
    }
    //更新歌曲列表
    private void loadNewSong() {
        String url = "http://10.0.2.2:90/song_info.txt";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String savePath = downloadPath+"song_info.txt";
                byte[] buf = response.body().bytes();
                File file = new File(savePath);
                if (file.exists()){
                    file.delete();
                }
                OutputStream os = new FileOutputStream(savePath);
                os.write(buf);
                os.flush();
                os.close();
                for (int i = 1;i<=SONG_ACCOUNT;i++){
                    final String i_str = i>9?""+i:"0"+i;
                    final int ii = i;
                    HttpUtil.sendOkHttpRequest("http://10.0.2.2:90/song_img/s" + i_str + ".jpg", new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                        }
                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            byte[] buf1 = response.body().bytes();
                            String imgPath = downloadPath+"/img/s"+i_str+".jpg";
                            OutputStream os1 = new FileOutputStream(imgPath);
                            os1.write(buf1);
                            os1.flush();
                            os1.close();
                            if (ii==SONG_ACCOUNT){
                                Message message = new Message();
                                message.what = GET_NEW_SONG;
                                handler.sendMessage(message);
                            }
                        }
                    });
                }
            }
        });
    }


    private void playNext() {
        if (index==SONG_ACCOUNT){
            changeSong(1);
        }else {
            changeSong(++index);
        }
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        createNotification();
    }
    private void changPP() {
        if (!mediaPlayer.isPlaying()){
            bt_play_pause.setImageResource(R.drawable.pause);
            if (!mediaPlayer.isPlaying()){
                mediaPlayer.start();
            }
        }else {
            bt_play_pause.setImageResource(R.drawable.play);
            if (mediaPlayer.isPlaying()){
                mediaPlayer.pause();
            }
        }
        createNotification();
    }
    private void playPre() {
        if (index==1){
            changeSong(SONG_ACCOUNT);
        }else {
            changeSong(--index);
        }
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
        }
        createNotification();
    }

    //判断是否已经下载
    private boolean[] isDownloaded(int id){
        String name = songList.get(id-1).getName();
        String saveMp3 = downloadPath+name+".mp3";
        String saveLyc = downloadPath+name+"_lyc.txt";
        String saveBgs = downloadPath+name+"_bgs.txt";
        File mp3File = new File(saveMp3);
        File lycFile = new File(saveLyc);
        File bgsFile = new File(saveBgs);
        return new boolean[]{mp3File.exists(),lycFile.exists(),bgsFile.exists()};
    }

    @Override
    public void onItemClickRV(int position) {
        index=position+1;
        drawerLayout.closeDrawers();
        changeSong(index);
        if (!mediaPlayer.isPlaying()){
            mediaPlayer.start();
            bt_play_pause.setImageResource(R.drawable.pause);
        }
        createNotification();
    }
    @Override
    public void onBackPressed() {
        final boolean isPlaying = mediaPlayer.isPlaying();
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("退出");
        builder.setMessage("确认要退出应用吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onDestroy();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (isPlaying){
                    mediaPlayer.start();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer!=null){
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        manager.cancel(1);
        unregisterReceiver(receiver);
        audioManager.abandonAudioFocus(onAudioFocusChangeListener);

        finish();
    }
}
