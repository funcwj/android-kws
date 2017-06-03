package com.stu.wujian.kwsdemo;

import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.widget.TextView;

import com.stu.wujian.kwsdemo.utils.KWSKERNEL;

/**
 * Created by wujian on 17-1-4.
 */

public class InitSystem extends AsyncTask<Void, Void, Boolean> {

    private TextView panel;
    private AssetManager asset;

    public InitSystem(TextView textView, AssetManager assetManager) {
        panel = textView;
        asset = assetManager;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        KWSKERNEL KWS = new KWSKERNEL();
        return KWS.initSystem(asset);
    }

    @Override
    protected void onPostExecute(Boolean status) {
        String msg = status == true ? "Initialization ok" : "Initialization failed";
        panel.setText(msg);
    }
}
