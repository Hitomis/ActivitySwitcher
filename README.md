# ActivitySwitcher

   ActivitySwitcher 是一个基于 Activity 视图操作管理库，可以实现 Activity 之间任意跳转、关闭任意一个 Activity
以及结束应用程序等功能。
   
   本库中的展现 Activity 视图时，附带阴影的卡片效果抽取自 (CrazyShadow)[https://github.com/Hitomis/CrazyShadow] 有兴趣的朋友可以移步看看。
   
   欢迎大家给 ActivitySwitcher 提 Issues，有问题我会尽快修复

# Preview

录制图像有丢帧的情况，所以预览图效果不够流畅，背景图显示的也有问题。
<img src="preview/activity_swither.gif"/>

# Import

导入 aslibrary Module 作为依赖库, 或者直接复制 com.hitomi.aslibrary 中所有类文件到自己的项目中即可

# Usage

#### 1、Application 中 初始化

    ActivitySwitcher.getInstance().init(this);

#### 2、在 Activity 中重写 dispatchTouchEvent 处理事件分发。最好直接在 BaseActivity 中处理。万事大吉

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        activitySwitcher.processTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

   如果不想通过手势打开 ActivitySwitcher，可以通过以下方式手动打开
   
       activitySwitcher.showSwitcher();

#### 3、Android 手机默认按下返回键就回 finish 掉当前 Activity，这与本库冲突，所以需要重写 onBackPressed 方法，同样最好在 BaseActivity 中去重写

    @Override
    public void onBackPressed() {
        activitySwitcher.finishSwitch(this);
    }

#### 4、如果希望监听 ActivitySwitcher 当前的行为状态，可以添加以下代码

    activitySwitcher.setOnActivitySwitchListener(new ActivitySwitcher.OnActivitySwitchListener() {
        @Override
        public void onSwitchStarted() {}

        @Override
        public void onSwitchFinished(Activity activity) {}
    });
    
   onSwitchStarted ：在 ActivitySwitcher 打开后被回调
   onSwitchFinished 在 ActivitySwitcher 关闭后被回调

   全部示例代码详情请前往 [MainActivity](https://github.com/Hitomis/ActivitySwitcher/blob/master/app/src/main/java/com/hitomi/activityswitcher/MainActivity.java) 查看

#Licence

      Copyright 2016 Hitomis, Inc.

      Licensed under the Apache License, Version 2.0 (the "License");
      you may not use this file except in compliance with the License.
      You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

      Unless required by applicable law or agreed to in writing, software
      distributed under the License is distributed on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      See the License for the specific language governing permissions and
      limitations under the License.
 


