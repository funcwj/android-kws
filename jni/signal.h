//
// Created by wujian on 16-12-28.
//

#ifndef TEST_SIGNAL_H
#define TEST_SIGNAL_H

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <math.h>
#include <limits.h>
#include "cmvn.h"
#include "clog.h"

#define PI      3.14159265358979
#define TPI     6.28318530717959
#define MINLARG     2.45E-308
#define LZERO       (-1.0E10)



const int FRAME_WNDLEN  = 400;
const int FRAME_OFFSET  = 160;
const int FRAME_FFTLEN  = 512;
const int SPECT_WNDLEN  = 257;
const int FBANK_MAXCHS  = 40;

const float EMP_COFX    = 0.97;


void InitSignalProc();
float* WaveToFBank(float *wave);


#endif //TEST_SIGNAL_H
