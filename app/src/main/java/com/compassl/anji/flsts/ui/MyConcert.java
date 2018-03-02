package com.compassl.anji.flsts.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.compassl.anji.flsts.R;

/**
 * Created by Administrator on 2018/2/27.
 */
public class MyConcert extends View {

    // 播放状态时唱针的旋转角度
    private static final int PLAY_DEGREE = -15;
    // 暂停状态时唱针的旋转角度
    private static final int PAUSE_DEGREE = -45;
    private float pictureRadius;
    private int needleDegreeCounter;
    private float halfMeasureWidth;
    private Paint needlePaint;
    private float longArmLength=70;
    private float shortArmLength=30;
    private float longHeadLength=30;
    private float shortHeadLength=30;
    private float bigCircleRadius=30;
    private float smallCircleRadius=20;
    private int ringWidth=10;
    private Paint discPaint;
    private Rect dstRect;
    private Rect srcRect;
    private Path clipPath;
    private Bitmap bitmap;
    private float diskDegreeCounter;
    private float diskRotateSpeed;
    private boolean isPlaying;

    public MyConcert(Context context) {
        super(context);
        init();
    }
    public MyConcert(Context context, AttributeSet attrs) {
        super(context, attrs);
        init_attrs(context,attrs);
        init();
    }

    public MyConcert(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init_attrs(context,attrs);
        init();
    }

    private void init_attrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MyConcert);
        pictureRadius = ta.getDimension(R.styleable.MyConcert_picture_radius,0);
        bitmap = ((BitmapDrawable)ta.getDrawable(R.styleable.MyConcert_src)).getBitmap();
        diskRotateSpeed = ta.getFloat(R.styleable.MyConcert_disk_rotate_speed,0);
        ta.recycle();
    }

    private void init(){
        clipPath = new Path();
        clipPath.addCircle(0,0,pictureRadius, Path.Direction.CW);
        discPaint = new Paint();
        discPaint.setStyle(Paint.Style.STROKE);
        discPaint.setStrokeWidth(60);
        needlePaint = new Paint();
        srcRect = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        dstRect = new Rect(-(int)pictureRadius,-(int)pictureRadius,(int)pictureRadius,(int)pictureRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        halfMeasureWidth = getMeasuredWidth()>>1;
        // 绘制唱片
        drawDisk(canvas);
        // 绘制唱针
        drawNeedle(canvas);
        // 根据唱针当前角度判断是否继续重绘
        if(needleDegreeCounter > PAUSE_DEGREE){
            invalidate();
        }
    }

    /**
     * 绘制旋转了指定角度的唱针。
     * 说明一下旋转了指定角度什么意思，看上面的流程图可以知道，
     * 长的那段手臂和垂直方向是成角15°的，实际上这个角度不是一成不变的，
     * 通过控制这个角度变化，可以达到唱针处于播放/暂停状态或者在两个状态之间摆动的效果。
     */
    private void drawNeedle(Canvas canvas, int degree){
        // 移动坐标到水平中点
        canvas.save();
        canvas.translate(halfMeasureWidth, 0);
        // 准备绘制唱针手臂
        needlePaint.setStrokeWidth(10);
        needlePaint.setColor(Color.parseColor("#FF6600"));
        // 绘制第一段臂
        canvas.rotate(degree);
        canvas.drawLine(0, 0, 0, longArmLength, needlePaint);
        // 绘制第二段臂
        canvas.translate(0, longArmLength);
        canvas.rotate(-30);
        canvas.drawLine(0, 0, 0, shortArmLength, needlePaint);
        // 绘制唱针头
        // 绘制第一段唱针头
        canvas.translate(0, shortArmLength);
        needlePaint.setStrokeWidth(20);
        canvas.drawLine(0, 0, 0, longHeadLength, needlePaint);
        // 绘制第二段唱针头
        canvas.translate(0, longHeadLength);
        needlePaint.setStrokeWidth(30);
        canvas.drawLine(0, 0, 0, shortHeadLength, needlePaint);
        canvas.restore();

        // 两个重叠的圆形，即唱针顶部的旋转点
        canvas.save();
        canvas.translate(halfMeasureWidth, 0);
        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setColor(Color.parseColor("#C0C0C0"));
        canvas.drawCircle(0, 0, bigCircleRadius, needlePaint);
        needlePaint.setColor(Color.parseColor("#8A8A8A"));
        canvas.drawCircle(0, 0, smallCircleRadius, needlePaint);
        canvas.restore();
    }

    // 绘制旋转了指定角度的唱片（类似唱针，唱片里面的图片是会旋转不同角度的）
    private void drawDisk(Canvas canvas, float degree){
        // 移动坐标系到唱针下方合适位置，然后旋转指定角度
        canvas.save();
        canvas.translate(halfMeasureWidth, pictureRadius+ringWidth+longArmLength);
        canvas.rotate(degree);
        // 绘制圆环
        canvas.drawCircle(0, 0, pictureRadius+ringWidth/2, discPaint);
        // 绘制图片
        canvas.clipPath(clipPath);
        canvas.drawBitmap(bitmap, srcRect, dstRect, discPaint);
        canvas.restore();
    }


    // 绘制唱片，该方法主要是控制旋转角度用的
    private void drawDisk(Canvas canvas){
        // 这里的diskRotateSpeed变量就是唱片每次变化角度，就是旋转速度的意思
        diskDegreeCounter = diskDegreeCounter%360+diskRotateSpeed;
        // 该方法就是前面讨论唱片绘制时说的“绘制旋转了指定角度的唱片”方法
        drawDisk(canvas, diskDegreeCounter);
    }

    // 绘制唱针，该方法主要是控制唱针旋转角度用的
    private void drawNeedle(Canvas canvas){
        // 根据播放/暂停状态控制唱针角度的加/减变化
        if(isPlaying){
            if(needleDegreeCounter < PLAY_DEGREE){
                needleDegreeCounter+=3;
            }
        } else {
            if(needleDegreeCounter > PAUSE_DEGREE){
                needleDegreeCounter-=3;
            }
        }
        // // 该方法就是前面讨论唱针绘制时说的“绘制旋转了指定角度的唱针”方法
        drawNeedle(canvas, needleDegreeCounter);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measure(widthMeasureSpec),measure(heightMeasureSpec));
    }

    private int measure(int widthMeasureSpec){
        int result = 0;
        int specMode = MeasureSpec.getMode(widthMeasureSpec);
        int specSize = MeasureSpec.getSize(widthMeasureSpec);

        if(specMode == MeasureSpec.EXACTLY){
            result = specSize;
        }else{
            result = 100;
            if(specMode == MeasureSpec.AT_MOST){
                result = Math.min(result,specSize);
            }
        }
        return  result;
    }
    public void setPlaying(boolean isPlaying){
        this.isPlaying = isPlaying;
        invalidate();
    }
    public void setSrc(Bitmap bitmap){
        this.bitmap = bitmap;
    }

}