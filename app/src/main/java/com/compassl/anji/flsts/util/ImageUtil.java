package com.compassl.anji.flsts.util;

import android.graphics.Bitmap;

import java.io.ByteArrayOutputStream;

/**
 * Created by Administrator on 2017/11/27.
 */
public class ImageUtil {
    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, output);

        int option = 100;
        while (output.toByteArray().length/1024>32 && option>0){
            output.reset();
            option-=10;
            bmp.compress(Bitmap.CompressFormat.JPEG,option,output);
        }
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


}