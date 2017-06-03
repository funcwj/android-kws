package com.stu.wujian.kwsdemo.utils;

import android.graphics.Color;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by wujian on 17-3-5.
 * 重在实现功能, 目前有点小问题
 */

public class DataGenerator {

    private float[] showData;
    private final int numOfLines = 4;
    private int base;

    private final String[] labels = new String[] {
            "hello",
            "小瓜",
            "rabbish",
            "confidence"
    };

    private final int[] colors = new int[] {
            Color.BLUE,
            Color.RED,
            Color.CYAN,
            Color.GREEN
    };

    private ArrayList<Entry>[] entry;
    private LineDataSet[] dataSets;

    ArrayList<LineDataSet> lineDataSets;

    public DataGenerator(int capacity) {
        // two class
        showData = new float[capacity * numOfLines];
        for (int i = 0; i < showData.length; i++) {
            showData[i] = i % numOfLines == 2 ? 1.0f: 0.0f;
        }

        base = 0;

        entry = new ArrayList[numOfLines];
        dataSets = new LineDataSet[numOfLines];
        lineDataSets = new ArrayList<>();

        for (int i = 0; i < numOfLines; i++) {
            entry[i] = new ArrayList<>();
            dataSets[i] = new LineDataSet(entry[i], labels[i]);
            dataSets[i].setLineWidth(1f);
            dataSets[i].setDrawCircles(false);
            dataSets[i].setDrawCubic(true);
            dataSets[i].setColor(colors[i]);
            lineDataSets.add(dataSets[i]);
        }
    }

    // 一个LineDataSet对应一条线条 有一个Entry数组和一个标记字符组成
    // 一个LineData对应若干条线条 由一个LineDataSet数组和一组x显示字符组成

    public void updateData(float[] newData) {
        int N = showData.length;
        for (int i = 0; i < numOfLines; i++)
            showData[(base + i) % N] = newData[i];
        base = (base + numOfLines) % N;
    }

    public ArrayList<LineDataSet> getCurrentShot() {

        int N = showData.length;

        for (int i = 0; i < entry.length; i++)
            entry[i].clear();

        for (int i = 0; i < N; i++) {
            int idx = i % numOfLines;
            entry[idx].add(new Entry(showData[(base + i) % N], i / numOfLines));
        }

//        for (int i = 0; i < entry.length; i++)
//            entry[i].clear();
//
//        for (int i = 0; i < N; i++) {
//            int idx = i % numOfClass;
//            entry[i].add(new Entry(showData[(base + i * numOfClass + 0) % N], i / numOfClass));
//
//        }
//        LineDataSet ds0 = new LineDataSet(en0, "score of hello");
//        LineDataSet ds1 = new LineDataSet(en1, "score of 小瓜");
//        LineDataSet ds2 = new LineDataSet(en2, "score of rabbish");
//        ds0.setLineWidth(1f);
//        ds0.setDrawCircles(false);
//        ds0.setDrawCubic(true);
//        ds0.setColor(Color.BLUE);
//
//        // dataSet.setCircleSize(1f);
//
//        // dataSet.setCubicIntensity(0.5f);
//        ds1.setLineWidth(1f);
//        // dataSet.setCircleSize(1f);
//        ds1.setDrawCircles(false);
//        ds1.setDrawCubic(true);
//        ds1.setColor(Color.RED);
//
//        ds2.setLineWidth(1f);
//        ds2.setDrawCircles(false);
//        ds2.setDrawCubic(true);
//        ds2.setColor(Color.CYAN);
//
//        ArrayList<LineDataSet> lineDataSets = new ArrayList<>();
//        lineDataSets.add(ds0);
//        lineDataSets.add(ds1);
//        lineDataSets.add(ds2);

        return lineDataSets;
    }
}
