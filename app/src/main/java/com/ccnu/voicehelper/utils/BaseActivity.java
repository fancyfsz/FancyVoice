package com.ccnu.voicehelper.utils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by mona on 16/3/9.
 */
public class BaseActivity extends Activity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("BaseActivity",getClass().getSimpleName());
    }
}
