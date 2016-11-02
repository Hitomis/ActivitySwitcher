package com.hitomi.activityswitcher.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.hitomi.activityswitcher.R;
import com.hitomi.aslibrary.ActivitySwitcher;

public class MainActivity extends AppCompatActivity {

    private static int index;
    private static int totalCount = 8;
    private Button btnNext;
    private TextView tvPage;
    private int tag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnNext = (Button) findViewById(R.id.btn_next);
        tvPage = (TextView) findViewById(R.id.tv_page);
        tag = index;
        tvPage.setText("当前第" + (tag + 1) + "页" );
        if (index == totalCount - 1) {
            btnNext.setVisibility(View.GONE);
        }
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (++index > totalCount - 1) return;
                startActivity(new Intent(MainActivity.this, MainActivity.class));
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        ActivitySwitcher.getInstance().processTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        ActivitySwitcher.getInstance().finishSwitch(this);
    }

    @Override
    public void onActionModeFinished(ActionMode mode) {
        super.onActionModeFinished(mode);
    }

    @Override
    protected void onDestroy() {
        MainActivity.index = tag - 1 == 0 ? 0 : tag - 1;
        super.onDestroy();
    }
}
