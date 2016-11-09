package com.hitomi.aslibrary;

import android.app.Activity;
import android.app.Application;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 *
 * Activity 切换器。支持：
 * <ul>
 *     <li>Activity 之间任意跳转</li>
 *     <li>关闭任意N个 Activity</li>
 *     <li>结束应用程序</li>
 * </ul>
 *
 *
 * email : 196425254@qq.com <br/>
 *
 * github : https://github.com/Hitomis <br/>
 *
 * Created by hitomi on 2016/10/11.
 */
public class ActivitySwitcher {

    private ActivitySwitcherHelper switcherHelper;

    private boolean switching;

    private float preX, touchSlop;

    private static class SingletonHolder {
        public final static ActivitySwitcher instance = new ActivitySwitcher();
    }

    public static ActivitySwitcher getInstance() {
        return SingletonHolder.instance;
    }

    public void init(Application application) {
        switcherHelper = new ActivitySwitcherHelper(this, application);
        ViewConfiguration conf = ViewConfiguration.get(application);
        touchSlop = conf.getScaledEdgeSlop();
    }

    public void processTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                float slideX = event.getX() - preX;
                if (preX <= touchSlop && slideX > 50 && !switching) {
                    showSwitch();
                }
                break;
        }
    }

    public void exit() {
        switcherHelper.exit();
    }

    /**
     * 关闭 ActivitySwitcher 切换视图
     * @param activity
     */
    public void finishSwitch(Activity activity) {
        if (switcherHelper.isActivityControllerDisplayed()) {
            switcherHelper.endSwitch();
        } else if (switcherHelper.isActivityControllerClosed()) {
            activity.finish();
        }
    }

    /**
     * 显示 ActivitySwitcher 切换视图
     */
    public void showSwitch() {
        switching = true;
        switcherHelper.startSwitch();
    }

    void setSwitching(boolean switching) {
        this.switching = switching;
    }

    public void setOnActivitySwitchListener(OnActivitySwitchListener listener) {
        switcherHelper.setOnActivitySwitchListener(listener);
    }

    public interface OnActivitySwitchListener {

        void onSwitchStarted();

        void onSwitchFinished(Activity activity);
    }

}
