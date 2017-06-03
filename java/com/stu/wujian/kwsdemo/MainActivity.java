package com.stu.wujian.kwsdemo;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;
import com.stu.wujian.kwsdemo.audio.ExtractThread;
import com.stu.wujian.kwsdemo.audio.PredictThread;
import com.stu.wujian.kwsdemo.utils.DataGenerator;
import com.stu.wujian.kwsdemo.utils.KWSKERNEL;
import com.stu.wujian.kwsdemo.utils.TransUtil;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

public class MainActivity extends AppCompatActivity {

    private final int CHART_UPDATE = 1;
    private final int LOG_MSG = 2;

    private final int CHAT_SAMPLE = 500;
    private final int UPDATE_ROUND = 10;

    private int round = 0;

    private ExtractThread extractThread;
    private PredictThread predictThread;
    private DataGenerator dataGenerator;

    private ArrayBlockingQueue<float[]> fbankQue = new ArrayBlockingQueue<float[]>(100);

    private boolean running = false;
    private LineChart chart;
    private Button btn;
    private TextView panel;
    private NumberPicker numberPicker;
    private TransUtil transTool;
    private long[] vibratePattern = {100, 400, 100, 400};

    private Handler dataHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            final ArrayList<String> x = new ArrayList<>();
            for (int i = 0; i < CHAT_SAMPLE; i++)
                x.add(".");

            switch (msg.what) {
                case CHART_UPDATE:
                    float[] updateDate = (float[]) msg.obj;
                    if (transTool.spot(updateDate[updateDate.length - 1])) {
                        Toast.makeText(getApplicationContext(), "spot success!", Toast.LENGTH_SHORT).show();
                        ((Vibrator)getSystemService(Context.VIBRATOR_SERVICE)).vibrate(vibratePattern, -1);
                    }
                    if (round == UPDATE_ROUND) {
                        dataGenerator.updateData(updateDate);
                        chart.setData(new LineData(x, dataGenerator.getCurrentShot()));
                        chart.notifyDataSetChanged();
                        chart.invalidate();
                        round = 0;
                    } else {
                        dataGenerator.updateData((float[]) msg.obj);
                        round++;
                    }
                    break;
                case LOG_MSG:
                    panel.setText((String)msg.obj);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button)findViewById(R.id.RECORD);
        chart = (LineChart)findViewById(R.id.SCORE);
        panel = (TextView)findViewById(R.id.PANEL);
        numberPicker = (NumberPicker)findViewById(R.id.numberPicker);

        numberPicker.setMaxValue(10);
        numberPicker.setMinValue(1);
        numberPicker.setValue(7);

        transTool = new TransUtil(0.5f, 10);

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                float newThres = (float)newVal / 10;
                transTool.setThreshold(newThres);
                panel.setText("confidence threshold set to " + newThres);
            }
        });

        dataGenerator = new DataGenerator(CHAT_SAMPLE);
        round = 0;

        chart.setDescription("Real Time Confidence Log");
        chart.setNoDataTextDescription("Real Time Confidence Log");
        chart.setDrawGridBackground(false);
        chart.getXAxis().setEnabled(false);
        chart.getAxisRight().setEnabled(false);
        chart.getAxisRight().setAxisMaxValue(1);

        new InitSystem(panel, getAssets()).execute();

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!running) {
                    btn.setText("stop");
                    extractThread = new ExtractThread(dataHandle, fbankQue);
                    predictThread = new PredictThread(dataHandle, fbankQue);
                    extractThread.start();
                    predictThread.start();
                    running = true;
                } else {
                    extractThread.setStop();
                    btn.setText("start");
                    running = false;
                }
            }
        });
    }

}
