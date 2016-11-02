package com.hitomi.aslibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

/**
 * Created by hitomi on 2016/11/2.
 */
public class ActivityContainer extends FrameLayout {

    private boolean intercept;

    public ActivityContainer(Context context) {
        this(context, null);
    }

    public ActivityContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return intercept;
    }

    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }
}
