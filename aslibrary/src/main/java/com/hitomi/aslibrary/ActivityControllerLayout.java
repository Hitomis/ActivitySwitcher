package com.hitomi.aslibrary;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.util.Map;

/**
 * 排列、展示 Activity 容器类
 *
 * Created by hitomi on 2016/10/11.
 */
class ActivityControllerLayout extends FrameLayout implements View.OnClickListener{

    public static final String TAG = "ActivitySwitcher";

    private static final int STYLE_SINGLE = 1;
    private static final int STYLE_DOUBLE = 1 << 1;
    private static final int STYLE_MULTIPLE = 1 << 2;

    private static final float CENTER_SCALE_RATE = .65f;
    private static final float OFFSET_SCALE_RATE = .02f;

    private static final int MIN_OFFSET_SIZE = 80;
    private static final int MAX_OFFSET_SIZE = 180;

    private float pageOffsetSize;
    private int screenWidth;

    private OnSelectedActivityCallback onSelectedActivityCallback;
    private Map<View, Drawable> backgroundMap;

    public ActivityControllerLayout(Context context) {
        this(context, null);
    }

    public ActivityControllerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityControllerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        screenWidth = getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    public void addView(View child) {
        child.setOnClickListener(this);
        super.addView(child);
    }

    @Override
    public void onClick(final View view) {
        AnimatorSet animatorSet = null;
        switch (getLayoutStyle()) {
            case STYLE_SINGLE:
                animatorSet = singleStyleAnimator(view);
                break;
            case STYLE_DOUBLE:
                animatorSet = doubleStyleAnimator(view);
                break;
            case STYLE_MULTIPLE:
                animatorSet = multipleStyleAnimator(view);
                break;
        }
        if (animatorSet == null) return ;
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onSelectedActivityCallback != null)
                    onSelectedActivityCallback.onSelected(view);
            }
        });
        animatorSet.start();
    }

    @NonNull
    private ObjectAnimator getCheckedScaleYAnima(View view) {
        ObjectAnimator chooseScaleYAnima = ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), 1.0f);
        chooseScaleYAnima.setDuration(200);
        chooseScaleYAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                View child;
                if (valueAnimator.getAnimatedFraction() > .5f) {
                    for (int i = 0; i < getChildCount(); i++) {
                        child = getChildAt(i);
                        child.setBackgroundDrawable(backgroundMap.get(child));
                    }
                }
            }
        });
        return chooseScaleYAnima;
    }

    @NonNull
    private ObjectAnimator getCheckedScaleXAnima(View view) {
        ObjectAnimator chooseScaleXAnima = ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), 1.0f);
        chooseScaleXAnima.setDuration(200);
        return chooseScaleXAnima;
    }

    @NonNull
    private AnimatorSet singleStyleAnimator(View view) {
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(getCheckedScaleXAnima(view))
                .with(getCheckedScaleYAnima(view));
        return animatorSet;
    }

    private AnimatorSet doubleStyleAnimator(View view) {
        ObjectAnimator preObjAnima;
        if (indexOfChild(view) == 0) {
            float afterEndTranX = getWidth() * .5f;
            View afterChild = getChildAt(1);
            preObjAnima = ObjectAnimator.ofFloat(afterChild, "X",
                    afterChild.getX(), afterChild.getX() + afterEndTranX);
            preObjAnima.setDuration(300);
        } else {
            preObjAnima = ObjectAnimator.ofFloat(view, "X", view.getX(), 0);
            preObjAnima.setDuration(200);
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(preObjAnima)
                .before(getCheckedScaleXAnima(view))
                .before(getCheckedScaleYAnima(view));
        return animatorSet;
    }

    private AnimatorSet multipleStyleAnimator(final View view) {
        final int chooseIndex = indexOfChild(view);
        ValueAnimator afterTranXAnima = null;
        if (chooseIndex < getChildCount() - 1) {
            float afterEndTranX = getWidth() - (chooseIndex + 2) * pageOffsetSize;
            final float[] currX = new float[getChildCount() - chooseIndex -1];
            for (int i = chooseIndex + 1; i < getChildCount(); i++) {
                currX[i - chooseIndex - 1] = getChildAt(i).getX();
            }
            afterTranXAnima = ValueAnimator.ofFloat(0, afterEndTranX);
            afterTranXAnima.setDuration(300);
            afterTranXAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float valueX = Float.parseFloat(valueAnimator.getAnimatedValue().toString());
                    View afterChild;
                    for (int i = chooseIndex + 1; i < getChildCount(); i++) {
                        afterChild = getChildAt(i);
                        afterChild.setX(currX[i - chooseIndex - 1] + valueX);
                    }
                }
            });
        }
        ObjectAnimator chooseTranXAnima = ObjectAnimator.ofFloat(view, "X", view.getX(), 0);
        chooseTranXAnima.setDuration(200);
        AnimatorSet animatorSet = new AnimatorSet();
        AnimatorSet.Builder animaBuilder = animatorSet
                .play(chooseTranXAnima)
                .before(getCheckedScaleXAnima(view))
                .before(getCheckedScaleYAnima(view));
        if (afterTranXAnima != null) {
            animaBuilder.after(afterTranXAnima);
        }
        return animatorSet;
    }

    private void displayBySingleStyle() {
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
        scaleAnima.start();
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
        ValueAnimator scaleAnima = ValueAnimator.ofFloat(1, 100);
        scaleAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                float scaleValue;
                int childCount = getChildCount();
                View child;
                for (int i = 0; i <  childCount; i++) {
                    child = getChildAt(i);
                    scaleValue = CENTER_SCALE_RATE + 3 * OFFSET_SCALE_RATE * (i - 1);
                    scaleValue = 1 - (1 - scaleValue) * fraction;
                    child.setScaleX(scaleValue);
                    child.setScaleY(scaleValue);
                }
            }
        });


        ValueAnimator tranXAnima = ValueAnimator.ofFloat(1, 100);
        tranXAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                int childCount = getChildCount();
                float tranX;
                View child;
                float initTranX;
                for (int i = 0; i < childCount; i++) {
                    child = getChildAt(i);
                    initTranX = (child.getWidth() - child.getWidth() * (CENTER_SCALE_RATE + 3 * OFFSET_SCALE_RATE * (i - 1))) * .5f;
                    tranX = pageOffsetSize * i;
                    tranX = fraction * tranX - initTranX + pageOffsetSize;
                    child.setX(tranX);
                }
            }
        });
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(scaleAnima).with(tranXAnima);
        animatorSet.start();
    }


    public void display(@NonNull OnSelectedActivityCallback callback, Map<View, Drawable> drawableMap) {
        onSelectedActivityCallback = callback;
        backgroundMap = drawableMap;
        int childCount = getChildCount();
        if (childCount <=0) return ;
        if (childCount == 1) {
            displayBySingleStyle();
        } else if (childCount == 2) {
            displayByDoubleStyle();
        } else {
            pageOffsetSize = screenWidth * 1.f / (childCount + 1);
            pageOffsetSize = pageOffsetSize < MIN_OFFSET_SIZE ? pageOffsetSize : MIN_OFFSET_SIZE;
            pageOffsetSize = pageOffsetSize > MAX_OFFSET_SIZE ? MAX_OFFSET_SIZE : pageOffsetSize;
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
