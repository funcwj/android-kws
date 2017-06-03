package com.stu.wujian.kwsdemo.audio;

import android.os.Handler;
import android.util.Log;


import com.stu.wujian.kwsdemo.ConfidenceBuilder;
import com.stu.wujian.kwsdemo.utils.KWSKERNEL;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by wujian on 17-1-8.
 */

public class PredictThread extends Thread {

    private ArrayBlockingQueue<float[]> fbankQue;
    private ConfidenceBuilder confidenceBuilder;
    private Handler uiHandle;

    private final int LEFT_CTX = 30;
    private final int RIGHT_CTX = 10;
    private final int FBANK_LEN = 40;

    private final int CHART_UPDATE = 1;
    private final int LOG_MSG = 2;


    public PredictThread(Handler handler, ArrayBlockingQueue<float[]> queue) {
        uiHandle = handler;
        fbankQue = queue;
        confidenceBuilder = new ConfidenceBuilder(30, 100, 2);
    }

    public int argMax(float[] in) {
        float max = in[0];
        int idx = 0;
        for (int i = 1; i < in.length; i++) {
            if (max < in[i]) {
                max = in[i];
                idx = i;
            }
        }
        return idx;
    }

    @Override
    public void run() {

        float[] buffer = new float[(LEFT_CTX + RIGHT_CTX + 1) * FBANK_LEN];
        float[] spect = null;
        float[] finalSPect = null;
        Log.i("KWS", "PredictThread start");

        KWSKERNEL KWS = new KWSKERNEL();

        try {
            spect = fbankQue.take();
            // 0 0 0 0 1 2
            // 0 0 0 1 2 3
            // 0 0 1 2 3 3
            for(int i = 0; i < LEFT_CTX + RIGHT_CTX + 1; i++) {
                if(i > LEFT_CTX)
                    spect = fbankQue.take();
                System.arraycopy(spect, 0, buffer, i * FBANK_LEN, spect.length);
            }
        } catch (InterruptedException e) {
            Log.i("KWS", "PredictThread: Catch InterruptedException for the first frame");
            uiHandle.obtainMessage(LOG_MSG, "PredictThread: InterruptedException exist").sendToTarget();
            return;
        }

        while (true) {
            try {
                // predict for buffer
                float[] expect = KWS.predictClass(buffer);
                float[] evalue = new float[expect.length + 1];

                System.arraycopy(expect, 0, evalue, 0, expect.length);
                evalue[expect.length] = confidenceBuilder.getConfidence(expect);

                uiHandle.obtainMessage(CHART_UPDATE, evalue).sendToTarget();

                // uiHandle.obtainMessage(LOG_MSG, fbankQue.size() + " waiting for process").sendToTarget();

                spect = fbankQue.take();
                if (spect.length == 1)
                    break;
                System.arraycopy(buffer, spect.length, buffer, 0, buffer.length - spect.length);
                System.arraycopy(spect, 0, buffer, buffer.length - FBANK_LEN, spect.length);
                finalSPect = spect;

            } catch (InterruptedException e) {
                Log.i("KWS", "PredictThread: Catch InterruptedException for the first frame");
                uiHandle.obtainMessage(LOG_MSG, "PredictThread: InterruptedException exist").sendToTarget();
                break;
            }
        }
        // buffer
        for(int i = 0; i < RIGHT_CTX; i++) {
            System.arraycopy(buffer, finalSPect.length, buffer, 0, buffer.length - finalSPect.length);
            float[] expect = KWS.predictClass(buffer);
            float[] evalue = new float[expect.length + 1];
            System.arraycopy(expect, 0, evalue, 0, expect.length);
            evalue[expect.length] = confidenceBuilder.getConfidence(expect);
            uiHandle.obtainMessage(CHART_UPDATE, evalue).sendToTarget();
        }
        uiHandle.obtainMessage(LOG_MSG, "Thread stop OK").sendToTarget();
    }
}
