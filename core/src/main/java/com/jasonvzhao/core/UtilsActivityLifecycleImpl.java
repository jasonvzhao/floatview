package com.jasonvzhao.core;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;


public final class UtilsActivityLifecycleImpl implements Application.ActivityLifecycleCallbacks {

    public static final UtilsActivityLifecycleImpl INSTANCE = new UtilsActivityLifecycleImpl();
    private final LinkedList<Activity> mActivityList = new LinkedList<>();
    private int mForegroundCount = 0;
    private int mConfigCount = 0;
    private boolean mIsBackground = false;
    private Listener listener;
    private Application application;
    void init(Application application) {
        mActivityList.clear();
        application.registerActivityLifecycleCallbacks(this);
        this.application=application;
    }


    Activity getTopActivity() {
        List<Activity> activityList = getActivityList();
        for (Activity activity : activityList) {
            if (!isActivityAlive(activity)) {
                continue;
            }
            return activity;
        }
        return null;
    }

     List<Activity> getActivityList() {
        if (!mActivityList.isEmpty()) {
            return mActivityList;
        }
        List<Activity> reflectActivities = getActivitiesByReflect();
        mActivityList.addAll(reflectActivities);
        return mActivityList;
    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
        setTopActivity(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (!mIsBackground) {
            setTopActivity(activity);
        }
        if (mConfigCount < 0) {
            ++mConfigCount;
        } else {
            ++mForegroundCount;
        }
    }

    @Override
    public void onActivityResumed(@NonNull final Activity activity) {
        setTopActivity(activity);
        if (mIsBackground) {
            mIsBackground = false;
            if (listener != null) {
                listener.onBecameForeground();
            }
        }
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity.isChangingConfigurations()) {
            --mConfigCount;
        } else {
            --mForegroundCount;
            if (mForegroundCount <= 0) {
                mIsBackground = true;
                if (listener != null) {
                    listener.onBecameBackground();
                }
            }
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, Bundle outState) {/**/}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        mActivityList.remove(activity);
    }


    private void setTopActivity(final Activity activity) {
        if (mActivityList.contains(activity)) {
            if (!mActivityList.getFirst().equals(activity)) {
                mActivityList.remove(activity);
                mActivityList.addFirst(activity);
            }
        } else {
            mActivityList.addFirst(activity);
        }
    }


    private List<Activity> getActivitiesByReflect() {
        LinkedList<Activity> list = new LinkedList<>();
        Activity topActivity = null;
        try {
            Object activityThread = getActivityThread();
            Field mActivitiesField = activityThread.getClass().getDeclaredField("mActivities");
            mActivitiesField.setAccessible(true);
            Object mActivities = mActivitiesField.get(activityThread);
            if (!(mActivities instanceof Map)) {
                return list;
            }
            Map<Object, Object> binder_activityClientRecord_map = (Map<Object, Object>) mActivities;
            for (Object activityRecord : binder_activityClientRecord_map.values()) {
                Class activityClientRecordClass = activityRecord.getClass();
                Field activityField = activityClientRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Activity activity = (Activity) activityField.get(activityRecord);
                if (topActivity == null) {
                    Field pausedField = activityClientRecordClass.getDeclaredField("paused");
                    pausedField.setAccessible(true);
                    if (!pausedField.getBoolean(activityRecord)) {
                        topActivity = activity;
                    } else {
                        list.add(activity);
                    }
                } else {
                    list.add(activity);
                }
            }
        } catch (Exception e) {
            Log.e("UtilsActivityLifecycle", "getActivitiesByReflect: " + e.getMessage());
        }
        if (topActivity != null) {
            list.addFirst(topActivity);
        }
        return list;
    }

    private Object getActivityThread() {
        Object activityThread = getActivityThreadInActivityThreadStaticField();
        if (activityThread != null) return activityThread;
        activityThread = getActivityThreadInActivityThreadStaticMethod();
        if (activityThread != null) return activityThread;
        return getActivityThreadInLoadedApkField();
    }

    private Object getActivityThreadInActivityThreadStaticField() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Field sCurrentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            sCurrentActivityThreadField.setAccessible(true);
            return sCurrentActivityThreadField.get(null);
        } catch (Exception e) {
            Log.e("UtilsActivityLifecycle", "getActivityThreadInActivityThreadStaticField: " + e.getMessage());
            return null;
        }
    }

    private Object getActivityThreadInActivityThreadStaticMethod() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            return activityThreadClass.getMethod("currentActivityThread").invoke(null);
        } catch (Exception e) {
            Log.e("UtilsActivityLifecycle", "getActivityThreadInActivityThreadStaticMethod: " + e.getMessage());
            return null;
        }
    }

    private Object getActivityThreadInLoadedApkField() {
        try {
            Field mLoadedApkField = Application.class.getDeclaredField("mLoadedApk");
            mLoadedApkField.setAccessible(true);
            Object mLoadedApk = mLoadedApkField.get(application);
            Field mActivityThreadField = mLoadedApk.getClass().getDeclaredField("mActivityThread");
            mActivityThreadField.setAccessible(true);
            return mActivityThreadField.get(mLoadedApk);
        } catch (Exception e) {
            Log.e("UtilsActivityLifecycle", "getActivityThreadInLoadedApkField: " + e.getMessage());
            return null;
        }
    }

    private boolean isActivityAlive(final Activity activity) {
        return activity != null && !activity.isFinishing()
                && (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1 || !activity.isDestroyed());
    }

    public Application getApplicationByReflect() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object thread = getActivityThread();
            Object app = activityThreadClass.getMethod("getApplication").invoke(thread);
            if (app == null) {
                return null;
            }
            return (Application) app;
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void addListener(Listener listener) {
        this.listener = listener;
    }

    public void removeListener() {
        this.listener = null;
    }

    public interface Listener {
        public void onBecameForeground();

        public void onBecameBackground();
    }
}
