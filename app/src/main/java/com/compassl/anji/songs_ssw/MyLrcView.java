package com.compassl.anji.songs_ssw;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.AnimationUtils;

import com.compassl.anji.songs_ssw.MainActivity;

import me.wcy.lrcview.LrcView;

import static com.compassl.anji.songs_ssw.R.id.vf_ly_bs;

/**
 * Created by Administrator on 2017/11/2.
 */
public class MyLrcView extends LrcView {

    private OnTouchListenerM mOnTouchListener;
    private float touchDownX;
    private float touchUpX;

    public interface OnTouchListenerM {
        void onTouchEventM(String direction);
    }

    public MyLrcView(Context context) {
        super(context);
    }

    public MyLrcView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyLrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setmOnTouchListenerM(OnTouchListenerM listener) {
        this.mOnTouchListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOnTouchListener!=null){
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                // 取得左右滑动时手指按下的X坐标
                touchDownX = event.getX();
                return true;
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                // 取得左右滑动时手指松开的X坐标
                touchUpX = event.getX();
                // 从左往右，看前一个View
                if (touchUpX - touchDownX > 200) {
                    //显示上一屏的动画
                    mOnTouchListener.onTouchEventM("previous");
                    return true;
                } else if (touchDownX - touchUpX > 200) {
                    //显示下一屏的动画
                    mOnTouchListener.onTouchEventM("next");
                    return true;
                }
            }
        }
        return super.onTouchEvent(event);
    }
}