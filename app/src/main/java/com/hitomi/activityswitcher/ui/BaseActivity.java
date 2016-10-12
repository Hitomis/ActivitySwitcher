package com.hitomi.activityswitcher.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import com.hitomi.activityswitcher.R;
import com.hitomi.aslibrary.ActivitySwitcher;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        ActivitySwitcher.getInstance().processTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }
}
