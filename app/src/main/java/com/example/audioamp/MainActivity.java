package com.example.audioamp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class MainActivity extends AppCompatActivity {

    private AudioRecord audio;
    private int bufferSize;

    private double lastLevel = 0;
    private Thread thread;
    private static final int sampleRate = 8000;
    private static final int SAMPLE_DELAY = 75;

    private LineChart amplChart,negAmplChart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        amplChart = findViewById(R.id.ampl_chart);
        negAmplChart = findViewById(R.id.ampl_chart_2);

        amplChart.setDrawGridBackground(true);
        amplChart.getLegend().setEnabled(false);
        amplChart.setDescription(null);
        amplChart.setTouchEnabled(false);
        amplChart.setViewPortOffsets(0f, 0f, 0f, 0f);

        negAmplChart.setDrawGridBackground(true);
        negAmplChart.getLegend().setEnabled(false);
        negAmplChart.setDescription(null);
        negAmplChart.setTouchEnabled(false);
        negAmplChart.setViewPortOffsets(0f, 0f, 0f, 0f);



        LineData data = new LineData();
        data.setValueTextColor(Color.BLACK);

        amplChart.setData(data);
        negAmplChart.setData(data);

        XAxis xl = amplChart.getXAxis();
        xl.setTextColor(Color.WHITE);

        xl.setDrawGridLines(true);
        xl.setAvoidFirstLastClipping(true);
        xl.setSpaceBetweenLabels(5);
        xl.setEnabled(true);

        YAxis leftAxis = amplChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaxValue(100f);
        leftAxis.setAxisMinValue(0f);
        leftAxis.setDrawGridLines(true);

        XAxis xl2 = negAmplChart.getXAxis();
        xl2.setTextColor(Color.WHITE);

        xl2.setDrawGridLines(true);
        xl2.setAvoidFirstLastClipping(true);
        xl2.setSpaceBetweenLabels(5);
        xl2.setEnabled(true);

        YAxis leftAxis2 = negAmplChart.getAxisLeft();
        leftAxis2.setTextColor(Color.WHITE);
        leftAxis2.setAxisMaxValue(0f);
        leftAxis2.setAxisMinValue(-100f);
        leftAxis2.setDrawGridLines(true);




        YAxis rightAxis = amplChart.getAxisRight();
        rightAxis.setEnabled(false);

        YAxis rightAxis2 = negAmplChart.getAxisRight();
        rightAxis2.setEnabled(false);


        int sampleRate = 8000;
        try {
            bufferSize = AudioRecord.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

        } catch (Exception e) {
        }

        startLogging();
    }


    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null,"");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setDrawCircles(false);
        set.setDrawFilled(true);


        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setDrawCubic(true);
        set.setCubicIntensity(0.2f);
//        set.setDrawHorizontalHighlightIndicator(false);


        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
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
                    Toast.makeText(MainActivity.this,"Need audio permission to operate",Toast.LENGTH_LONG).show();
                }
                return;
        }

    }

    private void addEntry(Double lastLevel, long currentTime) {
        Log.d("Buffer","val="+lastLevel);


        LineData data = amplChart.getData();
        LineData data2 = negAmplChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            ILineDataSet set2 = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                set2 = createSet();
                data.addDataSet(set);
                data2.addDataSet(set2);

            }

            data.addXValue(currentTime+"");
            data2.addXValue(currentTime+"");
            data.addEntry(new Entry(lastLevel.floatValue(), set.getEntryCount()), 0);
            data2.addEntry(new Entry(-lastLevel.floatValue(), set2.getEntryCount()), 0);




            amplChart.notifyDataSetChanged();
            amplChart.setVisibleXRangeMaximum(120);
            amplChart.moveViewToX(data.getXValCount() - 121);

            negAmplChart.notifyDataSetChanged();

            negAmplChart.setVisibleXRangeMaximum(120);
            negAmplChart.moveViewToX(data.getXValCount() - 121);


        }
    }


    public void startLogging(){
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
                                addEntry(-lastLevel, System.currentTimeMillis());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.wave) {
            closeAll();
            startActivity(new Intent(this, PaintActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
