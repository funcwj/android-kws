package com.stu.wujian.kwsdemo.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;


import com.stu.wujian.kwsdemo.utils.KWSKERNEL;
import com.stu.wujian.kwsdemo.utils.PCMWriter;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by wujian on 17-1-3.
 */

public class ExtractThread extends Thread {

    private AudioRecord audioRecord;
    private ArrayBlockingQueue<float[]> fbankQue;

    private Handler uiHandle;
    // 1280
    private int minBufferSize;
    private boolean running;

    private final int LOG_MSG = 2;

    private final int FREQ = 16000;
    private final int CONF = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private final int CODE = AudioFormat.ENCODING_PCM_16BIT;

    private final int FRAME_LEN = 400;
    private final int FRAME_OFF = 160;
    private final int FBANK_LEN = 40;

    public ExtractThread(Handler handler, ArrayBlockingQueue queue) {
        uiHandle = handler;
        fbankQue = queue;
        running  = true;
        minBufferSize = AudioRecord.getMinBufferSize(FREQ, CONF, CODE);
        Log.i("KWS", "ExtractThread: AudioRecord.getMinBufferSize = " + minBufferSize);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, FREQ, CONF, CODE, minBufferSize);
    }

    public void setStop() {
        running = false;
    }

    @Override
    public void run() {

        short[] readBuffer = new short[minBufferSize];
        short[] tmp = new short[FRAME_LEN];

        KWSKERNEL kWS = new KWSKERNEL();
        PCMWriter writer = new PCMWriter();

        Log.i("KWS", "ExtractThread start");
        int sampleRemain = 0;

        long frame_cnt = 0;

        audioRecord.startRecording();

        while(running) {
            // fix: 实际读取到的数据长度,不是总和buffer大小一致的
            int hasread = audioRecord.read(readBuffer, 0, readBuffer.length);
            // cost 50ms+
            int nframes = (hasread + sampleRemain - FRAME_LEN) / FRAME_OFF + 1;

            frame_cnt += nframes;

            short[] wavBuffer = new short[hasread + sampleRemain];
            System.arraycopy(tmp, 0, wavBuffer, 0, sampleRemain);
            System.arraycopy(readBuffer, 0, wavBuffer, sampleRemain, hasread);


            float[] fbankBuffer = kWS.waveToFBank(wavBuffer);
            for (int i = 0; i < fbankBuffer.length / FBANK_LEN; i++) {
                float[] fbank = new float[FBANK_LEN];
                System.arraycopy(fbankBuffer, i * FBANK_LEN, fbank, 0, fbank.length);
                try {
                    fbankQue.put(fbank);
                } catch (InterruptedException e) {
                    Log.i("KWS", "ExtractThread: InterruptedException exist in running thread");
                    uiHandle.obtainMessage(LOG_MSG, "ExtractThread: InterruptedException exist").sendToTarget();
                    break;
                }
            }
            sampleRemain = hasread + sampleRemain - nframes * FRAME_OFF;
            System.arraycopy(wavBuffer, nframes * FRAME_OFF, tmp, 0, sampleRemain);
        }
        audioRecord.stop();
        try {
            fbankQue.put(new float[1]);
        } catch (InterruptedException e) {
            uiHandle.obtainMessage(LOG_MSG, "ExtractThread: InterruptedException exist when exit").sendToTarget();
        }
        Log.i("KWS", "Record frames = " + frame_cnt);
    }
}
