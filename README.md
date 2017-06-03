## Simple KWS Demo on Android

### Component
* log-filterbank feature extractor in C(according to HTK, seems out of time)
* simple forword network in C(ReLU and Softmax, only fits for the trainning model)
* data visualization by [MPAndroidChart](https://github.com/PhilJay/MPAndroidChart)
* train the simple model by [keras](https://keras.io/)

### Notes
* nnet and fbank-extractor interface are called through JNI
* trainning scripts are in folder [scripts](scripts), and hdf5tobin.py aims to transfer the model into binary which could be read by the JNI.

### Snapshots
[view](snaptshots/shots.png)
