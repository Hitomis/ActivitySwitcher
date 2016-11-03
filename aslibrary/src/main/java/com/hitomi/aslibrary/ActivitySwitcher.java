package com.hitomi.aslibrary;

import android.app.Activity;
import android.app.Application;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * Created by hitomi on 2016/10/11.
 */
public class ActivitySwitcher {

    private ActivitySwitcherHelper switcherHelper;

    private OnActivitySwitchListener onActivitySwitchListener;

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
                    showSwitcher();
                }
                break;
        }
    }

    public void finishSwitch(Activity activity) {
        if (switcherHelper.isActivityControllerDisplayed()) {
            switcherHelper.endSwitch();
        } else if (switcherHelper.isActivityControllerClosed()) {
            activity.finish();
        }
    }

    public void showSwitcher() {
        switching = true;
        switcherHelper.startSwitch(onActivitySwitchListener);
    }

    public void setSwitching(boolean switching) {
        this.switching = switching;
    }

    public void setOnActivitySwitchListener(OnActivitySwitchListener listener) {
        onActivitySwitchListener = listener;
    }

    public interface OnActivitySwitchListener {

        void onSwitchStarted();

        void onSwitchFinished(Activity activity);
    }

}
