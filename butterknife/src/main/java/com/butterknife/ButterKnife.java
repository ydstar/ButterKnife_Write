package com.butterknife;

import android.app.Activity;

import java.lang.reflect.Constructor;

/**
 * Author: 信仰年轻
 * Date: 2021-06-18 14:48
 * Email: hydznsqk@163.com
 * Des: 通过反射找到响应activity对应的Activity_ViewBinding类,然后对字段属性进行findViewById
 */
public class ButterKnife {

    public static Unbinder bind(Activity activity) {
        // xxxActivity_ViewBinding viewBinding = new xxxActivity_ViewBinding(this);
        try {
            Class<? extends Unbinder> bindClassName = (Class<? extends Unbinder>)
                    Class.forName(activity.getClass().getName() + "_ViewBinding");
            // 构造函数
            Constructor<? extends Unbinder> bindConstructor = bindClassName.getDeclaredConstructor(activity.getClass());
            //创建的时候就把字段findViewById了
            Unbinder unbinder = bindConstructor.newInstance(activity);
            // 返回 Unbinder
            return unbinder;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Unbinder.EMPTY;
    }
}
