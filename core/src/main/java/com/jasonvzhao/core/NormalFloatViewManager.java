package com.jasonvzhao.core;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * 每个activity悬浮窗管理类
 */

class NormalFloatViewManager implements FloatViewManagerInterface {
    private static final String TAG = "NormalDokitViewManager";
    /**
     * 每个Activity中dokitView的集合
     */
    private Map<Activity, Map<String, AbsFloatView>> mActivityDokitViews;
    /**
     * 全局的同步mActivityFloatDokitViews 应该在页面上显示的dokitView集合
     */
    private Map<String, GlobalSingleDokitViewInfo> mGlobalSingleDokitViews;

    private Context mContext;

    /**
     * 当app进入后台时调用
     */
    @Override
    public void notifyBackground() {
        if (mActivityDokitViews == null) {
            return;
        }
        //双层遍历
        for (Map<String, AbsFloatView> dokitViewMap : mActivityDokitViews.values()) {
            for (AbsFloatView dokitView : dokitViewMap.values()) {
                dokitView.onEnterBackground();
            }
        }
    }

    /**
     * 当app进入前台时调用
     */
    @Override
    public void notifyForeground() {
        if (mActivityDokitViews == null) {
            return;
        }
        //双层遍历
        for (Map<String, AbsFloatView> dokitViewMap : mActivityDokitViews.values()) {
            for (AbsFloatView dokitView : dokitViewMap.values()) {
                dokitView.onEnterForeground();
            }
        }
    }

    public NormalFloatViewManager(Context context) {
        mContext = context.getApplicationContext();
        //创建key为Activity的dokitView
        mActivityDokitViews = new HashMap<>();
        mGlobalSingleDokitViews = new HashMap<>();
    }


    /**
     * 添加activity关联的所有dokitView activity resume的时候回调
     *
     * @param activity
     */
    @Override
    public void resumeAndAttachDokitViews(Activity activity) {
        if (mActivityDokitViews == null) {
            return;
        }


        ActivityLifecycleInfo activityLifecycleInfo = FloatConstant.ACTIVITY_LIFECYCLE_INFOS.get(activity.getClass().getCanonicalName());
        if (activityLifecycleInfo == null) {
            return;
        }
        //新建Activity
        if (activityLifecycleInfo.getActivityLifeCycleCount() == ActivityLifecycleInfo.ACTIVITY_LIFECYCLE_CREATE2RESUME) {
            onActivityCreate(activity);
            return;
        }

        //activity resume
        if (activityLifecycleInfo.getActivityLifeCycleCount() > ActivityLifecycleInfo.ACTIVITY_LIFECYCLE_CREATE2RESUME) {
            onActivityResume(activity);
        }

    }

    /**
     * 应用启动
     */
    @Override
    public void onMainActivityCreate(Activity activity) {

    }

    /**
     * 新建activity
     *
     * @param activity
     */
    @Override
    public void onActivityCreate(Activity activity) {
        if (mGlobalSingleDokitViews == null) {

            return;
        }

        //将所有的dokitView添加到新建的Activity中去
        for (GlobalSingleDokitViewInfo dokitViewInfo : mGlobalSingleDokitViews.values()) {
            FloatIntent floatIntent = new FloatIntent(dokitViewInfo.getAbsDokitViewClass());
            floatIntent.mode = FloatIntent.MODE_SINGLE_INSTANCE;
            floatIntent.bundle = dokitViewInfo.getBundle();
            attach(floatIntent);
        }
    }

    /**
     * activity onResume
     *
     * @param activity
     */
    @Override
    public void onActivityResume(Activity activity) {
        if (mActivityDokitViews == null) {
            return;
        }
        Map<String, AbsFloatView> existDokitViews = mActivityDokitViews.get(activity);
        //先清除页面上启动模式为DokitIntent.MODE_ONCE 的dokitView
        if (existDokitViews != null) {
            List<String> modeOnceDokitViews = new ArrayList<>();
            for (AbsFloatView existDokitView : existDokitViews.values()) {
                if (existDokitView.getMode() == FloatIntent.MODE_ONCE) {
                    modeOnceDokitViews.add(existDokitView.getClass().getSimpleName());
                }
            }

            for (String tag : modeOnceDokitViews) {
                detach(tag);
            }

        }


        //更新所有全局DokitView的位置
        if (mGlobalSingleDokitViews != null && mGlobalSingleDokitViews.size() > 0) {
            for (GlobalSingleDokitViewInfo globalSingleDokitViewInfo : mGlobalSingleDokitViews.values()) {

                AbsFloatView existDokitView = null;
                if (existDokitViews != null && !existDokitViews.isEmpty()) {
                    existDokitView = existDokitViews.get(globalSingleDokitViewInfo.getTag());
                }

                //当前页面已存在dokitview
                if (existDokitView != null && existDokitView.getRootView() != null) {
                    existDokitView.getRootView().setVisibility(View.VISIBLE);
                    //更新位置
                    existDokitView.updateViewLayout(existDokitView.getTag(), true);
                    existDokitView.onResume();
                } else {
                    //添加相应的
                    FloatIntent floatIntent = new FloatIntent(globalSingleDokitViewInfo.getAbsDokitViewClass());
                    floatIntent.mode = globalSingleDokitViewInfo.getMode();
                    floatIntent.bundle = globalSingleDokitViewInfo.getBundle();
                    attach(floatIntent);
                }
            }

        }

    }


    @Override
    public void onActivityPause(Activity activity) {
        Map<String, AbsFloatView> dokitViews = getDokitViews(activity);
        for (AbsFloatView absFloatView : dokitViews.values()) {
            absFloatView.onPause();
        }
    }


    /**
     * 在当前Activity中添加指定悬浮窗
     *
     * @param floatIntent
     */
    @Override
    public void attach(final FloatIntent floatIntent) {
        try {
            if (floatIntent.activity == null) {
                return;
            }
            if (floatIntent.targetClass == null) {
                return;
            }

            //通过newInstance方式创建floatPage
            final AbsFloatView dokitView = floatIntent.targetClass.newInstance();
            //判断当前Activity是否存在dokitView map
            Map<String, AbsFloatView> dokitViews;
            if (mActivityDokitViews.get(floatIntent.activity) == null) {
                dokitViews = new HashMap<>();
                mActivityDokitViews.put(floatIntent.activity, dokitViews);
            } else {
                dokitViews = mActivityDokitViews.get(floatIntent.activity);
            }
            //判断该dokitview是否已经显示在页面上 同一个类型的dokitview 在页面上只显示一个
            if (floatIntent.mode == FloatIntent.MODE_SINGLE_INSTANCE) {
                if (dokitViews.get(floatIntent.getTag()) != null) {
                    //拿到指定的dokitView并更新位置
                    dokitViews.get(floatIntent.getTag()).updateViewLayout(floatIntent.getTag(), true);
                    return;
                }
            }
            //在全局dokitviews中保存该类型的
            if (floatIntent.mode == FloatIntent.MODE_SINGLE_INSTANCE) {
                if (mGlobalSingleDokitViews != null) {
                    mGlobalSingleDokitViews.put(dokitView.getTag(), createGlobalSingleDokitViewInfo(dokitView));
                }
            }
            if (floatIntent.activity instanceof IgnoreShowDokitViewActivity) {
                return;
            }
            //在当前Activity中保存dokitView
            dokitViews.put(dokitView.getTag(), dokitView);
            //设置dokitview的属性
            dokitView.setMode(floatIntent.mode);
            dokitView.setBundle(floatIntent.bundle);
            dokitView.setTag(floatIntent.getTag());
            dokitView.setActivity(floatIntent.activity);
            dokitView.performCreate(mContext);

            //得到activity window中的根布局
            final FrameLayout mDecorView = (FrameLayout) floatIntent.activity.getWindow().getDecorView();
            //往DecorView的子RootView中添加dokitView
            if (dokitView.getNormalLayoutParams() != null && dokitView.getRootView() != null) {
                getDokitRootContentView(floatIntent.activity, mDecorView)
                        .addView(dokitView.getRootView(),
                                dokitView.getNormalLayoutParams());
                //延迟100毫秒调用
                dokitView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dokitView.onResume();
                        //操作DecorRootView
                        dokitView.dealDecorRootView(getDokitRootContentView(floatIntent.activity, mDecorView));
                    }
                }, 100);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final String dokit_contentview_tag = "dokit_contentview_tag";

    /**
     * @return rootView
     */
    private FrameLayout getDokitRootContentView(final Activity activity, FrameLayout decorView) {
        FrameLayout dokitRootView = decorView.findViewWithTag(dokit_contentview_tag);
        if (dokitRootView != null) {
            return dokitRootView;
        }

        dokitRootView = new FloatFrameLayout(mContext);
        //普通模式的返回按键监听
        dokitRootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                //LogHelper.i(TAG, "keyCode===>" + keyCode + " " + v.getClass().getSimpleName());
                //监听返回键
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    Map<String, AbsFloatView> dokitViews = getDokitViews(activity);
                    if (dokitViews == null || dokitViews.size() == 0) {
                        return false;
                    }
                    for (AbsFloatView dokitView : dokitViews.values()) {
                        if (dokitView.shouldDealBackKey()) {
                            return dokitView.onBackPressed();
                        }
                    }
                    return false;
                }
                return false;
            }
        });
        dokitRootView.setClipChildren(false);
        //解决无法获取返回按键的问题
        dokitRootView.setFocusable(true);
        dokitRootView.setFocusableInTouchMode(true);
        dokitRootView.requestFocus();
        dokitRootView.setTag(dokit_contentview_tag);

        FrameLayout.LayoutParams dokitParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        try {
            //解决由于项目集成SwipeBackLayout而出现的dokit入口不显示
            if (StatusBarUtil.isStatusBarVisible(activity)) {
                dokitParams.topMargin = StatusBarUtil.getStatusBarHeight();
            }
            if (StatusBarUtil.isSupportNavBar(activity)) {
                if (StatusBarUtil.isNavBarVisible(activity)) {
                    dokitParams.bottomMargin = StatusBarUtil.getNavBarHeight();
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
        dokitRootView.setLayoutParams(dokitParams);
        //添加到DecorView中 为了不和用户自己往根布局中添加view干扰
        decorView.addView(dokitRootView);

        return dokitRootView;
    }


    /**
     * 移除每个activity指定的dokitView
     */
    @Override
    public void detach(AbsFloatView dokitView) {
        if (mActivityDokitViews == null) {
            return;
        }

        //调用当前Activity的指定dokitView的Destroy方法
        //dokitView.performDestroy();

        detach(dokitView.getTag());

    }

    @Override
    public void detach(Activity activity, AbsFloatView dokitView) {
        detach(activity, dokitView.getTag());
    }


    /**
     * 根据tag 移除ui和列表中的数据
     *
     * @param tag
     */
    @Override
    public void detach(String tag) {
        if (mActivityDokitViews == null) {
            return;
        }
        //移除每个activity中指定的dokitView
        for (Activity activityKey : mActivityDokitViews.keySet()) {
            Map<String, AbsFloatView> dokitViews = mActivityDokitViews.get(activityKey);
            //定位到指定dokitView
            AbsFloatView dokitView = dokitViews.get(tag);
            if (dokitView == null) {
                continue;
            }
            if (dokitView.getRootView() != null) {
                dokitView.getRootView().setVisibility(View.GONE);
                getDokitRootContentView(dokitView.getActivity(), (FrameLayout) activityKey.getWindow().getDecorView()).removeView(dokitView.getRootView());
            }

            //移除指定UI
            //请求重新绘制
            activityKey.getWindow().getDecorView().requestLayout();
            //执行dokitView的销毁
            dokitView.performDestroy();
            //移除map中的数据
            dokitViews.remove(tag);

        }
        //同步移除全局指定类型的dokitView
        if (mGlobalSingleDokitViews != null && mGlobalSingleDokitViews.containsKey(tag)) {
            mGlobalSingleDokitViews.remove(tag);
        }

    }

    @Override
    public void detach(Activity activity, String tag) {
        if (activity == null) {
            return;
        }
        Map<String, AbsFloatView> dokitViews = mActivityDokitViews.get(activity);
        if (dokitViews == null) {
            return;
        }
        //定位到指定dokitView
        AbsFloatView dokitView = dokitViews.get(tag);
        if (dokitView == null) {
            return;
        }
        if (dokitView.getRootView() != null) {
            dokitView.getRootView().setVisibility(View.GONE);
            getDokitRootContentView(dokitView.getActivity(), (FrameLayout) activity.getWindow().getDecorView()).removeView(dokitView.getRootView());
        }

        //移除指定UI
        //请求重新绘制
        activity.getWindow().getDecorView().requestLayout();
        //执行dokitView的销毁
        dokitView.performDestroy();
        //移除map中的数据
        dokitViews.remove(tag);

        if (mGlobalSingleDokitViews != null && mGlobalSingleDokitViews.containsKey(tag)) {
            mGlobalSingleDokitViews.remove(tag);
        }
    }

    @Override
    public void detach(Class<? extends AbsFloatView> dokitViewClass) {
        detach(dokitViewClass.getSimpleName());
    }

    @Override
    public void detach(Activity activity, Class<? extends AbsFloatView> dokitViewClass) {
        detach(activity, dokitViewClass.getSimpleName());
    }


    /**
     * 移除所有activity的所有dokitView
     */
    @Override
    public void detachAll() {
        if (mActivityDokitViews == null) {
            return;
        }

        //移除每个activity中所有的dokitView
        for (Activity activityKey : mActivityDokitViews.keySet()) {
            Map<String, AbsFloatView> dokitViews = mActivityDokitViews.get(activityKey);
            //移除指定UI
            getDokitRootContentView(activityKey, (FrameLayout) activityKey.getWindow().getDecorView()).removeAllViews();
            //移除map中的数据
            dokitViews.clear();
        }
        if (mGlobalSingleDokitViews != null) {

            mGlobalSingleDokitViews.clear();
        }
    }

    /**
     * Activity销毁时调用
     */
    @Override
    public void onActivityDestroy(Activity activity) {
        if (mActivityDokitViews == null) {
            return;
        }
        Map<String, AbsFloatView> dokitViewMap = getDokitViews(activity);
        if (dokitViewMap == null) {
            return;
        }
        for (AbsFloatView dokitView : dokitViewMap.values()) {
            dokitView.performDestroy();
        }
        mActivityDokitViews.remove(activity);
    }


    /**
     * 获取当前页面指定的dokitView
     *
     * @param activity
     * @param tag
     * @return
     */
    @Override
    public AbsFloatView getDokitView(Activity activity, String tag) {
        if (TextUtils.isEmpty(tag) || activity == null) {
            return null;
        }
        if (mActivityDokitViews == null) {
            return null;
        }
        if (mActivityDokitViews.get(activity) == null) {
            return null;
        }
        return mActivityDokitViews.get(activity).get(tag);
    }


    /**
     * 获取当前页面所有的dokitView
     *
     * @param activity
     * @return
     */
    @NonNull
    @Override
    public Map<String, AbsFloatView> getDokitViews(Activity activity) {
        if (activity == null) {
            return Collections.emptyMap();
        }
        if (mActivityDokitViews == null) {
            return Collections.emptyMap();
        }

        return mActivityDokitViews.get(activity) == null ? Collections.<String, AbsFloatView>emptyMap() : mActivityDokitViews.get(activity);
    }


    private GlobalSingleDokitViewInfo createGlobalSingleDokitViewInfo(AbsFloatView dokitView) {
        return new GlobalSingleDokitViewInfo(dokitView.getClass(), dokitView.getTag(), FloatIntent.MODE_SINGLE_INSTANCE, dokitView.getBundle());
    }

}
