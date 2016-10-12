package com.hitomi.aslibrary;

import android.app.Application;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;

/**
 * Created by hitomi on 2016/10/11.
 */
public class ActivitySwitcher {

    private ActivitySwitcherHelper switcherHelper;

    /**
     * 手势识别器
     */
    private GestureDetectorCompat menuDetector;

    private boolean isOpen;


    private static class SingletonHolder {
        public final static ActivitySwitcher instance = new ActivitySwitcher();
    }

    public static ActivitySwitcher getInstance() {
        return SingletonHolder.instance;
    }

    public void init(Application application) {
        switcherHelper = new ActivitySwitcherHelper(application);
        menuDetector = new GestureDetectorCompat(application, menuGestureListener);
    }

    private GestureDetector.SimpleOnGestureListener menuGestureListener = new GestureDetector.SimpleOnGestureListener() {

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (distanceX <= -50 && Math.abs(distanceY) < 20 && !isOpen) {
                showSwitcher();
                isOpen = true;
            }
            return true;
        }


    };

    public void processTouchEvent(MotionEvent event) {
        menuDetector.onTouchEvent(event);
    }

    public void showSwitcher() {
        switcherHelper.startSwitch();
    }

}
