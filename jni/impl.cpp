//
// Created by wujian on 17-3-6.
//
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include "nnet.h"
#include "signal.h"
#include "com_stu_wujian_kwsdemo_utils_KWSKERNEL.h"


nnet net = NULL;
vector input = NULL;

JNIEXPORT jboolean JNICALL Java_com_stu_wujian_kwsdemo_utils_KWSKERNEL_initSystem
        (JNIEnv *env, jobject obj, jobject assets) {
    LOGI("JNI: InitSystem");
    AAssetManager *manager = AAssetManager_fromJava(env, assets);
    AAsset *nnet_cfg = AAssetManager_open(manager, "cmvn-10-nadam.bin", AASSET_MODE_UNKNOWN);
    if (nnet_cfg == NULL) {
        KWS_ERROR("JNI: initSystem: AAssetManager_open: failed");
        return false;
    }

    if ((net = create_nnet(nnet_cfg)) == NULL) {
        KWS_ERROR("JNI: initSystem: create_nnet failed");
        return false;
    }
    InitSignalProc();
    input = new Vector();
    return true;
}

JNIEXPORT jfloatArray JNICALL Java_com_stu_wujian_kwsdemo_utils_KWSKERNEL_waveToFBank
        (JNIEnv *env, jobject obj, jshortArray data) {
    int nsamples = env->GetArrayLength(data);
    short *dat = env->GetShortArrayElements(data, 0);
    float *wave = new float[nsamples + 1];
    wave[0] = nsamples;
    for (int i = 1; i <= nsamples; i++)
        wave[i] = dat[i - 1];
    env->ReleaseShortArrayElements(data, dat, 0);
    float *fbank = WaveToFBank(wave);
    int fbank_len = int(fbank[0]);
    jfloatArray ret = env->NewFloatArray(fbank_len);
    env->SetFloatArrayRegion(ret, 0, fbank_len, fbank + 1);
    return ret;
}

JNIEXPORT jfloatArray JNICALL Java_com_stu_wujian_kwsdemo_utils_KWSKERNEL_predictClass
        (JNIEnv *env, jobject obj, jfloatArray array) {
    int input_len = env->GetArrayLength(array);
    float *data = env->GetFloatArrayElements(array, 0);
    if (input == NULL) {
        KWS_ERROR("JNI: vector input not initialized");
        input = new Vector();
    }
    input->size = input_len;
    input->data = data;
    vector pred = expect(net, input);
    env->ReleaseFloatArrayElements(array, data, 0);
    jfloatArray ret = env->NewFloatArray(pred->size);
    env->SetFloatArrayRegion(ret, 0, pred->size, pred->data);
    return ret;
}

JNIEXPORT jboolean JNICALL Java_com_stu_wujian_kwsdemo_utils_KWSKERNEL_freeSystem
        (JNIEnv *env, jobject obj) {
    depose_nnet(net);
    depose_vector(input);
    input = NULL;
    net = NULL;
    LOGI("JNI: FreeSystem");
}