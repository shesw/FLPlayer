package com.compassl.anji.flsts.ui;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Administrator on 2018/2/28.
 */
public class MySwipeRefreshLayout extends SwipeRefreshLayout {
    public MySwipeRefreshLayout(Context context) {
        super(context);
    }

    public MySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX(),y = (int) ev.getY();
        if(x<=1000 && x>=100 && y<=700 && y>=100){
            return false;
        }
        return super.onInterceptTouchEvent(ev);
    }
}