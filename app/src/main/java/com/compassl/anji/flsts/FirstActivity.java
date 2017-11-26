package com.compassl.anji.flsts;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.compassl.anji.flsts.service.UpdateBackgroundPic;
import com.compassl.anji.songs_ssw.R;

import java.util.List;

public class FirstActivity extends AppCompatActivity {


    private ProgressBar pb_wait;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("changeactivity", "first onCreate: ");
        if (!isTaskRoot()){
            finish();
            return;
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        pb_wait = (ProgressBar) findViewById(R.id.pb_wait);
    }

    public void toMusic(View view){
        Intent intent = new Intent(FirstActivity.this,MusicActivity.class);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(FirstActivity.this);
        builder.setTitle("退出");
        builder.setMessage("确认要退出应用吗？");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                FirstActivity.super.onBackPressed();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    protected void onStart() {
        Log.d("changeactivity", "first onStart: ");
        super.onStart();
    }

    @Override
    protected void onPostResume() {
        Log.d("changeactivity", " first onPostResume: ");
        super.onPostResume();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("changeactivity", " first onRestart: ");
    }

    @Override
    protected void onPause() {
        //pb_wait.setVisibility(View.VISIBLE);
        super.onPause();
        Log.d("changeactivity", "first onpause");
    }

    @Override
    protected void onStop() {
        pb_wait.setVisibility(View.GONE);
        super.onStop();
        Log.d("changeactivity", "first onstop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTaskRoot()){
            Intent intent = new Intent();
            intent.setAction("MUSIC_PLAYING_SERVICE");
            intent.setPackage(getPackageName());
            stopService(intent);
        }
        Log.d("changeactivity", "first destroy");
    }
}
