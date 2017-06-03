package com.stu.wujian.kwsdemo.utils;

/**
 * Created by wujian on 17-4-6.
 */

public class TransUtil {

    private float THRESHOLD;
    private int KWS_DUR;
    private int cur_state;
    private int cur_durat;

    private final int SIL = 0;
    private final int LOC = 1;
    private final int SPT = 2;


    public TransUtil(float thres, int dur) {
        THRESHOLD = thres;
        KWS_DUR = dur;
        cur_state = SIL;
        cur_durat = 0;
    }

    public boolean frameActive(float thres) {
        return thres >= THRESHOLD;
    }

    public void setThreshold(float threshold) {
        THRESHOLD = threshold;
    }

    public void transition(float thres) {
        switch (cur_state) {
            case SIL:
                if (frameActive(thres) && cur_durat < KWS_DUR) {
                    cur_durat++;
                    if (cur_durat == KWS_DUR)
                        cur_state = SPT;
                } else {
                    cur_durat = 0;
                }
                break;
            case SPT:
                cur_state = LOC;
                break;
            case LOC:
                if (frameActive(thres) == false)
                    cur_state = SIL;
                break;
        }
    }
        public boolean spot(float thres) {
            transition(thres);
            return cur_state == SPT;
        }


//    public boolean spot(float thres) {
//        if (thres > THRESHOLD && locked == false) {
//            locked = true;
//            return true;
//        } else if (thres < THRESHOLD) {
//            locked = false;
//        }
//        return false;
//    }
}
