package com.hitomi.aslibrary;

import android.app.Activity;
import android.app.Application;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by hitomi on 2016/10/11.
 */
class ActivitySwitcherHelper {

    private ActivitySwitcher actSwitcher;

    private Context appContext;

    private ActivityManager actManager;

    private ActivityControllerLayout actControllerLayout;

    private List<Activity> preActivities;

    public ActivitySwitcherHelper(ActivitySwitcher switcher, @NonNull Application application) {
        actSwitcher = switcher;
        appContext = application;
        actManager = ActivityManager.getInstance();
        application.registerActivityLifecycleCallbacks(actManager);
        actControllerLayout = new ActivityControllerLayout(application);
        attachBlurBackground();
    }

    public void startSwitch() {
        preActivities = actManager.getPreActivies();
        Activity currAct = actManager.getCurrentActivity();
        preActivities.add(currAct);

        ViewGroup contentViewGroup, contentView;
        final int radius = 8;
        final int shadowSize = 8;
        for (Activity activity : preActivities) {
            if (activity.getWindow() == null) continue;
            contentViewGroup = getContentView(activity.getWindow());
            contentView = (ViewGroup) contentViewGroup.getChildAt(0);
            contentViewGroup.removeView(contentView);
            if (contentView.getBackground() instanceof ColorDrawable) {
                ColorDrawable colorDrawable = (ColorDrawable) contentView.getBackground();
                RoundRectDrawableWithShadow roundDrawable = new RoundRectDrawableWithShadow(
                        colorDrawable.getColor(), radius, shadowSize, shadowSize);
                contentView.setBackgroundDrawable(roundDrawable);
            }
            actControllerLayout.addView(contentView);
        }

        FrameLayout currContentView = getContentView(currAct.getWindow());
        currContentView.addView(actControllerLayout);
        actControllerLayout.display(new ActivityControllerLayout.OnSelectedActivityCallback() {
            @Override
            public void onSelected(View view) {
                actSwitcher.setSwitching(false);
                endSwitch(view);
            }
        });
    }

    public void endSwitch(View selectedChild) {
        int index = actControllerLayout.indexOfChild(selectedChild);
        Activity selectedAct = preActivities.get(index);
        if (actManager.getCurrentActivity() == selectedAct) {
            FrameLayout currContentView = getContentView(selectedAct.getWindow());
            actControllerLayout.removeView(selectedChild);
            currContentView.removeView(actControllerLayout);
            currContentView.addView(selectedChild);
        } else {

        }
    }

    /**
     * 附加高斯模糊图片背景
     */
    private void attachBlurBackground() {
        if (actControllerLayout.getBackground() != null) return;
        setContainerBackground();
    }

    /**
     * 抽取系统桌面背景图, 设置为高斯模糊效果 <br/>
     *
     * 高斯模糊性能优化，参考文章 ：<a href = "http://www.jianshu.com/p/7ae7dfe47a70"/>[here]
     */
    private void setContainerBackground() {
        new AsyncTask<Void, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Void... voids) {
                // 获取系统桌面背景图片
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(appContext);
                Drawable wallpaperDrawable = wallpaperManager.getDrawable();
                Bitmap wallpaperBitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
                // 以当前 Activity 尺寸为参照物，居中裁剪
                Bitmap centerBitmap = cropCenterBitmap(wallpaperBitmap);
                final int scaleRatio = 20;
                final int blurRadius = 8;
                // filter是指缩放的效果，filter为true则会得到一个边缘平滑的bitmap，反之，则会得到边缘锯齿、pixelrelated的bitmap。
                // 这里我们要对缩放的图片进行虚化，所以无所谓边缘效果，filter=false。
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(centerBitmap,
                        centerBitmap.getWidth() / scaleRatio,
                        centerBitmap.getHeight() / scaleRatio,
                        false);
                // 返回高斯模糊渲染后的图片
                return FastBlur.doBlur(scaledBitmap, blurRadius, true);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                ImageView imageView = new ImageView(appContext);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setImageBitmap(bitmap);
                actControllerLayout.setBackgroundDrawable(imageView.getDrawable());
            }
        }.execute();
    }

    /**
     * 以当前 Activity 尺寸为参照物，居中裁剪 Bitmap
     * @param bitmap
     * @return
     */
    private Bitmap cropCenterBitmap(Bitmap bitmap) {
        Bitmap resultBitmap = bitmap;
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        int[] actSize = getActivitySize();
        int x = 0, y = 0;
        if (srcWidth <= actSize[0] && srcHeight <= actSize[1]){
            // 背景图的宽高都小于当前 Activity 宽高 —> 放大到 Activity 宽高
            resultBitmap = Bitmap.createScaledBitmap(bitmap, actSize[0], actSize[1], true);
        } else if (srcWidth <= actSize[0] && srcHeight > actSize[1]) {
            // 背景图的宽小于当前 Activity 宽, 高大于当前 Activity 的高 -> 截取高
            y = (srcHeight - actSize[1]) / 2;
            resultBitmap = Bitmap.createBitmap(bitmap, x, y, srcWidth, actSize[1]);
        } else if (srcHeight <= actSize[1] && srcWidth > actSize[0]) {
            // 背景图的高小于当前 Activity 高, 宽大于当前 Activity 的宽 -> 截取宽
            x = (srcWidth - actSize[0]) / 2;
            resultBitmap = Bitmap.createBitmap(bitmap, x, y, actSize[0], srcHeight);
        } else if (srcWidth > actSize[0] && srcHeight > actSize[1]) {
            // 背景图的宽高均大于当前 Activity 宽高 -> 截取宽高
            x = (srcWidth - actSize[0]) / 2;
            y = (srcHeight - actSize[1]) / 2;
            resultBitmap = Bitmap.createBitmap(bitmap, x, y, actSize[0], actSize[1]);
        }
        return resultBitmap;
    }

    /**
     * 获取当前 Activity 在窗口中显示的尺寸大小
     * @return
     */
    private int[] getActivitySize() {
        int[] actSize = new int[2];
        DisplayMetrics displayMetrics = appContext.getResources().getDisplayMetrics();
        actSize[0] = displayMetrics.widthPixels;
        actSize[1] = displayMetrics.heightPixels - getStatusHeight();
        return actSize;
    }

    /**
     * 获取状态栏高度
     * @return
     */
    private int getStatusHeight() {
        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object object = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = (Integer) field.get(object);
            return appContext.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            return 0;
        }
    }

    private final FrameLayout getContentView(Window window) {
        if(window == null) return null;
        return (FrameLayout) window.findViewById(Window.ID_ANDROID_CONTENT);
    }

}
