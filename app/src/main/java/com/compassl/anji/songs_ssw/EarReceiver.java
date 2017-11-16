package com.compassl.anji.songs_ssw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class EarReceiver extends BroadcastReceiver {

    private EarListener mEarListener;

    public EarReceiver() {
    }



    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("ear", "onReceive: "+1);


    }

    public interface EarListener{
        void onReceiveEar();
    }

    public void setmEarListener(EarListener listener){
        this.mEarListener = listener;
    }

}
