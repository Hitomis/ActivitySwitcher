package com.hitomi.aslibrary;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Activity 管理工具类
 *
 * email : 196425254@qq.com <br/>
 *
 * github : https://github.com/Hitomis <br/>
 *
 * Created by hitomi on 2016/10/11.
 */
class ActivityManager implements Application.ActivityLifecycleCallbacks {

    private static Stack<Activity> activityStack;

    private static class SingletonHolder {
        public final static ActivityManager instance = new ActivityManager();
    }

    public static ActivityManager getInstance() {
        activityStack = new Stack<>();
        return SingletonHolder.instance;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        addActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
    }

    @Override
    public void onActivityResumed(Activity activity) {
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        removeActivity(activity);
    }

    /**
     * 添加Activity到堆栈
     */
    public void addActivity(Activity activity) {
        if (activityStack == null) {
            activityStack = new Stack<>();
        }
        activityStack.add(activity);
    }

    /**
     * 获取当前Activity（堆栈中最后一个压入的）
     */
    public Activity getCurrentActivity() {
        Activity activity = activityStack.get(activityStack.size() - 1);
        return activity;
    }

    public Activity getPreActivity() {
        int size = activityStack.size();
        if(size < 2)return null;
        return activityStack.get(size - 2);
    }

    /**
     * 获取当前 Activity 之前所有 Activity
     * @return
     */
    public List<Activity> getPreActivies() {
        List<Activity> preActivities = new ArrayList<>();
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (activityStack.get(i) == getCurrentActivity()) {
                break;
            }
            preActivities.add(activityStack.get(i));
        }
//        activityStack.subList(from, to); 这个方法有毒，巨坑爹
        return preActivities;
    }

    /**
     * 结束当前Activity（堆栈中最后一个压入的）
     */
    public void finishActivity() {
        Activity activity = activityStack.get(activityStack.size() - 1);
        finishActivity(activity);
    }

    /**
     * 结束指定的Activity
     */
    public void finishActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
            activity.finish();
        }
    }

    /**
     * 结束所有Activity
     */
    public void finishAllActivity() {
        for (int i = 0, size = activityStack.size(); i < size; i++) {
            if (null != activityStack.get(i)) {
                Activity activity = activityStack.get(i);
                if (!activity.isFinishing()) {
                    activity.finish();
                }
            }
        }
        activityStack.clear();
    }

    public void removeActivity(Activity activity) {
        if (activity != null) {
            activityStack.remove(activity);
        }
    }

    public void removeAllWithoutItself(Activity activity) {
        activityStack.clear();
        addActivity(activity);
    }

}
