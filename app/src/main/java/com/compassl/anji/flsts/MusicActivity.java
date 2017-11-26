package com.compassl.anji.flsts;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.compassl.anji.flsts.service.DownloadMusic;
import com.compassl.anji.flsts.service.NewSongListeningService;
import com.compassl.anji.flsts.service.MusicPlayService;
import com.compassl.anji.flsts.service.UpdateBackgroundPic;
import com.compassl.anji.flsts.util.HttpUtil;
import com.compassl.anji.songs_ssw.R;
import com.compassl.anji.flsts.util.InitialTool;
import com.compassl.anji.flsts.util.TextHandle;
import com.compassl.anji.flsts.util.MathUtil;
import com.tencent.mm.sdk.openapi.IWXAPI;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import me.wcy.lrcview.LrcView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class MusicActivity extends AppCompatActivity implements View.OnClickListener,RvAdapter.OnItemClickListenerRV{
    private static final String APP_ID = "";
    private IWXAPI wxapi;

    private int index= 1;
    private static final int CURRENT_INFO = 0;
    private static final int CURRENT_LY = 1;
    private static final int CURRENT_BS = 2;
    private static int currentPage=CURRENT_LY;

    private SharedPreferences prefs;
    private List<Song> songList = new ArrayList<>();


    private ViewFlipper vf_ly_bs;
    private ImageButton bt_previous;
    private ImageButton bt_play_pause;
    private ImageButton bt_next;
    private ImageButton bt_mode;
    private ImageButton bt_download;
    private ImageButton bt_share_to_wx;
    private FloatingActionButton fbt_home;
    private DrawerLayout drawerLayout;
    private MyTextView tv_bs;
    private MyTextView tv_song_info;
    private RvAdapter adapter;
    private RecyclerView rv;
    private SeekBar sb_song_play_progress;
    private TextView tv_display_time_current;
    private TextView tv_display_time_total;
    private ImageView iv_background;
    private ImageButton ib_refresh_list;
    private ProgressBar pb_download;
    private MyLrcView lv_ly;
    private String downloadPath;
    private SwipeRefreshLayout layout_fresh;

    private static final int MODE_LIST_LOOP = 1;
    private static final int MODE_SINGLE_LOOP = 2;
    private static final int MODE_RANDOM = 3;
    private static final int MODE_PLAY_BY_ORDER = 4;
    private static int MODE = MODE_LIST_LOOP;
    private static final int SEEK_BAR_UPDATE = 1;
    private static final int GET_LY = 2;
    private static final int GET_BS = 3;
    private static final int GET_NEW_SONG = 4;
    private boolean update = true;

    //Handler 类，处理子线程发出的请求
    private Handler handler = new Handler(){
        public void handleMessage(Message message){
            switch (message.what){
                case SEEK_BAR_UPDATE:
                    if (update && mediaPlayer!=null && !isOnPause){
                        try {
                            sb_song_play_progress.setProgress(mediaPlayer.getCurrentPosition());
                            tv_display_time_current.setText(MathUtil.getDisplayTime(mediaPlayer.getCurrentPosition()));
                        }catch (Exception e ){e.printStackTrace();}
                        lv_ly.updateTime(mediaPlayer.getCurrentPosition());
                    }
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
                    Toast.makeText(MusicActivity.this,"ok",Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };


    private boolean hasNewSong = false;
    private ServiceConnection conn_en = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            NewSongListeningService.UBPBinder newSongBinder = (NewSongListeningService.UBPBinder) service;
            NewSongListeningService.OnUpdateFinishListener listener = new NewSongListeningService.OnUpdateFinishListener() {
                @Override
                public void updateUI() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hasNewSong = true;
                            ib_refresh_list.setImageResource(R.drawable.refresh_list_new);
                        }
                    });
                }
            };
            newSongBinder.setOnUpdateFinishListener(listener);
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };
    private MusicPlayService.MyBinder mediaPlayer;
    ServiceConnection conn_mp = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaPlayer = (MusicPlayService.MyBinder) service;
            mediaPlayer.setOnRequestListener(new MusicPlayService.OnRequestListener() {
                @Override
                public void changeInfo_Ly_Bs(int i,String fileOrNet) {
                    index = i;
                    setLB(i,fileOrNet);
                }
                @Override
                public void changePP(String startOrPause) {
                    if ("pause".equals(startOrPause)){
                        bt_play_pause.setImageResource(R.drawable.pause);
                    }else if ("start".equals(startOrPause)){
                        bt_play_pause.setImageResource(R.drawable.play);
                    }
                }
                @Override
                public void noNetWork(int id) {
                    index = id;
                    Toast.makeText(MusicActivity.this,"not internet",Toast.LENGTH_SHORT).show();
                }

                @Override
                public void setL_B(int id, int FromWhere) {
                    setLB(id, FromWhere==0?"net":"file");
                }

                @Override
                public void closeMusicActivity() {
                    finish();
                }
            });
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };




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
        bt_share_to_wx = (ImageButton) findViewById(R.id.bt_share_to_wx);
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
        pb_download = (ProgressBar) findViewById(R.id.pb_download);
        layout_fresh = (SwipeRefreshLayout) findViewById(R.id.layout_fresh);
        //为按钮设置监听事件
        bt_previous.setOnClickListener(this);
        bt_play_pause.setOnClickListener(this);
        bt_next.setOnClickListener(this);
        bt_mode.setOnClickListener(this);
        bt_share_to_wx.setOnClickListener(this);
        bt_download.setOnClickListener(this);
        fbt_home.setOnClickListener(this);
        ib_refresh_list.setOnClickListener(this);
        layout_fresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        int id_use;
                        Random random = new Random();
                        id_use = random.nextInt(8);
                        File file = new File(downloadPath+"B"+id_use+".txt");
                        if (!file.exists()){
                            id_use = 0;
                        }
                        if (id_use == 0){
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    layout_fresh.setRefreshing(false);
                                    Glide.with(MusicActivity.this).load(R.drawable.background_pic_default).into(iv_background);
                                }
                            });
                        }else {
                            String savePath = downloadPath+"backgroundPic/B"+id_use+".txt";
                            try {
                                InputStream is = new FileInputStream(savePath);
                                byte[] buf = new byte[is.available()];
                                is.read(buf);
                                is.close();
                                final String url = new String(buf);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        layout_fresh.setRefreshing(false);
                                        Glide.with(MusicActivity.this).load(url).into(iv_background);
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        prefs.edit().putInt("id",id_use).apply();
                    }
                });
                thread.start();
            }
        });
        //音乐下载地址
        downloadPath = getFilesDir().getAbsolutePath()+"/FLMusic/";
        if (isNetWorkAvailable()){
           startSPService();
        }else {
            listenNewsongAccordingToNet();
        }


        if (isServiceRunning(this,"com.compassl.anji.flsts.service.MusicPlayService")){
            //启动歌曲播放服务，如果服务已经启动，则直接bind
            Intent intent_mp = new Intent(MusicActivity.this,MusicPlayService.class);
            bindService(intent_mp,conn_mp,BIND_AUTO_CREATE);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initOther();
                            mediaPlayer.ACreate();
                        }
                    });
                }
            }).start();
        }else {
            //启动歌曲播放服务，如果服务未启动，则先start
            Intent intent_mp = new Intent(MusicActivity.this,MusicPlayService.class);
            startService(intent_mp);
            bindService(intent_mp,conn_mp,BIND_AUTO_CREATE);
            //初始化界面
            initChangSong();
        }

        Log.d("changeactivity", "music onCreate: ");

    }

    ////////////////////////////////////////////////////
    //OnCreate结束

    //监听网络状态更新音乐和背景图片
    private void listenNewsongAccordingToNet() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000*60*10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        if (isNetWorkAvailable()){
            startSPService();
        }else {
            listenNewsongAccordingToNet();
        }
    }

    private void startSPService() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //启动更新背景图片服务
                Intent intent_ubp = new Intent(MusicActivity.this, UpdateBackgroundPic.class);
                startService(intent_ubp);
                //启动新歌更新服务
                Intent intent_en = new Intent(MusicActivity.this,NewSongListeningService.class);
                startService(intent_en);
                bindService(intent_en,conn_en,BIND_AUTO_CREATE);
            }
        }).start();
    }


    /*
     * 判断服务是否启动,context上下文对象 ，className服务的name
     */
    public static boolean isServiceRunning(Context mContext, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) mContext
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = activityManager
                .getRunningServices(30);
        if (!(serviceList.size() > 0)) {
            return false;
        }
        for (int i = 0; i < serviceList.size(); i++) {
            String get = serviceList.get(i).service.getClassName();
            if (get.equals(className)) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }


    //方法：初始化界面
    private void initChangSong(){
        lv_ly.loadLrc("[00:05.000]choose one song please");
        tv_bs.setText("请选择歌曲");
        tv_song_info.setText("请选择歌曲");
        tv_display_time_total.setText("00.00");
        tv_display_time_current.setText("00.00");
        new Thread(new Runnable() {
            @Override
            public void run() {
                //设置背景图案
                prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
                final int pic_id = prefs.getInt("id",-1);
                if (pic_id!=-1){
                    String savePath = downloadPath+"backgroundPic/B"+pic_id+".txt";
                    try {
                        InputStream is = new FileInputStream(savePath);
                        byte[] buf = new byte[is.available()];
                        is.read(buf);
                        is.close();
                        final String url = new String(buf);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.with(MusicActivity.this).load(url).into(iv_background);
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    //初始化其它
    private void initOther(){
        //加载拉动菜单中recyclerview布局的适配器
        initRecyclerView();
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
        lv_ly.setLabel("请选择歌曲");
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
        lv_ly.setOnPlayClickListener(new LrcView.OnPlayClickListener() {
            @Override
            public boolean onPlayClick(long time) {
                mediaPlayer.seekTo(mediaPlayer.getCurrentPosition());
                return true;
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

        //判断有无配置文件，若没有，则创建
        prefs = getSharedPreferences("bingPic",MODE_PRIVATE);
        int count = prefs.getInt("song_count_show",9);
        if (!hasSongInfoText()) {
            InitialTool.loadInfo(MusicActivity.this, downloadPath, null,count);
        }
        vf_ly_bs.setDisplayedChild(1);
    }




    private boolean hasSongInfoText() {
        File file = new File(downloadPath+"flsts");
        boolean re = file.exists();
        if (!re){
            file.mkdirs();
        }
        return re;
    }

    //方法：加载RecyclerView
    private void initRecyclerView() {
        songList = InitialTool.initSongChoose(MusicActivity.this);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        rv.setLayoutManager(manager);
        rv.setHasFixedSize(true);
        adapter = new RvAdapter(songList);
        rv.setAdapter(adapter);
        adapter.setOnItemClickListenerRV(MusicActivity.this);
    }



    private boolean setLB(int index,String fileOrNet) {
        //加载歌词和信息
        if ("file".equals(fileOrNet)){
            String name = songList.get(index-1).getName();
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
                //装载文案
                String saveBgs = downloadPath+name+"_bgs.txt";
                InputStream is1 = new FileInputStream(saveBgs);
                byte[] buf = new byte[is1.available()];
                is1.read(buf);
                String bgs = new String(buf);
                tv_bs.setText(bgs);
                is1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else if ("net".equals(fileOrNet)){
            //歌词和信息
            String[] str3 = TextHandle.getWholeFilePath(index);
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
            //文案
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
            lv_ly.loadLrc("无网络连接，暂无歌词");
            tv_song_info.setText("无网络连接，暂无信息");
            return false;
        }
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
        sb_song_play_progress.setMax(mediaPlayer.getDuration());
        tv_display_time_total.setText(MathUtil.getDisplayTime(mediaPlayer.getDuration()));
        return true;
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

    //private AlertDialog dialog;
    private void share_to_wx() {
        //wxapi = WXAPIFactory.createWXAPI(MusicActivity.this,APP_ID);
        //启动微信
        //wxapi.openWXApp();
        AlertDialog.Builder builder = new AlertDialog.Builder(MusicActivity.this,R.style.AlertDialog);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        //
        final AlertDialog dialog = builder.create();
        View view = inflater.inflate(R.layout.wx_choose_dialog,null);
        dialog.setView(view,0,0,0,0);
        dialog.setIcon(R.drawable.pause);
        Window win = dialog.getWindow();
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.x = 0;
        params.y = 300;
        if (win != null) {
            win.setAttributes(params);
        }
        dialog.show();
        ImageButton ib_hy = (ImageButton) view.findViewById(R.id.ib_hy);
        ib_hy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(MusicActivity.this,"hy",Toast.LENGTH_SHORT).show();
            }
        });
        ImageButton ib_pyq = (ImageButton) view.findViewById(R.id.ib_pyq);
        ib_pyq.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Toast.makeText(MusicActivity.this,"pyq",Toast.LENGTH_SHORT).show();
            }
        });
    }

//    //为请求生成一个唯一的标识
//    private String buildTransaction(final String type){
//        return (type == null)?String.valueOf(System.currentTimeMillis()):type+ System.currentTimeMillis();
//    }

    private boolean isDownloadFinish = true;
    private void showDownloadProgress(){
        isDownloadFinish = false;
        pb_download.setVisibility(View.VISIBLE);
    }
    private void closeDownloadProgress(final int index_downloaded){
        isDownloadFinish = true;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MusicActivity.this,downloadPath+songList.get(index_downloaded-1).getName()+".mp3 downloaded",Toast.LENGTH_SHORT).show();
                pb_download.setVisibility(View.GONE);
            }
        });
    }
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            DownloadMusic.DownloadBinder binder = (DownloadMusic.DownloadBinder) service;
            binder.setMyDownloadListener(new DownloadMusic.MyDownloadListener() {
                @Override
                public void closeProgress(int index_downloaded) {
                    closeDownloadProgress(index_downloaded);
                }
            });
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {}
    } ;


    @Override
    public void onItemClickRV(int position) {
        index=position+1;
        drawerLayout.closeDrawers();
        mediaPlayer.changSong(index);
        mediaPlayer.start();
        bt_play_pause.setImageResource(R.drawable.pause);
        vf_ly_bs.setDisplayedChild(1);
    }

    @Override
    protected void onDestroy() {
        Log.d("changeactivity", "music onDestroy: ");
        unbindService(conn_mp);
        unbindService(conn_en);
        Intent intent_en = new Intent(MusicActivity.this,NewSongListeningService.class);
        stopService(intent_en);
        if (isTaskRoot()){
            Intent intent = new Intent();
            intent.setAction("MUSIC_PLAYING_SERVICE");
            intent.setPackage(getPackageName());
            stopService(intent);
        }
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //startActivity(new Intent(MusicActivity.this,FirstActivity.class));
    }

    @Override
    protected void onStart() {
        Log.d("changeactivity", "music onStart: ");
        super.onStart();
    }

    @Override
    protected void onStop() {
        Log.d("changeactivity", "music onStop: ");
        isOnPause = true;
        super.onStop();
    }

    private boolean isOnPause = false;
    @Override
    protected void onPause() {
        Log.d("changeactivity", "music onPause: ");
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        Log.d("changeactivity", "music PostResume");
    }


    @Override
    protected void onRestart() {
        isOnPause = false;
        super.onRestart();
        Log.d("changeactivity", "music Restart");
    }

    private  boolean firstIn = true;
    //button 监听事件
    @Override
    public void onClick(View v) {
        if (firstIn){
            initOther();
            firstIn = false;
        }
        switch (v.getId()){
            case R.id.bt_previous:
                mediaPlayer.playPre();
                break;
            case R.id.bt_play_and_pause:
                mediaPlayer.changP_P();
                if (!mediaPlayer.isPlaying()){
                    bt_play_pause.setImageResource(R.drawable.play);
                }else {
                    bt_play_pause.setImageResource(R.drawable.pause);
                }
                break;
            case R.id.bt_next:
                mediaPlayer.playNext();
                break;
            case R.id.bt_mode:
                if (MODE==MODE_LIST_LOOP){
                    MODE = MODE_SINGLE_LOOP;
                    mediaPlayer.setLooping(true);
                    mediaPlayer.setMode(MODE);
                    bt_mode.setImageResource(R.drawable.single);
                    Toast.makeText(MusicActivity.this,"单曲循环",Toast.LENGTH_SHORT).show();
                }else if (MODE == MODE_SINGLE_LOOP){
                    MODE = MODE_RANDOM;
                    mediaPlayer.setLooping(false);
                    mediaPlayer.setMode(MODE);
                    bt_mode.setImageResource(R.drawable.random_play);
                    Toast.makeText(MusicActivity.this,"随机播放",Toast.LENGTH_SHORT).show();
                }else if (MODE == MODE_RANDOM){
                    MODE = MODE_PLAY_BY_ORDER;
                    mediaPlayer.setLooping(false);
                    mediaPlayer.setMode(MODE);
                    bt_mode.setImageResource(R.drawable.play_by_order);
                    Toast.makeText(MusicActivity.this,"顺序播放",Toast.LENGTH_SHORT).show();
                }else if(MODE == MODE_PLAY_BY_ORDER){
                    MODE = MODE_LIST_LOOP;
                    mediaPlayer.setMode(MODE);
                    mediaPlayer.setLooping(false);
                    bt_mode.setImageResource(R.drawable.allplay);
                    Toast.makeText(MusicActivity.this,"列表循环",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_download:
                boolean isn = isNetWorkAvailable();
                boolean isd = isDownloadeded(index)[0];
                if (isn && !isd ){
                    if (isDownloadFinish){
                        showDownloadProgress();
                        Intent intent = new Intent(MusicActivity.this, DownloadMusic.class);
                        intent.putExtra("id",index);
                        intent.putExtra("name",songList.get(index-1).getName());
                        startService(intent);
                        bindService(intent,conn,BIND_AUTO_CREATE);
                    }else {
                        Toast.makeText(MusicActivity.this,"is downloading, please wait",Toast.LENGTH_SHORT).show();
                    }
                }else if (isd){
                    Toast.makeText(MusicActivity.this,"already downloaded",Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MusicActivity.this,"no network",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.bt_share_to_wx:
                share_to_wx();
                break;
            case R.id.fbt_menu:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.ib_refresh_songList:
                if (hasNewSong){
                    int song_count_new = prefs.getInt("song_count",1);
                    prefs.edit().putInt("song_count_show",song_count_new).apply();
                    initRecyclerView();
                    ib_refresh_list.setImageResource(R.drawable.refresh_list);
                    Toast.makeText(MusicActivity.this,"ok",Toast.LENGTH_SHORT).show();
                    hasNewSong = false;
                }else {
                    Toast.makeText(MusicActivity.this,"no new song",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
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


}
