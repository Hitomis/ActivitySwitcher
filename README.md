# ActivitySwitcher


# Preview

<img src="preview/activity_swither.gif"/>

# Import

导入 aslibrary Module 作为依赖库, 或者直接复制 com.hitomi.aslibrary 中所有类文件到自己的项目中即可

# Usage
    ​
    1、Application 中 初始化
        ActivitySwitcher.getInstance().init(this);

    2、在 Activity 中重写 dispatchTouchEvent 处理事件分发。最好直接在 BaseActivity 中处理。万事大吉
        @Override
        public boolean dispatchTouchEvent(MotionEvent ev) {
            activitySwitcher.processTouchEvent(ev);
            return super.dispatchTouchEvent(ev);
        }

    3、Android 手机默认按下返回键就回 finish 掉当前 Activity，这与本库冲突，所以需要重写 onBackPressed 方法，同样最好在 BaseActivity 中去重写，万事大吉
        @Override
        public void onBackPressed() {
            activitySwitcher.finishSwitch(this);
        }

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
 


