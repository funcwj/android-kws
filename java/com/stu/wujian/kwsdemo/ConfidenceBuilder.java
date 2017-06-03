package com.stu.wujian.kwsdemo;

/**
 * Created by wujian on 17-3-7.
 */

public class ConfidenceBuilder {

    private WindBuffer[] smoothBuffer;
    private WindBuffer[] confidBufder;
    private int nclass;

    public ConfidenceBuilder(int sTimeLength, int cTimeLength, int numOfClass) {
        smoothBuffer = new WindBuffer[numOfClass];
        confidBufder = new WindBuffer[numOfClass];
        nclass = numOfClass;
        for (int i = 0; i < numOfClass; i++) {
            smoothBuffer[i] = new WindBuffer(sTimeLength);
            confidBufder[i] = new WindBuffer(cTimeLength);
        }
    }

    public float getConfidence(float[] prob) {
        for (int i = 0; i < nclass; i++) {
            smoothBuffer[i].pushItem(prob[i]);
            confidBufder[i].pushItem(smoothBuffer[i].getMeanOfItems());
        }
        float confidence = 1.0f;
        for (int i = 0; i < nclass; i++)
            confidence = confidence * confidBufder[i].getMaxOfItem();
        // default nclass = 2
        return (float) Math.sqrt(confidence);
    }
}
