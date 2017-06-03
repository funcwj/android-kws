#!/usr/bin/env python

import sys
import htkreader
import kaldi_cmvn
import numpy as np

LCTX = 30
RCTX = 10

class data_reader(object):
    def __init__(self, train_path, label_path, batch, cmvn = False):
        with open(train_path, "r") as train:
            self.train_scp = [line.strip().split() for line in train.readlines()]
        with open(label_path, "r") as label:
            self.label_dat = [line.strip().split() for line in label.readlines()]
        
        self.batch = batch
        self.fetch = 0
        self.nitem = len(self.train_scp)
        self.cmvn = cmvn
        if cmvn:
            self.cmvn_mat = kaldi_cmvn.get_cmvn("cmvn.ark")

    def has_more(self):
        return self.fetch < self.nitem
    
    def reset(self):
        self.fetch = 0

    def apply_cmvn(self, feats):
        avg = self.cmvn_mat[0]
        var = self.cmvn_mat[1]
        assert(self.cmvn_mat.shape[1] == feats.shape[1])
        for idx in range(feats.shape[0]):
            feats[idx] -= avg
            feats[idx] /= var
        return feats

    def gen_input_samples(self, feats):
        nframe, featwnd = feats.shape
        input_size = (LCTX + RCTX + 1) * featwnd
        # print "[%d, %d]" %(featwnd, nframe)
        input_feats = np.zeros([nframe, input_size])
        # 0 0 0 1 2
        # init 
        cnt = prc = 0
        for idx in range(LCTX + RCTX + 1):
            base = featwnd * idx
            input_feats[prc, base: base + featwnd] = feats[cnt, :]
            if idx >= LCTX:
                cnt += 1
        prc += 1

        while prc < nframe:
            input_feats[prc, 0: input_size - featwnd] = input_feats[prc - 1, featwnd: input_size]
            input_feats[prc, input_size - featwnd: input_size] = feats[cnt, :]
            if cnt < nframe - 1:
                cnt += 1
            prc += 1

        return input_feats

    def gen_label_samples(self, ph_info, nframe):
        id = ph_info[0]
        sp = [int(n) for n in ph_info[1: len(ph_info)]]
        assert(len(sp) == 8)

        # prev 20: hello xiaogua
        # fix: xiaogua has two segment [2, 3] [6, 7]
        x = 4 if int(id[-2:]) <= 20 else 0
        c = np.zeros([nframe, 3])
        for idx in range(nframe):
            if idx >= sp[x] and idx <= sp[x + 1]:
                c[idx][0] = 1
            elif (idx >= sp[2] and idx <= sp[3]) or (idx >= sp[6] and idx <= sp[7]):
                c[idx][1] = 1
            else:
                c[idx][2] = 1
        return c        

    def fetch_next_batch(self):
        # init matrix with single cols
        # train_x = np.zeros([1, (LCTX + RCTX + 1) * 40])
        # label_y = np.zeros([1, 3])
        LX = []
        LY = []
        tot_frames = tot_labels = 0
        base = self.fetch
        intr = self.batch if base + self.batch < self.nitem else self.nitem - base
        # print "fetch from scp[%d, %d]" %(base, base + intr)
        for idx in range(base, base + intr):
            # sys.stdout.write("\r")
            feats = htkreader.HTKFeat_read(self.train_scp[idx][1]).getall()
            if self.cmvn:
                feats = self.apply_cmvn(feats)
            x = self.gen_input_samples(feats)
            y = self.gen_label_samples(self.label_dat[idx], feats.shape[0])
            tot_frames += x.shape[0]
            tot_labels += y.shape[0]
            LX.append(x)
            LY.append(y)
            # sys.stdout.write(str(idx))
            # sys.stdout.flush()
        self.fetch = base + intr

        assert(tot_frames == tot_labels)
        train_x = np.zeros([tot_frames, (LCTX + RCTX + 1) * 40])
        label_y = np.zeros([tot_labels, 3])
        base = 0
        for idx in range(intr):
            assert(LX[idx].shape[0] == LY[idx].shape[0])
            inc = LX[idx].shape[0]
            train_x[base: base + inc, :] = LX[idx]
            label_y[base: base + inc, :] = LY[idx]
            base += inc
        
        return train_x, label_y
