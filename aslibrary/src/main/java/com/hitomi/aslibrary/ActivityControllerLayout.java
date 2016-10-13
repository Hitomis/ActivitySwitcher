package com.hitomi.aslibrary;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * Created by hitomi on 2016/10/11.
 */
class ActivityControllerLayout extends FrameLayout {

    public static final String TAG = "ActivityControllerLayout";

    private static final int STYLE_SINGLE = 1;
    private static final int STYLE_DOUBLE = 1 << 1;
    private static final int STYLE_MULTIPLE = 1 << 2;

    private float maxScaleValue;

    private float maxOffsetX;

    public ActivityControllerLayout(Context context) {
        this(context, null);
    }

    public ActivityControllerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityControllerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        int screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;

        maxScaleValue = .95f;
        maxOffsetX = screenWidth - 150;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        return super.dispatchTouchEvent(ev);
    }

    private void laoutBySingleStyle() {
        final float scaleRate = .75f;
        final View singleChild = getChildAt(0);
        ValueAnimator scaleAnima = ValueAnimator.ofFloat(1, 100);
        scaleAnima.setDuration(200);
        scaleAnima.setInterpolator(new DecelerateInterpolator());
        scaleAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                float scaleValue = 1 - (1 - scaleRate) * fraction;
                log(String.valueOf(scaleValue));
                singleChild.setScaleX(scaleValue);
                singleChild.setScaleY(scaleValue);
            }
        });
        scaleAnima.start();
    }

    private void layoutByDoubleStyle() {

    }

    private void layoutByMultipleStyle() {
        int childCount = getChildCount();
        View child;
        float scaleValue, offsetX;
        for (int i = 0; i < childCount; i++) {
            child = getChildAt(i);
            if (child instanceof ImageView) continue;
            scaleValue = maxScaleValue - (childCount - i - 1) * .08f;
            offsetX = i * 100;
            child.setScaleX(scaleValue);
            child.setScaleY(scaleValue);
            child.setX(offsetX);
            child.setAlpha(0);
        }

    }

    public void display() {
        int childCount = getChildCount();
        if (childCount <=0) return ;
        if (childCount == 1) {
            laoutBySingleStyle();
        } else if (childCount == 2) {
            layoutByDoubleStyle();
        } else {
            layoutByMultipleStyle();
        }
    }

    public void log(String text) {
        Log.d(TAG, text);
    }
}
