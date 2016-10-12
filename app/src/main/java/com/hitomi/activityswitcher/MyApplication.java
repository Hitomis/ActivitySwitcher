package com.hitomi.activityswitcher;

import android.app.Application;

import com.hitomi.aslibrary.ActivitySwitcher;

/**
 * Created by hitomi on 2016/10/11.
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ActivitySwitcher.getInstance().init(this);
    }
}
