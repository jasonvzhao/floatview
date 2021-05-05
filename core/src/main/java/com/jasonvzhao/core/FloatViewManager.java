package com.jasonvzhao.core;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;

import java.util.HashMap;
import java.util.Map;

/**
 * 浮标管理类
 */
public class FloatViewManager implements FloatViewManagerInterface {
    private static final String TAG = "DokitViewManagerProxy";
    /**
     * 每个类型在页面中的位置 只保存marginLeft 和marginTop
     */
    private static Map<String, Point> mDokitViewPos;

    private Map<String, LastFloatViewPosInfo> mLastDokitViewPosInfoMaps;


    private FloatViewManagerInterface mDokitViewManager;
    private Context mContext;


    /**
     * 静态内部类单例
     */
    private static class Holder {
        private static FloatViewManager INSTANCE = new FloatViewManager();
    }

    public static FloatViewManager getInstance() {
        return Holder.INSTANCE;
    }


    public void init(Context context) {
        mContext = context;
        mDokitViewManager = new NormalFloatViewManager(context);
        mDokitViewPos = new HashMap<>();
        mLastDokitViewPosInfoMaps = new HashMap<>();
    }


    /**
     * 当app进入后台时调用
     */
    @Override
    public void notifyBackground() {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.notifyBackground();
    }

    /**
     * 当app进入前台时调用
     */
    @Override
    public void notifyForeground() {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.notifyForeground();
    }

    /**
     * 只有普通浮标才会调用
     * 保存每种类型dokitView的位置
     */
   public void saveDokitViewPos(String tag, int marginLeft, int marginTop) {
        if (mDokitViewPos == null) {
            return;
        }

        if (mDokitViewPos.get(tag) == null) {
            Point point = new Point(marginLeft, marginTop);
            mDokitViewPos.put(tag, point);
        } else {
            Point point = mDokitViewPos.get(tag);
            if (point != null) {
                point.set(marginLeft, marginTop);
            }
        }


    }

    /**
     * 只有普通的浮标才需要调用
     * 获得指定dokitView的位置信息
     *
     * @param tag
     * @return
     */
    Point getDokitViewPos(String tag) {
        if (mDokitViewPos == null) {
            return null;
        }

//        for (String key : mDokitViewPos.keySet()) {
//            LogHelper.i(TAG, "getDokitViewPos  key==> " + key + "  point===>" + mDokitViewPos.get(key));
//        }
        return mDokitViewPos.get(tag);
    }

    /**
     * 只有普通的浮标才需要调用
     * 添加activity关联的所有dokitView activity resume的时候回调
     *
     * @param activity
     */
    @Override
    public void resumeAndAttachDokitViews(Activity activity) {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.resumeAndAttachDokitViews(activity);
    }

    @Override
    public void onMainActivityCreate(Activity activity) {

    }

    @Override
    public void onActivityCreate(Activity activity) {

    }

    @Override
    public void onActivityResume(Activity activity) {

    }

    @Override
    public void onActivityPause(Activity activity) {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.onActivityPause(activity);
    }

    /**
     * 在当前Activity中添加指定悬浮窗
     *
     * @param floatIntent
     */
    @Override
    public void attach(FloatIntent floatIntent) {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.attach(floatIntent);
    }






    /**
     * 移除每个activity指定的dokitView
     */
    @Override
    public void detach(String tag) {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.detach(tag);
    }

    @Override
    public void detach(Activity activity, String tag) {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.detach(activity, tag);
    }


    /**
     * 移除每个activity指定的dokitView
     */
    @Override
    public void detach(AbsFloatView dokitView) {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.detach(dokitView);
    }

    @Override
    public void detach(Activity activity, AbsFloatView dokitView) {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.detach(activity, dokitView);
    }


    @Override
    public void detach(Class<? extends AbsFloatView> dokitViewClass) {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.detach(dokitViewClass);
    }

    @Override
    public void detach(Activity activity, Class<? extends AbsFloatView> dokitViewClass) {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.detach(activity, dokitViewClass);
    }

    /**
     * 移除所有activity的所有dokitView
     */
    @Override
    public void detachAll() {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.detachAll();
    }


    /**
     * Activity销毁时调用
     */
    @Override
    public void onActivityDestroy(Activity activity) {
        if (mDokitViewManager == null) {
            return;
        }
        mDokitViewManager.onActivityDestroy(activity);
    }

    /**
     * 获取页面上指定的dokitView
     *
     * @param activity 如果是系统浮标 activity可以为null
     * @param tag
     * @return
     */
    @Override
    public AbsFloatView getDokitView(Activity activity, String tag) {
        if (mDokitViewManager == null) {
            return null;
        }
        return mDokitViewManager.getDokitView(activity, tag);
    }

    /**
     * @param activity
     * @return
     */
    @Override
    public Map<String, AbsFloatView> getDokitViews(Activity activity) {
        if (mDokitViewManager == null) {
            return new HashMap<>();
        }
        return mDokitViewManager.getDokitViews(activity);
    }







    void saveLastDokitViewPosInfo(String key, LastFloatViewPosInfo lastFloatViewPosInfo) {
        if (mLastDokitViewPosInfoMaps != null) {
            mLastDokitViewPosInfoMaps.put(key, lastFloatViewPosInfo);
        }
    }

    LastFloatViewPosInfo getLastDokitViewPosInfo(String key) {
        if (mLastDokitViewPosInfoMaps == null) {
            return null;
        }
        return mLastDokitViewPosInfoMaps.get(key);
    }


    void removeLastDokitViewPosInfo(String key) {
        if (mLastDokitViewPosInfoMaps == null) {
            return;
        }
        mLastDokitViewPosInfoMaps.remove(key);
    }

}
