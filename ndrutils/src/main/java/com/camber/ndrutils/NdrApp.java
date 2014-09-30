package com.camber.ndrutils;

import android.app.Activity;
import android.os.Bundle;

import com.camber.ndrutils.util.RootUtil;

public class NdrApp extends Activity {

    private static NdrApp mInstance = null;

    public static NdrApp getInstance() {
        return mInstance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInstance = this;
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new RebootFragment())
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RootUtil.getRoot().dispose();
    }
}
