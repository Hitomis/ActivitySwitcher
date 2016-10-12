package com.hitomi.activityswitcher.ui;

import android.graphics.Color;
import android.os.Bundle;

import com.hitomi.activityswitcher.R;

public class ThreeActivitry extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setBackground(Color.parseColor("#6b6bd8"));
        setActivityTag("ThreeActivitry");
    }

}
