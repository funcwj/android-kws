#!/usr/bin/env python

import generator
import sys
from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation

def LOG(his):
    for key, val in his.iteritems():
        print "%10s:%10f" % (key, float(val[0])),
    print ""

def train(data_dir):
    train_scp = data_dir + "train.scp"
    train_lbl = data_dir + "train.lbl"
    test_scp = data_dir + "test.scp"
    test_lbl = data_dir + "test.lbl"

    data_reader = generator.data_reader(train_scp, train_lbl, 10)
    test_reader = generator.data_reader(test_scp, test_lbl, 10)

    model = Sequential([Dense(128, input_shape = (1640, )),
        Activation('relu'), Dropout(0.2),
        Dense(128),
        Activation('relu'), Dropout(0.2),
        Dense(128),
        Activation('relu'), Dropout(0.2),
        Dense(3),
        Activation('softmax')]
    )

    model.compile(loss = 'categorical_crossentropy', optimizer = "nadam", metrics=['accuracy'])

    print "generate test samples"
    test_x, test_y = test_reader.fetch_next_batch()
    if not test_reader.has_more():
        print "fetch all test samples"

    for it in range(20):
        data_reader.reset()
        while data_reader.has_more():
            train_x, label_y = data_reader.fetch_next_batch()
            his = model.fit(train_x, label_y, batch_size = 1024, nb_epoch = 1, verbose = 0, validation_data = (test_x, test_y)).history
            LOG(his)
            # print model.evaluate(test_x, test_y, show_accuracy = True)
        model.save_weights('mdl.h5py', overwrite = True)
            # print "input size: ", train_x.shape
            # print "label size: ", label_y.shape

def main(argv):
    if len(argv) != 2:
        raise SystemExit('format error: {} [data-dir]'.format(argv[0]))
    data_dir = argv[1]
    if data_dir[-1] != '/'
        data_dir += '/'
    train(data_dir)

if __name__ == '__main__':
    main(sys.argv)
