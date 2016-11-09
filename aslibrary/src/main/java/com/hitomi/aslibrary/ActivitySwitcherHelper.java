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
import java.util.ArrayList;
import java.util.List;

/**
 * ActivitySwitch 帮助类。负责：
 * <ul>
 *     <li>打开/关闭 ActivitySwitcher</li>
 *     <li>ActivitySwitcher 背景处理</li>
 *     <li>Activity关闭，程序结束进程处理</li>
 * </ul>
 *
 * email : 196425254@qq.com <br/>
 *
 * github : https://github.com/Hitomis <br/>
 *
 * Created by hitomi on 2016/10/11.
 */
class ActivitySwitcherHelper {

    private ActivitySwitcher actSwitcher;

    private Context appContext;

    private ActivityManager actManager;

    private ActivityControllerLayout actControllerLayout;

    private List<Activity> preActivities;
    private List<Activity> flingActivities;

    private ActivitySwitcher.OnActivitySwitchListener onActivitySwitchListener;

    private ActivityControllerLayout.OnControlCallback callback = new ActivityControllerLayout.OnControlCallback() {

        @Override
        public void onDisplayed() {
            if (onActivitySwitchListener != null)
                onActivitySwitchListener.onSwitchStarted();
        }

        @Override
        public void onSelected(ActivityContainer selectedContainer) {
            actSwitcher.setSwitching(false);
            endSwitch(actControllerLayout.indexOfChild(selectedContainer));
        }

        @Override
        public void onFling(ActivityContainer flingContainer) {
            int index = actControllerLayout.indexOfChild(flingContainer);
            Activity flingActivity = preActivities.get(index);
            actControllerLayout.removeView(flingContainer);
            preActivities.remove(flingActivity);
            flingActivities.add(flingActivity);

            if (preActivities.isEmpty()) {
                for (Activity flingAct : flingActivities) {
                    finishActivityByNoAnimation(flingAct);
                }
                // 必须先关闭所有 Activity 才能结束进程
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        }
    };

    public ActivitySwitcherHelper(ActivitySwitcher switcher, @NonNull Application application) {
        actSwitcher = switcher;
        appContext = application;
        actManager = ActivityManager.getInstance();
        application.registerActivityLifecycleCallbacks(actManager);
        actControllerLayout = new ActivityControllerLayout(application);
        flingActivities = new ArrayList<>();
        attachBlurBackground();
    }

    /**
     * Start the Activity switch
     */
    public void startSwitch() {
        // 获取当前 Activity 以及当前 Activity 之前所有 Activity
        preActivities = actManager.getPreActivies();
        Activity currAct = actManager.getCurrentActivity();
        preActivities.add(currAct);

        ViewGroup contentViewGroup, contentView;
        final int radius = 8;
        final int shadowSize = 12;
        int[] actSize = getActivitySize();
        Drawable background;
        ActivityContainer container;
        for (Activity activity : preActivities) {
            if (activity.getWindow() == null) continue;
            contentViewGroup = getContentView(activity.getWindow());
            contentView = (ViewGroup) contentViewGroup.getChildAt(0);
            contentViewGroup.removeView(contentView);
            container = new ActivityContainer(appContext);
            container.addView(contentView);
            // 如果 Activity 没有背景则使用 window 的背景
            background = contentView.getBackground() != null
                    ? contentView.getBackground()
                    : activity.getWindow().getDecorView().getBackground();
            if (background instanceof ColorDrawable) {
                ColorDrawable colorDrawable = (ColorDrawable) background;
                RoundRectDrawableWithShadow roundDrawable = new RoundRectDrawableWithShadow(
                        colorDrawable.getColor(), radius, shadowSize, shadowSize);
                // 设置背景
                container.setBackgroundDrawable(roundDrawable);
            }
            FrameLayout.LayoutParams contentViewLp = new FrameLayout.LayoutParams(actSize[0], actSize[1]);
            container.setLayoutParams(contentViewLp);
            // 将修改好背景/尺寸/布局的 Activity 中的 contentView 添加到 ActivityControllerLayout 中
            actControllerLayout.addView(container);
        }

        FrameLayout currContentView = getContentView(currAct.getWindow());
        currContentView.addView(actControllerLayout);
        actControllerLayout.display(callback);
    }

    public void endSwitch() {
        actControllerLayout.closure(true);
    }

    public boolean isActivityControllerClosed() {
        return actControllerLayout.getFlag() == ActivityControllerLayout.FLAG_CLOSED;
    }

    public boolean isActivityControllerDisplayed() {
        return actControllerLayout.getFlag() == ActivityControllerLayout.FLAG_DISPLAYED;
    }

    public void exit() {
        if (preActivities == null) {
            preActivities = actManager.getPreActivies();
        } else {
            preActivities.addAll(flingActivities);
        }
        for (Activity activity : preActivities) {
            finishActivityByNoAnimation(activity);
        }
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * End of the Activity switch
     * @param selectedIndex
     */
    private void endSwitch(int selectedIndex) {
        // 从栈顶 Activity 的 ContentView 中移除 ActivityControllerLayout
        FrameLayout topContentViewGroup = getContentView(actManager.getCurrentActivity().getWindow());
        topContentViewGroup.removeView(actControllerLayout);

        // 关闭当前选中的 Activity 之后的 Activity 和被 fling 掉的 Activity
        Activity activity;
        View contentView;
        for (int i = preActivities.size() - 1; i > selectedIndex; i--) {
            activity = preActivities.get(i);
            finishActivityByNoAnimation(activity);
        }
        for (Activity act : flingActivities) {
            finishActivityByNoAnimation(act);
        }

        // 将 ActivityControllerLayout 中的每个 ContentView 还原给 Activity
        FrameLayout contentViewGroup;
        FrameLayout.LayoutParams contentViewLp;
        ActivityContainer activityContainer;
        for (int i = selectedIndex; i >= 0; i--) {
            activityContainer = (ActivityContainer) actControllerLayout.getChildAt(i);
            contentView = activityContainer.getChildAt(0);
            activityContainer.removeView(contentView);
            actControllerLayout.removeView(activityContainer);

            activity = preActivities.get(i);
            contentViewGroup = getContentView(activity.getWindow());
            contentViewLp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            contentViewGroup.addView(contentView, contentViewLp);
        }
        actControllerLayout.removeAllViews();

        if (onActivitySwitchListener != null)
            onActivitySwitchListener.onSwitchFinished(preActivities.get(selectedIndex));
    }

    /**
     * 无任何痕迹实现 Activity 关闭
     * @param activity
     */
    private void finishActivityByNoAnimation(Activity activity) {
        Window window = activity.getWindow();
        window.getDecorView().setAlpha(0);
        activity.finish();
        activity.overridePendingTransition(0, 0);
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

    public void setOnActivitySwitchListener(ActivitySwitcher.OnActivitySwitchListener listener) {
        this.onActivitySwitchListener = listener;
    }
}
