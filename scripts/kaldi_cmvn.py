#!/usr/bin/env python
# coding=utf-8

import numpy as np



def get_cmvn(cmvn_path):
    L = []
    with open("cmvn.ark", "r") as dat:
        for line in dat.readlines():
            L.extend(float(string) for string in line.strip().split())

    T = len(L) / 2
    num = np.array(L).reshape((2, T))
    N = num[0][T - 1]
    cmvn = num[0:T - 1, 0:T - 1]
    cmvn[0] = cmvn[0] / N
    cmvn[1] = cmvn[1] / N - cmvn[0] * cmvn[0]
    return cmvn

def apply_cmvn(cmvn, feats):
    avg = cmvn[0]
    var = cmvn[1]
    assert(cmvn.shape[1] == feats.shape[1])
    for idx in range(feats.shape[0]):
        feats[idx] = (feats[idx] - avg) 
        # / np.sqrt(var)
    return feats