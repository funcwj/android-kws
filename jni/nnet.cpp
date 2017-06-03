//
// Created by wujian on 17-3-5.
//

#include "nnet.h"


vector create_vector(int size) {
    vector vec = (vector)malloc(sizeof(Vector));
    vec->size = size;
    vec->data = (float*)malloc(sizeof(float) * size);
    return vec;
}

void depose_vector(vector vec) {
    if (vec != NULL && vec->data != NULL) {
        free(vec->data);
    } else if (vec != NULL) {
        free(vec);
    }
}

//void show_vector(vector vec) {
//    // printf("vector: [%d]\n", vec->size);
//    for (int i = 0; i < vec->size; i++)
//        i == vec->size - 1 ? printf("%10f\n", vec->data[i]): printf("%10f", vec->data[i]);
//}
//
//void show_matrix(matrix mat) {
//    printf("matrix [%d] X [%d]\n", mat->nrow, mat->ncol);
//    for (int i = 0; i < mat->nrow; i++) {
//        for (int j = 0; j < mat->ncol; j++)
//            j == mat->ncol - 1 ? printf("%10f\n", mat->data[i * mat->ncol + j]): printf("%10f", mat->data[i * mat->ncol + j]);
//    }
//}


matrix create_matrix(int nrow, int ncol) {
    matrix mat = (matrix)malloc(sizeof(Matrix));
    mat->ncol = ncol, mat->nrow = nrow;
    mat->data = (float*)malloc(sizeof(float) * ncol * nrow);
    return mat;
}

void depose_matrix(matrix mat) {
    if (mat != NULL && mat->data != NULL) {
        free(mat->data);
    } else if (mat != NULL) {
        free(mat);
    }
}

bool read(AAsset *src, int *nrow, int *ncol) {
    if (AAsset_read(src, nrow, sizeof(int)) != sizeof(int))
        return false;
    if (AAsset_read(src, ncol, sizeof(int)) != sizeof(int))
        return false;
    return true;
}

void depose_nnet(nnet net) {

    if(net == NULL)
        return;

    for (int i = 0; i < net->depth; i++) {
        depose_vector(net->x[i]);
        depose_vector(net->bias[i]);
        depose_matrix(net->weight[i]);
    }
    free(net);
}


//void show_nnet(nnet net) {
//    for (int i = 0; i < net->depth; i++) {
//        show_matrix(net->weight[i]);
//        show_vector(net->bias[i]);
//    }
//}

nnet create_nnet(AAsset *src) {
    int nrow, ncol, dp = 0;
    nnet net = (nnet)malloc(sizeof(NNet));
    while (read(src, &nrow, &ncol)) {
        LOGI("Depth %d:  %d X %d", dp, nrow, ncol);
        net->bias[dp] = create_vector(ncol);
        net->x[dp] = create_vector(ncol);
        net->weight[dp] = create_matrix(nrow, ncol);
        if (AAsset_read(src, net->weight[dp]->data, sizeof(float) * ncol * nrow) != ncol * nrow * sizeof(float) ||
                AAsset_read(src, net->bias[dp]->data, sizeof(float) * ncol) != ncol * sizeof(float)) {
            KWS_ERROR("Not read complete data");
            depose_nnet(net);
            return NULL;
        }
        dp++;
    }

    net->depth = dp;
    return net;
}


void relu(vector vec) {
    for (int i = 0; i < vec->size; i++) {
        if (vec->data[i] < 0.0)
            vec->data[i] = 0.0;
    }
}


void softmax(vector vec) {
    float *base = vec->data;
    float max = base[0], sum = 0;

    for (int i = 1; i < vec->size; i++) {
        if (max < base[i])
            max = base[i];
    }
    for (int i = 0; i < vec->size; i++) {
        base[i] = exp(base[i] - max);
        sum += base[i];
    }
    for (int i = 0; i < vec->size; i++)
        base[i] /= sum;
}

void forword(vector x, matrix w, vector y, vector b) {

    if (y->size != b->size) {
        KWS_ERROR("Output vector don't much bias size");
        return;
    }
    if (x->size != w->nrow || y->size != w->ncol) {
        KWS_ERROR("Input vector don't much weight size");
        return;
    }
    for (int i = 0; i < b->size; i++)
        y->data[i] = b->data[i];

    for (int i = 0; i < x->size; i++) {
        for (int j = 0; j < y->size; j++)
            y->data[j] += x->data[i] * w->data[i * w->ncol + j];
    }
}

vector expect(nnet net, vector fbank) {
    for (int i = 0; i < net->depth; i++) {
        if (i == 0)
            forword(fbank, net->weight[i], net->x[i], net->bias[i]);
        else
            forword(net->x[i - 1], net->weight[i], net->x[i], net->bias[i]);

        if (i == net->depth - 1)
            softmax(net->x[i]);
        else
            relu(net->x[i]);
    }
    return net->x[net->depth - 1];
}