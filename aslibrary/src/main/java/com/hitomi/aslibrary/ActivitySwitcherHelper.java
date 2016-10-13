package com.hitomi.aslibrary;

import android.app.Activity;
import android.app.Application;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.List;

/**
 * Created by hitomi on 2016/10/11.
 */
class ActivitySwitcherHelper {

    private Context appContext;

    private ActivityManager actManager;

    private ActivityControllerLayout actControllerLayout;

    public ActivitySwitcherHelper(@NonNull Application application) {
        appContext = application;
        actManager = ActivityManager.getInstance();
        application.registerActivityLifecycleCallbacks(actManager);
        actControllerLayout = new ActivityControllerLayout(application);
    }

    public void startSwitch() {
        attachBlurBackground();
        List<Activity> preActivities = actManager.getPreActivies();
        Activity currAct = actManager.getCurrentActivity();
        preActivities.add(currAct);

        ViewGroup contentViewGroup, contentView;
        for (Activity activity : preActivities) {
            if (activity.getWindow() == null) continue;
            contentViewGroup = getContentView(activity.getWindow());
            contentView = (ViewGroup) contentViewGroup.getChildAt(0);
            contentViewGroup.removeView(contentView);
            actControllerLayout.addView(contentView);
        }

        FrameLayout currContentView = getContentView(currAct.getWindow());
        currContentView.addView(actControllerLayout);
        actControllerLayout.display();
    }

    /**
     * 抽取系统桌面背景图, 设置为高斯模糊效果 <br/>
     *
     * 高斯模糊性能优化，参考文章 ：<a href = "http://www.jianshu.com/p/7ae7dfe47a70"/>[here]
     */
    private void attachBlurBackground() {
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
        Bitmap blurBitmap = FastBlur.doBlur(scaledBitmap, blurRadius, true);
        ImageView imageView = new ImageView(appContext);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageBitmap(blurBitmap);
        actControllerLayout.setBackgroundDrawable(imageView.getDrawable());
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
        Activity currAct = actManager.getCurrentActivity();
        FrameLayout currContentView = getContentView(currAct.getWindow());
        actSize[0] = currContentView.getMeasuredWidth();
        actSize[1] = currContentView.getMeasuredHeight();
        return actSize;
    }

    private final FrameLayout getContentView(Window window) {
        if(window == null) return null;
        return (FrameLayout) window.findViewById(Window.ID_ANDROID_CONTENT);
    }

}
