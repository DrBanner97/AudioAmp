package com.example.audioamp;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Random;

public class WaveformView extends View {

    private Paint mGridPaint,negPaint;
    private int[] mHeightsAtThisZoomLevel;
    private int[] mLenByZoomLevel;
    private int mZoomLevel;

    private double[][] mValuesByZoomLevel;

    private double[] mZoomFactorByZoomLevel;

    private Canvas currentCanvas;
    int measuredWidth,measuredHeight,halfheight;
    int currentX,strokeWidth;
    float heightToDraw;


    public void setHeightToDraw(float val){
        heightToDraw = val;
    }



    public interface WaveformListener{

        public void getCanvasObject(Canvas canvas);

    }

    private WaveformListener mListener;



    public void setListener(WaveformListener listener) {
        mListener = listener;
    }


    public WaveformView(Context context) {
        super(context);

    }

    public WaveformView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        strokeWidth=10;

        mGridPaint = new Paint();
        mGridPaint.setAntiAlias(false);
        mGridPaint.setStrokeWidth(strokeWidth);
        mGridPaint.setColor(res.getColor(R.color.grid_line));


        negPaint = new Paint();
        negPaint.setAntiAlias(false);
        negPaint.setStrokeWidth(strokeWidth);
        negPaint.setColor(res.getColor(R.color.colorAccent));
         measuredWidth = getMeasuredWidth();
         measuredHeight = getMeasuredHeight();
         halfheight = measuredHeight/2;
         currentX=20;

    }



    @Override
    protected void onDraw(Canvas canvas) {
        currentCanvas = canvas;

//        if (mListener != null) {
//            mListener.getCanvasObject(canvas);
//        }

            Log.d("Waveform","onDraw "+canvas);
            currentX = getMeasuredWidth()/2;

            canvas.drawLine(currentX,getMeasuredHeight()/2,currentX,(getMeasuredHeight()/2)-heightToDraw,mGridPaint);
            canvas.drawLine(currentX,getMeasuredHeight()/2,currentX,(getMeasuredHeight()/2)+heightToDraw,negPaint);
            currentX += strokeWidth;


//        startWaveform();

        super.onDraw(canvas);

    }


    public void startWaveform(){
        Random rand = new Random();


        for(int i=0;i<250;i++){
            int val = rand.nextInt(500);
            currentCanvas.drawLine(currentX,getMeasuredHeight()/2,currentX,(getMeasuredHeight()/2)-heightToDraw,mGridPaint);
            currentCanvas.drawLine(currentX,getMeasuredHeight()/2,currentX,(getMeasuredHeight()/2)+heightToDraw,negPaint);
            currentX += strokeWidth;


        }

    }


    @Override
    public void computeScroll() {
        super.computeScroll();

    }

    public void plot(){

        draw(currentCanvas);
    }



}
