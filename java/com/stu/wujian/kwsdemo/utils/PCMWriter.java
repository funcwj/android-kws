package com.stu.wujian.kwsdemo.utils;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by wujian on 17-2-22.
 */

public class PCMWriter {

    private DataOutputStream ds;

    public PCMWriter() {}

    public boolean openDstFile(String filename) {

        try {
            ds = new DataOutputStream(new FileOutputStream(filename));
        } catch (FileNotFoundException e) {
            return false;
        }
        return true;
    }

    public boolean closeDstFile() {
        try {
            ds.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean writePCMData(short[] data, int count) {
        try {
            for (int i = 0; i < count; i++) {
                ds.writeShort(data[i]);
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
