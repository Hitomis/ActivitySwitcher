package com.hitomi.activityswitcher.ui;

import android.graphics.Color;
import android.os.Bundle;

import com.hitomi.activityswitcher.R;

public class OneActivitry extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBackground(Color.parseColor("#8cb1b1"));
        setActivityTag("OneActivitry");
//        startActivity(new Intent(this, TwoActivitry.class));
    }

}
