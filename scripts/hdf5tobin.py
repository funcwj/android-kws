#!/usr/bin/env python

import sys
import h5py
import struct


if len(sys.argv) != 3:
    print "format error: %s [hdf5-in] [bin-out]" % sys.argv[0]
    sys.exit()

src = sys.argv[1]
dst = sys.argv[2]

mdl = h5py.File(src)

sav = open(dst, "wb")

for L in mdl.attrs['layer_names']:
    dense = mdl[L]
    for w in dense.attrs['weight_names']:
        if len(dense[w].shape) == 2:
            for x in dense[w].shape:
                sav.write(struct.pack("i", x))

        for v in dense[w]:
            if hasattr(v, '__len__'):
                for x in v:
                    sav.write(struct.pack("f", x))
            else:
                sav.write(struct.pack("f", v))

sav.close()