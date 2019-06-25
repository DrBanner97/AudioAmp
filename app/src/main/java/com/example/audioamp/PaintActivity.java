package com.example.audioamp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class PaintActivity extends AppCompatActivity implements WaveformView.WaveformListener {

    WaveformView wave;
    private AudioRecord audio;
    private int bufferSize;

    private double lastLevel = 0;
    private Thread thread;
    private static final int sampleRate = 8000;
    private static final int SAMPLE_DELAY = 75;
    int count=0;


    int measuredWidth,measuredHeight,halfheight;
    int currentX,strokeWidth;

    private Paint mGridPaint,negPaint;


    Canvas currentCanvas;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paint);

        wave = findViewById(R.id.waveform);
        wave.setListener(this);

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

        halfheight = measuredHeight/2;
        currentX=20;



        int sampleRate = 8000;
        try {
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

        } catch (Exception e) {
        }


        startLogging();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case 123:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLogging();
                } else {
                    Toast.makeText(PaintActivity.this,"Need audio permission to operate",Toast.LENGTH_LONG).show();
                }
                return;
        }
    }

    public void startLogging(){
//        currentCanvas = new Canvas();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                    123);
        }

        else {
            audio = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT, bufferSize);


            audio.startRecording();
            thread = new Thread(new Runnable() {
                public void run() {
                    while (thread != null && !thread.isInterrupted()) {
                        try {
                            Thread.sleep(SAMPLE_DELAY);
                        } catch (InterruptedException ie) {
                            ie.printStackTrace();
                        }
                        readAudioBuffer();

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
//                                if(count<=80) {
                                    plot((float) lastLevel);
                                    count++;
//                                }
                            }
                        });
                    }
                }
            });
            thread.start();
        }
    }

    private void readAudioBuffer() {

        try {
            short[] buffer = new short[bufferSize];

            int bufferReadResult = 1;

            if (audio != null) {

                bufferReadResult = audio.read(buffer, 0, bufferSize);

                double sumLevel = 0;
                for (int i = 0; i < bufferReadResult; i++) {
                    sumLevel += buffer[i];
//                    addEntry((double)buffer[i], System.currentTimeMillis());

                }
                lastLevel = Math.abs((sumLevel / bufferReadResult));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void getCanvasObject(Canvas canvas) {
        currentCanvas = canvas;
//        startLogging();
//        wave.draw(canvas);

    }

    public void plot(Float val){

        wave.setHeightToDraw(val);
//        wave.draw(currentCanvas);
        wave.plot();
        wave.invalidate();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void closeAll(){
        thread.interrupt();
        thread = null;
        try {
            if (audio != null) {
                audio.stop();
                audio.release();
                audio = null;
            }
        } catch (Exception e) {e.printStackTrace();}

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.wave) {
            closeAll();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
