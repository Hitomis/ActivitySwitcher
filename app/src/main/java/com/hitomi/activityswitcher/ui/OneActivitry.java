package com.hitomi.activityswitcher.ui;

import android.content.Intent;
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
        startActivity(new Intent(OneActivitry.this, TwoActivitry.class));

//        findViewById(R.id.btn).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(OneActivitry.this, TwoActivitry.class));
//            }
//        });
    }

}
