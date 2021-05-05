package com.jasonvzhao.core;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;


/**
 * ================================================
 * 描    述：全局的activity生命周期回调
 * ================================================
 */
public class FloatActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {
    private int startedActivityCounts;
    /**
     * fragment 生命周期回调
     */
    private FragmentManager.FragmentLifecycleCallbacks sFragmentLifecycleCallbacks;

    public FloatActivityLifecycleCallbacks() {
        sFragmentLifecycleCallbacks = new FloatFragmentLifecycleCallbacks();
    }

    public static void install(Application application) {
        application.registerActivityLifecycleCallbacks(new FloatActivityLifecycleCallbacks());
        ScreenUtils.install(application);
        ActivityUtils.init(application);
        FloatViewManager.getInstance().init(application);
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        recordActivityLifeCycleStatus(activity, LIFE_CYCLE_STATUS_CREATE);
        if (activity instanceof FragmentActivity) {
            //注册fragment生命周期回调
            ((FragmentActivity) activity).getSupportFragmentManager().registerFragmentLifecycleCallbacks(sFragmentLifecycleCallbacks, true);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (startedActivityCounts == 0) {
            FloatViewManager.getInstance().notifyForeground();

        }
        startedActivityCounts++;
    }

    @Override
    public void onActivityResumed(Activity activity) {
        recordActivityLifeCycleStatus(activity, LIFE_CYCLE_STATUS_RESUME);


        //添加DokitView
        resumeAndAttachDokitViews(activity);


    }


    @Override
    public void onActivityPaused(Activity activity) {


        FloatViewManager.getInstance().onActivityPause(activity);
    }


    @Override
    public void onActivityStopped(Activity activity) {
        recordActivityLifeCycleStatus(activity, LIFE_CYCLE_STATUS_STOPPED);
        startedActivityCounts--;
        //通知app退出到后台
        if (startedActivityCounts == 0) {
            FloatViewManager.getInstance().notifyBackground();
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        recordActivityLifeCycleStatus(activity, LIFE_CYCLE_STATUS_DESTROY);
        FloatViewManager.getInstance().onActivityDestroy(activity);
    }


    /**
     * 显示所有应该显示的dokitView
     *
     * @param activity
     */
    private void resumeAndAttachDokitViews(Activity activity) {
        FloatViewManager.getInstance().resumeAndAttachDokitViews(activity);


    }


    /**
     * Activity 创建
     */
    private static int LIFE_CYCLE_STATUS_CREATE = 100;
    /**
     * Activity resume
     */
    private static int LIFE_CYCLE_STATUS_RESUME = 101;
    /**
     * Activity stop
     */
    private static int LIFE_CYCLE_STATUS_STOPPED = 102;
    /**
     * Activity destroy
     */
    private static int LIFE_CYCLE_STATUS_DESTROY = 103;


    /**
     * 记录当前Activity的生命周期状态
     */
    private void recordActivityLifeCycleStatus(Activity activity, int lifeCycleStatus) {
        ActivityLifecycleInfo activityLifecaycleInfo = FloatConstant.ACTIVITY_LIFECYCLE_INFOS.get(activity.getClass().getCanonicalName());
        if (activityLifecaycleInfo == null) {
            activityLifecaycleInfo = new ActivityLifecycleInfo();
            activityLifecaycleInfo.setActivityName(activity.getClass().getCanonicalName());
            if (lifeCycleStatus == LIFE_CYCLE_STATUS_CREATE) {
                activityLifecaycleInfo.setActivityLifeCycleCount(0);
            } else if (lifeCycleStatus == LIFE_CYCLE_STATUS_RESUME) {
                activityLifecaycleInfo.setActivityLifeCycleCount(activityLifecaycleInfo.getActivityLifeCycleCount() + 1);
            } else if (lifeCycleStatus == LIFE_CYCLE_STATUS_STOPPED) {
                activityLifecaycleInfo.setInvokeStopMethod(true);
            }
            FloatConstant.ACTIVITY_LIFECYCLE_INFOS.put(activity.getClass().getCanonicalName(), activityLifecaycleInfo);
        } else {
            activityLifecaycleInfo.setActivityName(activity.getClass().getCanonicalName());
            if (lifeCycleStatus == LIFE_CYCLE_STATUS_CREATE) {
                activityLifecaycleInfo.setActivityLifeCycleCount(0);
            } else if (lifeCycleStatus == LIFE_CYCLE_STATUS_RESUME) {
                activityLifecaycleInfo.setActivityLifeCycleCount(activityLifecaycleInfo.getActivityLifeCycleCount() + 1);
            } else if (lifeCycleStatus == LIFE_CYCLE_STATUS_STOPPED) {
                activityLifecaycleInfo.setInvokeStopMethod(true);
            } else if (lifeCycleStatus == LIFE_CYCLE_STATUS_DESTROY) {
                FloatConstant.ACTIVITY_LIFECYCLE_INFOS.remove(activity.getClass().getCanonicalName());
            }
        }
    }
}
