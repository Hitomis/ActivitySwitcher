package com.hitomi.aslibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by hitomi on 2016/11/2.
 */
public class ActivityContainer extends FrameLayout {

    public ActivityContainer(Context context) {
        this(context, null);
    }

    public ActivityContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ActivityContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
