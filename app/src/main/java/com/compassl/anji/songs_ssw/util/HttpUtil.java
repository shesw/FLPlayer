package com.compassl.anji.songs_ssw.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by Administrator on 2017/11/1.
 */
public class HttpUtil {
    public static void sendOkHttpRequest(String adrress, okhttp3.Callback callback){
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(adrress).build();
        client.newCall(request).enqueue(callback);
    }

}