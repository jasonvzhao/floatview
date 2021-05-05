package com.jasonvzhao.core;

import android.os.Bundle;


/**
 * ================================================
 * 描    述：关于全局dokitView的基本信息 由于普通的浮标是每个页面自己管理的
 * 需要有一个map用来保存当前每个类型的dokitview 便于新开页面和页面resume时的dokitview添加
 * 修订历史：
 * ================================================
 */
class GlobalSingleDokitViewInfo {
    private Class<? extends AbsFloatView> mAbsDokitViewClass;
    private String mTag;
    private int mMode;
    private Bundle mBundle;

    GlobalSingleDokitViewInfo(Class<? extends AbsFloatView> absDokitViewClass, String tag, int mode, Bundle bundle) {
        this.mAbsDokitViewClass = absDokitViewClass;
        this.mTag = tag;
        this.mMode = mode;
        this.mBundle = bundle;
    }

    Class<? extends AbsFloatView> getAbsDokitViewClass() {
        return mAbsDokitViewClass;
    }


    public String getTag() {
        return mTag;
    }


    public int getMode() {
        return mMode;
    }


    public Bundle getBundle() {
        return mBundle;
    }


    @Override
    public String toString() {
        return "GlobalSingleDokitViewInfo{" +
                "absDokitViewClass=" + mAbsDokitViewClass +
                ", tag='" + mTag + '\'' +
                ", mode=" + mMode +
                ", bundle=" + mBundle +
                '}';
    }
}
