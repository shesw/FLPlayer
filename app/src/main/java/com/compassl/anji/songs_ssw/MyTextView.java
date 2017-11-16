package com.compassl.anji.songs_ssw;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Printer;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/11/2.
 */
public class MyTextView extends TextView {

    private onTouchListenerM mOntouchListener;
    private float downX;
    private float upX;
    private int downY;
    private int totalDeltY;
    private final int DIVIDE = 20;

    public interface onTouchListenerM{
        void onTouch(String direction);
        void onTouch(int Y);
        void resetY();
        void setToBottom(int Y);
    }

    public void setOntouchListenerM(onTouchListenerM listener){
        this.mOntouchListener = listener;
    }


    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mOntouchListener!=null){
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                downX = event.getX();
                downY = (int) event.getY();
                return true;
            }else if (event.getAction() == MotionEvent.ACTION_UP){
                upX = event.getX();
                if (upX - downX > 200) {
                    //显示上一屏的动画
                    mOntouchListener.onTouch("previous");
                    return true;
                } else if (downX - upX > 200) {
                    //显示下一屏的动画
                    mOntouchListener.onTouch("next");
                    return true;
                }
            }else if (event.getAction() == MotionEvent.ACTION_MOVE){
                int deltY = downY - (int)event.getY();
                mOntouchListener.onTouch(deltY/DIVIDE);
                totalDeltY +=deltY/DIVIDE;
                return true;
            }
            if (totalDeltY<0){
                totalDeltY = 0;
                mOntouchListener.resetY();
            }else if (totalDeltY> (getLineBounds(getLineCount()-1,null)-getHeight()) ){
                totalDeltY = ( getLineBounds(getLineCount()-1,null)-getHeight() )  > 0 ?
                        getLineBounds(getLineCount()-1,null)-getHeight(): 0;
                mOntouchListener.setToBottom(totalDeltY);
            }
        }
        return super.onTouchEvent(event);
    }
}