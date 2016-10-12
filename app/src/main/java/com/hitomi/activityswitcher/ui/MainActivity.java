package com.hitomi.activityswitcher.ui;

import android.content.Intent;
import android.os.Bundle;

import com.hitomi.activityswitcher.R;

public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(this, OneActivitry.class));
    }

}
