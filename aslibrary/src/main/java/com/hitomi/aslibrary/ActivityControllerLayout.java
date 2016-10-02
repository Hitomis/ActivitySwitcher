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
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * 排列、展示 Activity 容器类
 *
 * Created by hitomi on 2016/10/11.
 */
class ActivityControllerLayout extends FrameLayout implements View.OnClickListener{

    public static final String TAG = "ActivitySwitcher";

    public static final int FLAG_DISPLAYING = 100;
    public static final int FLAG_DISPLAYED = 2000;
    public static final int FLAG_CLOSING = -100;
    public static final int FLAG_CLOSED = -200;

    private static final int STYLE_SINGLE = 1;
    private static final int STYLE_DOUBLE = 1 << 1;

    private static final int STYLE_MULTIPLE = 1 << 2;

    private static final float CENTER_SCALE_RATE = .65f;
    private static final float OFFSET_SCALE_RATE = .02f;

    private static final int MIN_OFFSET_SIZE = 80;
    private static final int MAX_OFFSET_SIZE = 180;

    private int flag;
    private int width;
    private float pageOffsetSize;

    private boolean resetBackground;

    private OnSelectedActivityCallback onSelectedActivityCallback;
    private View controView;

    public ActivityControllerLayout(Context context) {
        this(context, null);
    }

    public ActivityControllerLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityControllerLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        flag = FLAG_CLOSED;
        width = getResources().getDisplayMetrics().widthPixels;
    }

    @Override
    public void addView(View child) {
        child.setOnClickListener(this);
        super.addView(child);
    }

    @Override
    public void onClick(final View view) {
        if (flag == FLAG_DISPLAYED) {
            controView = view;
            closure();
        }
    }

    @NonNull
    private ObjectAnimator getCheckedScaleYAnima(View view) {
        ObjectAnimator chooseScaleYAnima = ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), 1.0f);
        chooseScaleYAnima.setDuration(200);
        chooseScaleYAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewGroup vgChild;
                RoundRectDrawableWithShadow drawable;
                if (valueAnimator.getAnimatedFraction() > .3f && !resetBackground) {
                    for (int i = 0; i < getChildCount(); i++) {
                        vgChild = (ViewGroup) getChildAt(i);
                        if (vgChild.getBackground() instanceof RoundRectDrawableWithShadow) {
                            drawable = (RoundRectDrawableWithShadow) vgChild.getBackground();
                            vgChild.setBackgroundColor(drawable.getBackgroundColor());
                        }
                    }
                    resetBackground = true;
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
            float afterEndTranX = width * .5f;
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
            float afterEndTranX = width - (chooseIndex + 2) * pageOffsetSize;
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

    private Animator displayBySingleStyle() {
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
        return scaleAnima;
    }

    private Animator displayByDoubleStyle() {
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

        float endTranX = width * (CENTER_SCALE_RATE + OFFSET_SCALE_RATE) / 2;
        ObjectAnimator tranXAnima = ObjectAnimator.ofFloat(aboveChild, "X", aboveChild.getX(), endTranX);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(200);
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.play(scaleAnima).with(tranXAnima);
        return animatorSet;
    }

    private Animator displayByMultipleStyle() {
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
                    initTranX = (width - width * (CENTER_SCALE_RATE + 3 * OFFSET_SCALE_RATE * (i - 1))) * .5f;
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
        return animatorSet;
    }

    private int getLayoutStyle() {
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

    private void updateContainerIntercept(boolean interceptEvent) {
        ActivityContainer container;
        for (int i = 0; i < getChildCount(); i++) {
            container = (ActivityContainer) getChildAt(i);
            container.setIntercept(interceptEvent);
        }
    }

    public void display(@NonNull OnSelectedActivityCallback callback) {
        onSelectedActivityCallback = callback;
        flag = FLAG_DISPLAYING;
        Animator animator;
        int childCount = getChildCount();
        if (childCount <=0) return ;
        if (childCount == 1) {
            animator = displayBySingleStyle();
        } else if (childCount == 2) {
            animator = displayByDoubleStyle();
        } else {
            pageOffsetSize = width * 1.f / (childCount + 1);
            pageOffsetSize = pageOffsetSize < MIN_OFFSET_SIZE ? pageOffsetSize : MIN_OFFSET_SIZE;
            pageOffsetSize = pageOffsetSize > MAX_OFFSET_SIZE ? MAX_OFFSET_SIZE : pageOffsetSize;
            animator = displayByMultipleStyle();
        }
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                updateContainerIntercept(true);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                flag = FLAG_DISPLAYED;
            }
        });
        animator.start();
    }

    public void closure() {
        controView = controView == null ? getChildAt(getChildCount() - 1) : controView;
        flag = FLAG_CLOSING;
        AnimatorSet animatorSet = null;
        resetBackground = false;
        switch (getLayoutStyle()) {
            case STYLE_SINGLE:
                animatorSet = singleStyleAnimator(controView);
                break;
            case STYLE_DOUBLE:
                animatorSet = doubleStyleAnimator(controView);
                break;
            case STYLE_MULTIPLE:
                animatorSet = multipleStyleAnimator(controView);
                break;
        }
        if (animatorSet == null) return ;
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onSelectedActivityCallback != null) {
                    updateContainerIntercept(false);
                    onSelectedActivityCallback.onSelected(controView);
                    controView.setOnClickListener(null);
                    controView = null;
                }
                flag = FLAG_CLOSED;
            }
        });
        animatorSet.start();
    }

    public int getFlag() {
        return flag;
    }

    public void log(String text) {
        Log.d(TAG, text);
    }

    public interface OnSelectedActivityCallback {
        void onSelected(View selectedChild);
    }
}
