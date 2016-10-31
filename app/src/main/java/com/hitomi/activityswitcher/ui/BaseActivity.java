package com.hitomi.activityswitcher.ui;

import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.widget.TextView;

import com.hitomi.activityswitcher.R;
import com.hitomi.aslibrary.ActivitySwitcher;

public class BaseActivity extends AppCompatActivity {

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        ActivitySwitcher.getInstance().processTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    protected void setActivityTag(String str) {
        TextView tvTag = (TextView) findViewById(R.id.text_view);
        tvTag.setTextSize(30);
        tvTag.setText(str);
    }

    protected  void setBackground(int color) {
        findViewById(R.id.relay_background).setBackgroundColor(color);
    }
}
