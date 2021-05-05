package com.jasonvzhao.floatview;

import android.os.Bundle;

import com.jasonvzhao.core.FloatIntent;
import com.jasonvzhao.core.FloatViewManager;
import com.jasonvzhao.core.IgnoreShowDokitViewActivity;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity2 extends AppCompatActivity implements IgnoreShowDokitViewActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        FloatIntent floatIntent = new FloatIntent(WaitMatchFloatView.class);
//        dokitIntent.mode=DokitIntent.MODE_ONCE;
        FloatViewManager.getInstance().attach(floatIntent);
    }
}