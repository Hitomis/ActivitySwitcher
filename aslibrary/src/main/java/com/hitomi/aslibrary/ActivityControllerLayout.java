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
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * 排列、展示、拖动 Activity 容器类 <br/>
 *
 * email : 196425254@qq.com <br/>
 *
 * github : https://github.com/Hitomis <br/>
 *
 * Created by hitomi on 2016/10/11.
 */
class ActivityControllerLayout extends FrameLayout implements View.OnClickListener{
    public static final String TAG = "ActivitySwitcher";

    public static final int FLAG_DISPLAYING = 100;
    public static final int FLAG_DISPLAYED = 200;
    public static final int FLAG_CLOSING = -100;
    public static final int FLAG_CLOSED = -200;
    public static final int FLAG_SLIDING = 0;

    private static final int STYLE_SINGLE = 1;
    private static final int STYLE_DOUBLE = 1 << 1;

    private static final int STYLE_MULTIPLE = 1 << 2;

    private static final float CENTER_SCALE_RATE = .65f;
    private static final float OFFSET_SCALE_RATE = .02f;

    private static final int MIN_OFFSET_SIZE = 80;
    private static final int MAX_OFFSET_SIZE = 180;

    private int maxVelocity = 2500;
    private int touchSlop = 8;

    private int flag;
    private int width;

    private float pageOffsetSize;
    private float preY, diffY;
    private float controlViewBottom = 0.f;

    private float[] originalContainerX;
    private float[] originalContainerScale;

    private boolean resetBackground;

    private VelocityTracker velocityTracker;

    private OnControlCallback onControlCallback;
    private ActivityContainer controlView;

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
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (flag == FLAG_DISPLAYED || flag == FLAG_SLIDING)
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (null == velocityTracker) {
                    velocityTracker = VelocityTracker.obtain();
                } else {
                    velocityTracker.clear();
                }
                velocityTracker.addMovement(ev);

                if (findControlView(ev) == null || flag == FLAG_SLIDING) return false;
                cacheOrginalContainerParamter(controlView = findControlView(ev));

                preY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                velocityTracker.addMovement(ev);

                diffY = ev.getY() - preY;
                float newDiffY = diffY;
                if (controlView.getY() <= 0) { // 在中线之上
                    moveToLastContainerPos();
                } else if (controlView.getY() > 0 && diffY > 0) { // 在中线之下
                    float slopRate = 1.f - 1.65f * controlView.getY() / (controlView.getIntrinsicHeight());
                    newDiffY *= slopRate;
                }
                controlView.setY(controlView.getY() + newDiffY);

                preY = ev.getY();
                break;
            case MotionEvent.ACTION_UP:
                velocityTracker.addMovement(ev);
                velocityTracker.computeCurrentVelocity(1000);
                float velocityY = velocityTracker.getYVelocity();

                boolean over = Math.abs(controlView.getY()) >= controlView.getIntrinsicHeight() * .618;
                if (diffY < touchSlop * .4f && controlView.getY() < 0 && (over || Math.abs(velocityY) >= maxVelocity)) {
                    // 上移且超出阈值 或者 上移速度超过阈值 -> 移除到窗外
                    slideOutAnimation();
                } else if (flag != FLAG_CLOSING && Math.abs(controlView.getY()) >= touchSlop) {
                    // 下移或者上移没有超出阈值- > 回落到原始位置
                    slideOrignalPosAnimation();
                }

                diffY = 0;

                if (null != velocityTracker) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (null != velocityTracker) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(final View view) {
        if (flag == FLAG_DISPLAYED
                && Math.abs(diffY) < touchSlop
                && Math.abs(view.getY()) < touchSlop) {
            closure(false);
        }
    }

    private void cacheOrginalContainerParamter(ActivityContainer controlContainer) {
        controlViewBottom = controlContainer.getBounds().bottom;
        int controlIndex = indexOfChild(controlContainer);
        if (getChildCount() != 3) {
            originalContainerX = new float[getChildCount()];
            originalContainerScale = new float[getChildCount()];
            View child;
            for (int i = 0; i < getChildCount(); i++) {
                child = getChildAt(i);
                originalContainerX[i] = child.getX();
                originalContainerScale[i] = child.getScaleX();
            }
        } else {
            originalContainerX = new float[2];
            originalContainerScale = new float[2];
            if (controlIndex == 0) {
                originalContainerX[0] = getChildAt(1).getX();
                originalContainerX[1] = getChildAt(2).getX();

                originalContainerScale[0] = getChildAt(1).getScaleX();
                originalContainerScale[1] = getChildAt(2).getScaleX();
            } else if (controlIndex == 1) {
                originalContainerX[0] = getChildAt(0).getX();
                originalContainerX[1] = getChildAt(2).getX();

                originalContainerScale[0] = getChildAt(0).getScaleX();
                originalContainerScale[1] = getChildAt(2).getScaleX();
            } else {
                originalContainerX[0] = getChildAt(0).getX();
                originalContainerX[1] = getChildAt(1).getX();

                originalContainerScale[0] = getChildAt(0).getScaleX();
                originalContainerScale[1] = getChildAt(1).getScaleX();
            }

        }
    }

    private void slideOrignalPosAnimation() {
        ObjectAnimator tranYAnima = ObjectAnimator.ofFloat(controlView, "Y", controlView.getY(), 0);
        tranYAnima.setDuration(350);
        tranYAnima.setInterpolator(new DecelerateInterpolator());
        tranYAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (controlView.getY() < 0)
                    moveToLastContainerPos();
            }
        });
        tranYAnima.start();
    }

    private void slideOutAnimation() {
        flag = FLAG_SLIDING;
        float endTranY = controlView.getY() - controlView.getBounds().bottom;
        ObjectAnimator tranYAnima = ObjectAnimator.ofFloat(controlView, "Y", controlView.getY(), endTranY);
        tranYAnima.setDuration(150);
        tranYAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                moveToLastContainerPos();
            }
        });
        tranYAnima.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onControlCallback != null) {
                    onControlCallback.onFling(controlView);
                }
                flag = FLAG_DISPLAYED;
            }
        });
        tranYAnima.start();
    }

    private void moveToLastContainerPos() {
        int controlIndex = indexOfChild(controlView);
        if (getChildCount() == 3) {
            View belowChild, aboveChild;
            float totalOffset, currOffset;
            if (controlIndex == 0) {
                belowChild = getChildAt(1);
                aboveChild = getChildAt(2);
            } else if (controlIndex == 1) {
                belowChild = getChildAt(0);
                aboveChild = getChildAt(2);
            } else {
                belowChild = getChildAt(0);
                aboveChild = getChildAt(1);
            }

            totalOffset = originalContainerX[0];
            currOffset = calcOffsetSize(totalOffset);
            belowChild.setX(originalContainerX[0] + currOffset);

            totalOffset = CENTER_SCALE_RATE - originalContainerScale[0];
            currOffset = calcOffsetSize(totalOffset);
            belowChild.setScaleX(originalContainerScale[0] - currOffset);
            belowChild.setScaleY(originalContainerScale[0] - currOffset);

            totalOffset = width * (CENTER_SCALE_RATE + OFFSET_SCALE_RATE) / 2 - originalContainerX[1];
            currOffset = calcOffsetSize(totalOffset);
            aboveChild.setX(originalContainerX[1] - currOffset);

            totalOffset = CENTER_SCALE_RATE + OFFSET_SCALE_RATE - originalContainerScale[1];
            currOffset = calcOffsetSize(totalOffset);
            aboveChild.setScaleX(originalContainerScale[1] - currOffset);
            aboveChild.setScaleY(originalContainerScale[1] - currOffset);
        } else if (getChildCount() != 1 && getChildCount() != controlIndex + 1) {
            float currOffsetX, currScaleSize;
            float totalOffsetX = originalContainerX[controlIndex + 1] - originalContainerX[controlIndex];
            float totalScaleSize = getLayoutStyle() == STYLE_DOUBLE
                    ? OFFSET_SCALE_RATE : 3 * OFFSET_SCALE_RATE;

            View child;
            for (int i = controlIndex + 1; i < getChildCount(); i++) {
                if (controlViewBottom == 0.f) continue;
                child = getChildAt(i);

                currOffsetX = calcOffsetSize(totalOffsetX);
                child.setX(originalContainerX[i] + currOffsetX);

                currScaleSize = calcOffsetSize(totalScaleSize);
                child.setScaleX(originalContainerScale[i] + currScaleSize);
                child.setScaleY(originalContainerScale[i] + currScaleSize);
            }
        }
    }

    private float calcOffsetSize(float totalSize) {
        return controlView.getY() * totalSize / controlViewBottom;
    }


    private ActivityContainer findControlView(MotionEvent ev) {
        int childCount = getChildCount();
        ActivityContainer controlView = null;
        ActivityContainer container;
        for (int i = childCount - 1; i >= 0; i--) {
            container = (ActivityContainer) getChildAt(i);
            if (container.getBounds().contains(ev.getX(), ev.getY())) {
                controlView = container;
                break;
            }
        }
        return controlView;
    }

    @NonNull
    private ObjectAnimator getCheckedScaleXAnima(View view) {
        ObjectAnimator chooseScaleXAnima = ObjectAnimator.ofFloat(view, "scaleX", view.getScaleX(), 1.0f);
        chooseScaleXAnima.setDuration(200);
        return chooseScaleXAnima;
    }

    @NonNull
    private ObjectAnimator getCheckedScaleYAnima(View view) {
        ObjectAnimator chooseScaleYAnima = ObjectAnimator.ofFloat(view, "scaleY", view.getScaleY(), 1.0f);
        chooseScaleYAnima.setDuration(200);
        chooseScaleYAnima.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                removeShadow(valueAnimator);
            }
        });
        return chooseScaleYAnima;
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

    private void removeShadow(ValueAnimator valueAnimator) {
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

    public void display(@NonNull OnControlCallback callback) {
        onControlCallback = callback;
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
            pageOffsetSize = pageOffsetSize < MIN_OFFSET_SIZE ? MIN_OFFSET_SIZE : pageOffsetSize;
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
                if (onControlCallback != null)
                    onControlCallback.onDisplayed();
            }
        });
        animator.start();
    }

    public void closure(boolean back) {
        controlView = controlView == null || indexOfChild(controlView) == -1 || back
                ? (ActivityContainer) getChildAt(getChildCount() - 1)
                : controlView;
        flag = FLAG_CLOSING;
        AnimatorSet animatorSet = null;
        resetBackground = false;
        switch (getLayoutStyle()) {
            case STYLE_SINGLE:
                animatorSet = singleStyleAnimator(controlView);
                break;
            case STYLE_DOUBLE:
                animatorSet = doubleStyleAnimator(controlView);
                break;
            case STYLE_MULTIPLE:
                animatorSet = multipleStyleAnimator(controlView);
                break;
        }
        if (animatorSet == null) return ;
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onControlCallback != null)
                    onControlCallback.onSelected(controlView);

                updateContainerIntercept(false);
                controlView.setOnClickListener(null);
                controlView = null;
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

    public interface OnControlCallback {

        void onDisplayed();

        void onSelected(ActivityContainer selectedContainer);

        void onFling(ActivityContainer flingContainer);

    }
}
