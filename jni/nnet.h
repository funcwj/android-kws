//
// Created by wujian on 17-3-5.
//

#ifndef NNET_NNET_H
#define NNET_NNET_H

#include <stdio.h>
#include <math.h>
#include <string.h>
#include <stdlib.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include "clog.h"

struct Vector {
    float *data;
    int size;
};

struct Matrix {
    float *data;
    int nrow, ncol;
};


typedef Vector* vector;
typedef Matrix* matrix;

struct NNet {
    static const int CAP = 16;
    int depth;
    vector bias[CAP];
    vector x[CAP];
    matrix weight[CAP];
};

typedef NNet* nnet;


nnet create_nnet(AAsset *src);
void depose_nnet(nnet net);
vector expect(nnet net, vector fbank);
vector create_vector(int size);
void depose_vector(vector vec);

#endif //NNET_NNET_H
