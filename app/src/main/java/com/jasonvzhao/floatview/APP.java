package com.jasonvzhao.floatview;

import android.app.Application;

import com.jasonvzhao.core.FloatKit;
import com.opensource.svgaplayer.SVGAParser;

public class APP extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FloatKit.install(this);
        SVGAParser.Companion.shareParser().init(this);
    }
}
