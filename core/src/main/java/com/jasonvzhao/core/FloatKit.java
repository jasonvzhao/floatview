package com.jasonvzhao.core;

import android.app.Application;

public class FloatKit {

    public static void  install(Application application){
        FloatActivityLifecycleCallbacks.install(application);
    }
}
