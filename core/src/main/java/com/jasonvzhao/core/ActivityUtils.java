package com.jasonvzhao.core;

import android.app.Activity;
import android.app.Application;

import java.util.List;

/**
 * 全局管理Activity的工具类
 * Created by zk on 2020/4/24.
 */
public class ActivityUtils {

    public static void init(Application application){
        UtilsActivityLifecycleImpl.INSTANCE.init(application);
    }

    /**
     * 获取栈顶的Activity
     * @return
     */
    public static Activity getTopActivity() {
        return UtilsActivityLifecycleImpl.INSTANCE.getTopActivity();
    }

    /**
     * 获取Activity的栈链表
     * @return
     */
    public static List<Activity> getActivityList() {
        return UtilsActivityLifecycleImpl.INSTANCE.getActivityList();
    }

    public static void finshAllActivity(){
        List<Activity> activityList = getActivityList();
        for (int i = 0; i < activityList.size(); i++) {
            activityList.get(i).finish();
        }
    }

}
