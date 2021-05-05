package com.jasonvzhao.core;

/**
 * ================================================
 * ================================================
 */
public class ActivityLifecycleInfo {
    /**
     * activityLifeCycleCount = 1 页面创建调用onResume
     * activityLifeCycleCount > 1 页面创建返回onResume
     */
    public static final int ACTIVITY_LIFECYCLE_CREATE2RESUME = 1;
    /**
     * 生命周期是否已经调用过stop 交叉判断是第一次调用resume还是页面返回调用resume
     */
    private boolean invokeStopMethod = false;
    private String activityName;
    /**
     * activityLifeCycleCount = 1 页面创建调用onResume
     * activityLifeCycleCount > 1 页面创建返回onResume
     */
    private int activityLifeCycleCount = 0;

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public int getActivityLifeCycleCount() {
        return activityLifeCycleCount;
    }

    public void setActivityLifeCycleCount(int activityLifeCycleCount) {
        this.activityLifeCycleCount = activityLifeCycleCount;
    }

    public boolean isInvokeStopMethod() {
        return invokeStopMethod;
    }

    public void setInvokeStopMethod(boolean invokeStopMethod) {
        this.invokeStopMethod = invokeStopMethod;
    }
}
