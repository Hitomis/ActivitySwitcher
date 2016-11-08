# ActivitySwitcher

   ActivitySwitcher 是一个基于 Activity 视图操作管理库，可以实现 Activity 之间任意跳转、关闭任意一个 Activity
以及结束应用程序等功能。

# Preview

录制图像有丢帧的情况，所以预览图效果不够流畅，背景图显示的也有问题。
<img src="preview/activity_swither.gif"/>

# Import

导入 aslibrary Module 作为依赖库, 或者直接复制 com.hitomi.aslibrary 中所有类文件到自己的项目中即可

# Usage

   1、Application 中 初始化

    ActivitySwitcher.getInstance().init(this);

   2、在 Activity 中重写 dispatchTouchEvent 处理事件分发。最好直接在 BaseActivity 中处理。万事大吉

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        activitySwitcher.processTouchEvent(ev);
        return super.dispatchTouchEvent(ev);
    }

   如果不想通过手势打开 ActivitySwitcher，可以通过以下方式手动打开
    activitySwitcher.showSwitcher();

   3、Android 手机默认按下返回键就回 finish 掉当前 Activity，这与本库冲突，所以需要重写 onBackPressed 方法，同样最好在 BaseActivity 中去重写

    @Override
    public void onBackPressed() {
        activitySwitcher.finishSwitch(this);
    }

   示例代码详情前前往 [MainActivity](https://github.com/Hitomis/ActivitySwitcher/blob/master/app/src/main/java/com/hitomi/activityswitcher/MainActivity.java) 查看

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
 


