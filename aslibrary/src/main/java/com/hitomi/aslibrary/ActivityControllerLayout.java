package com.hitomi.aslibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

/**
 * 启用 ActivitySwitcher 候的 Activity 容器类
 *
 * TODO: 应该抽象出一个接口，Activity 的容器可以有很多种。用来展示出不同风格
 *
 * Created by hitomi on 2016/10/11.
 */
class ActivityControllerLayout extends FrameLayout implements View.OnClickListener{

    public static final String TAG = "ActivitySwitcher";

    private static final int STYLE_SINGLE = 1;
    private static final int STYLE_DOUBLE = 1 << 1;
    private static final int STYLE_MULTIPLE = 1 << 2;

    private static final float DELAY_RATE = 3f;

    private static final float CENTER_SCALE_RATE = .75f;

    private static final float OFFSET_SCALE_RATE = .02f;

    private OnSelectedActivityCallback onSelectedActivityCallback;

    private View controChild;

    private float maxScaleValue;

    private float maxOffsetX, preX, preY;

    private float touchSlopX, getTouchSlopY;

    private int screenWidth;

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
        ViewConfiguration conf = ViewConfiguration.get(getContext());
        touchSlopX = getTouchSlopY = conf.getScaledTouchSlop() / 2;
        screenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        maxOffsetX = screenWidth - 150;
        maxScaleValue = .95f;
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        child.setOnClickListener(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        controChild = pressWhichChild(ev);
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                switch (getLayoutStyle()) {
                    case STYLE_SINGLE:
                        float moveY = (ev.getY() - preY) / DELAY_RATE;
                        controChild.setY(controChild.getY() + moveY);
                        preY = ev.getY();
                        break;
                    case STYLE_DOUBLE:
                        break;
                    case STYLE_MULTIPLE:
                        break;
                }
                break;
            case MotionEvent.ACTION_UP:
                switch (getLayoutStyle()) {
                    case STYLE_SINGLE:
                        if (controChild.getY() != 0) {
                            rollback(controChild);
                        }
                        break;
                    case STYLE_DOUBLE:
                        break;
                    case STYLE_MULTIPLE:
                        break;
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void rollback(View controChild) {
        ObjectAnimator tranYAnimator = ObjectAnimator.ofFloat(controChild, "y", controChild.getY(), 0);
        tranYAnimator.setDuration(200);
        tranYAnimator.start();
    }

    private View pressWhichChild(MotionEvent ev) {
        if (getLayoutStyle() == STYLE_SINGLE) return getChildAt(0);
        View clickChild = null;
        return clickChild;
    }

    @Override
    public void onClick(View view) {
        if (Math.abs(view.getX()) > touchSlopX || Math.abs(view.getY()) > getTouchSlopY) return;
        switch (getLayoutStyle()) {
            case STYLE_SINGLE:
                displayBySingleStyle(true);
                break;
            case STYLE_DOUBLE:
                break;
            case STYLE_MULTIPLE:
                break;
        }
    }

    private void displayBySingleStyle(boolean reverse) {
        final View singleChild = getChildAt(0);
        ValueAnimator scaleAnima = ValueAnimator.ofFloat(1, 100);
        scaleAnima.setDuration(200);
        scaleAnima.setInterpolator(new DecelerateInterpolator());
        scaleAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                float scaleValue = 1 - (1 - CENTER_SCALE_RATE) * fraction;
                singleChild.setScaleX(scaleValue);
                singleChild.setScaleY(scaleValue);
            }
        });
        if (reverse) {
            scaleAnima.reverse();
            scaleAnima.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    onSelectedActivityCallback.onSelected(controChild);
                }
            });
        } else {
            scaleAnima.start();
        }
    }

    private void displayByDoubleStyle() {
        final View belowChild = getChildAt(0);
        final View aboveChild = getChildAt(1);
        ValueAnimator scaleAnima = ValueAnimator.ofFloat(1, 100);
        scaleAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                float scaleValue = 1 - (1 - CENTER_SCALE_RATE) * fraction;
                belowChild.setScaleX(scaleValue);
                belowChild.setScaleY(scaleValue);

                scaleValue = 1 - (1 - (CENTER_SCALE_RATE + OFFSET_SCALE_RATE)) * fraction;
                aboveChild.setScaleX(scaleValue);
                aboveChild.setScaleY(scaleValue);
            }
        });

        float endTranX = aboveChild.getWidth() * (CENTER_SCALE_RATE + OFFSET_SCALE_RATE) / 2;
        ObjectAnimator tranXAnima = ObjectAnimator.ofFloat(aboveChild, "X", aboveChild.getX(), endTranX);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(scaleAnima).with(tranXAnima);
        animatorSet.start();
    }

    private void displayByMultipleStyle() {
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

    public void display(@NonNull OnSelectedActivityCallback callback) {
        onSelectedActivityCallback = callback;
        int childCount = getChildCount();
        if (childCount <=0) return ;
        if (childCount == 1) {
            displayBySingleStyle(false);
        } else if (childCount == 2) {
            displayByDoubleStyle();
        } else {
            displayByMultipleStyle();
        }
    }

    public int getLayoutStyle() {
        int style = 0;
        int childCount = getChildCount();
        if (childCount == 1) {
            style = STYLE_SINGLE;
        } else if (childCount == 2) {
            style = STYLE_DOUBLE;
        } else if (childCount >=3) {
            style = STYLE_MULTIPLE;
        }
        return style;
    }

    public void log(String text) {
        Log.d(TAG, text);
    }

    public interface OnSelectedActivityCallback {
        void onSelected(View selectedChild);
    }
}
