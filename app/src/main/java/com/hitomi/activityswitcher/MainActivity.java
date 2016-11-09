package com.hitomi.activityswitcher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hitomi.aslibrary.ActivitySwitcher;

public class MainActivity extends AppCompatActivity {

    static int index;
    static int totalCount = 8;
    static int[] bgColors = new int[] {
            Color.parseColor("#92c8d0"),
            Color.parseColor("#c4dcce"),
            Color.parseColor("#cd7b91"),
            Color.parseColor("#e5c5dc"),
            Color.parseColor("#742a8d"),
            Color.parseColor("#2eb2d8"),
            Color.parseColor("#b9d84e"),
            Color.parseColor("#35fe62")
    };

    private RelativeLayout relativeLayout;
    private Button btnNext;
    private TextView tvPage;

    private ActivitySwitcher activitySwitcher;
    private int tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activitySwitcher = ActivitySwitcher.getInstance();

        relativeLayout = (RelativeLayout) findViewById(R.id.relayout);
        btnNext = (Button) findViewById(R.id.btn_next);
        tvPage = (TextView) findViewById(R.id.tv_page);

        relativeLayout.setBackgroundColor(bgColors[index]);
        tag = index;
        tvPage.setText("当前第" + (tag + 1) + "页" );

        if (index == totalCount - 1) {
            btnNext.setText("退出程序");
        }
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (++index > totalCount - 1) {
                    activitySwitcher.exit();
                } else {
                    startActivity(new Intent(MainActivity.this, MainActivity.class));
                }
            }
        });

        activitySwitcher.setOnActivitySwitchListener(new ActivitySwitcher.OnActivitySwitchListener() {
            @Override
            public void onSwitchStarted() {}

            @Override
            public void onSwitchFinished(Activity activity) {
                if (activity instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) activity;
                    MainActivity.index = mainActivity.getTag();
                }
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        activitySwitcher.processTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        MainActivity.index = tag - 1 <= 0 ? 0 : tag - 1;
        activitySwitcher.finishSwitch(this);
    }

    public int getTag() {
        return tag;
    }
}
