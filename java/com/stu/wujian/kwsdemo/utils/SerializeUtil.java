package com.stu.wujian.kwsdemo.utils;


import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Created by wujian on 17-1-8.
 */

public class SerializeUtil {

    private PrintStream writer;

    public SerializeUtil() {
    }

    public boolean openDstFile(String dst) {

        try {
            writer = new PrintStream(new FileOutputStream(dst));
        } catch (FileNotFoundException e) {
            Log.i("RTDecode", "SerializeUtil: FileNotFoundException exist when openDstFile");
            return false;
        }
        return true;
    }

    public void serializeVector(float[] vec) {
        for(int i = 0; i < vec.length; i++) {
            writer.printf("%10f ", vec[i]);
            if (i == vec.length - 1)
                writer.println();
        }
    }

    public void serializeShortVector(short[] vec) {
        for(int i = 0; i < vec.length; i++) {
            writer.print(vec[i] + " ");
            if (i == vec.length - 1)
                writer.println();
        }
    }

    public void closeDesFile() {
        writer.close();
    }
}
