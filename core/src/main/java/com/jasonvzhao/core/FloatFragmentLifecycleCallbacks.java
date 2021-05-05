package com.jasonvzhao.core;

import android.content.Context;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * ================================================
 * 描    述：全局的fragment 生命周期回调
 * ================================================
 */
public class FloatFragmentLifecycleCallbacks extends FragmentManager.FragmentLifecycleCallbacks {
    private static final String TAG = "DokitFragmentLifecycleCallbacks";


    @Override
    public void onFragmentAttached(FragmentManager fm, Fragment fragment, Context context) {
        super.onFragmentAttached(fm, fragment, context);


    }

    @Override
    public void onFragmentDetached(FragmentManager fm, Fragment fragment) {
        super.onFragmentDetached(fm, fragment);
    }
}
