package com.stu.wujian.kwsdemo.utils;

import android.content.res.AssetManager;

/**
 * Created by wujian on 17-3-6.
 */

public class KWSKERNEL {

    static {
        System.loadLibrary("kws");
    }

    public native boolean initSystem(AssetManager assetManager);
    public native float[] waveToFBank(short[] wav);
    public native float[] predictClass(float[] fbanl);
    public native boolean freeSystem();
}
