package com.hitomi.aslibrary;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import java.util.List;

/**
 * Created by hitomi on 2016/10/11.
 */
class ActivitySwitcherHelper {

    private Context appContext;

    private ActivityManager actManager;

    private ActivityControllerLayout actControllerLayout;

    public ActivitySwitcherHelper(@NonNull Application application) {
        appContext = application;
        actManager = ActivityManager.getInstance();
        application.registerActivityLifecycleCallbacks(actManager);
        actControllerLayout = new ActivityControllerLayout(application);
    }

    public void startSwitch() {
        List<Activity> preActivities = actManager.getPreActivies();
        Activity currAct = actManager.getCurrentActivity();
        preActivities.add(currAct);

        ViewGroup contentViewGroup, contentView;
        int i = 0;
        for (Activity activity : preActivities) {
            if (activity.getWindow() == null) continue;
            contentViewGroup = getContentView(activity.getWindow());
            contentView = (ViewGroup) contentViewGroup.getChildAt(0);
            contentViewGroup.removeView(contentView);
            i++;
            contentView.setX(i * 50);
            actControllerLayout.addView(contentView);
        }

        FrameLayout currContentView = getContentView(currAct.getWindow());
        currContentView.addView(actControllerLayout);
    }

    private final FrameLayout getContentView(Window window) {
        if(window == null) return null;
        return (FrameLayout) window.findViewById(Window.ID_ANDROID_CONTENT);
    }



}
