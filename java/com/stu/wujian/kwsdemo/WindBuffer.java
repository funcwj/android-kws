package com.stu.wujian.kwsdemo;
/**
 * Created by wujian on 16-9-10.
 */
public class WindBuffer {

    final float MINIMUN = -1;
    private int bufferSize;
    private int head, tail, cnt;
    private float[] items;
    private float maxItem, sumOfItem;

    public WindBuffer(int windsize){
        bufferSize = windsize;
        head = tail = cnt = 0;
        items = new float[bufferSize];
        maxItem = MINIMUN;
        sumOfItem = 0;
    }

    public float popItem() {
        float item = items[head];
        sumOfItem -= item;
        head = (head + 1) % bufferSize;
        cnt--;
        return item;
    }

    public void resetMaxItem() {
        maxItem = items[head];
        for(int i = 1; i < bufferSize; i++) {
            if(maxItem < items[(head + i) % bufferSize])
                maxItem = items[(head + i) % bufferSize];
        }
    }
    // 插入尾部
    public void pushItem(float item){
        if(cnt < bufferSize) {
            cnt++;
            sumOfItem += item;
            items[tail] = item;
            tail = (tail + 1) % bufferSize;
            if(item > maxItem) {
                maxItem = item;
            }
        } else {
            float tmp = popItem();
            pushItem(item);
            if(Math.abs(tmp - maxItem) < 1e-10) {
                resetMaxItem();
            }
        }
    }

    public float getMeanOfItems() {
        return (float)(sumOfItem / (float)cnt);
    }

    public float getMaxOfItem() {
        return maxItem;
    }

}
