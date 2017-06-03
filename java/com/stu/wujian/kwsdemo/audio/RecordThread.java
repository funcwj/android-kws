package com.stu.wujian.kwsdemo.audio;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wujian on 12/12/2016.
 */

public class RecordThread extends Thread {

    private boolean running;
    private int minBufferSize;
    private AudioRecord record;
    private Context context;
    private DataOutputStream pcm;

    public RecordThread() {
        running = true;
        minBufferSize = AudioRecord.getMinBufferSize(16000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT);
        record = new AudioRecord(MediaRecorder.AudioSource.MIC, 16000,
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
    }

    public void setStop() {
        running = !running;
    }

    public boolean openDstFile(String dst) {
        try {
            pcm = new DataOutputStream(context.openFileOutput("wave.pcm", Context.MODE_PRIVATE));
        } catch (FileNotFoundException e) {
            Log.i("KWS", "RecordThread: FileNotFoundException in openDstFile");
            return false;
        }
        return true;
    }
    @Override
    public void run() {

        short buffer[] = new short[minBufferSize];
        record.startRecording();
        try {
            while (running) {
                int readSize = record.read(buffer, 0, minBufferSize);
                for (int i = 0; i < readSize; i++)
                    pcm.writeShort(buffer[i]);
            }
            record.stop();
            pcm.close();
        } catch (IOException e) {
            Log.i("KWS", "Run: IOException when Recording");
        }
    }
}
