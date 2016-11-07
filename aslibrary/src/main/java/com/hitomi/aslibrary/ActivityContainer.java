package com.hitomi.aslibrary;

import android.content.Context;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * 用于 Activity 实际大小、位置以及事件分发处理 <br/>
 *
 * email : 196425254@qq.com <br/>
 *
 * github : https://github.com/Hitomis <br/>
 *
 * Created by hitomi on 2016/11/2.
 */
public class ActivityContainer extends FrameLayout {

    private RectF bounds;

    private float offsetX, offsetY;
    private float tranX, tranY;

    private boolean intercept;

    public ActivityContainer(Context context) {
        this(context, null);
    }

    public ActivityContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bounds = new RectF();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return intercept;
    }

    @Override
    public void setLayoutParams(ViewGroup.LayoutParams params) {
        super.setLayoutParams(params);
        bounds.left = 0;
        bounds.top = 0;
        bounds.right = params.width;
        bounds.bottom = params.height;
    }

    @Override
    public void setScaleX(float scaleX) {
        super.setScaleX(scaleX);
        int width = getLayoutParams().width;
        float halfScaleX = width * (1 - scaleX) * .5f;
        bounds.left += halfScaleX - offsetX;
        bounds.right -= halfScaleX - offsetX;
        offsetX = halfScaleX;
    }

    @Override
    public void setScaleY(float scaleY) {
        super.setScaleY(scaleY);
        int height = getLayoutParams().height;
        float halfScaleY = height * (1 - scaleY) * .5f;
        bounds.top += halfScaleY - offsetY;
        bounds.bottom -= halfScaleY - offsetY;
        offsetY = halfScaleY;
    }

    @Override
    public void setTranslationX(float translationX) {
        super.setTranslationX(translationX);
        bounds.left += translationX - tranX;
        bounds.right += translationX - tranX;
        tranX = translationX;
    }

    @Override
    public void setTranslationY(float translationY) {
        super.setTranslationY(translationY);
        bounds.top += translationY - tranY;
        bounds.bottom += translationY - tranY;
        tranY = translationY;
    }

    @Override
    public void setX(float x) {
        super.setX(x);
        bounds.left += x - tranX;
        bounds.right += x - tranX;
        tranX = x;
    }

    @Override
    public void setY(float y) {
        super.setY(y);
        bounds.top += y - tranY;
        bounds.bottom += y - tranY;
        tranY = y;
    }

    public void setIntercept(boolean intercept) {
        this.intercept = intercept;
    }

    public RectF getBounds() {
        return bounds;
    }

    public float getIntrinsicHeight() {
        return bounds.bottom - bounds.top;
    }

    public float getIntrinsicWidth() {
        return bounds.right - bounds.left;
    }
}
