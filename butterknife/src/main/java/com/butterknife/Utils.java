package com.butterknife;

import android.app.Activity;
import android.view.View;

/**
 * Author: 信仰年轻
 * Date: 2021-06-18 14:48
 * Email: hydznsqk@163.com
 * Des:
 */
public class Utils {
    public static <T extends View> T findViewById(Activity activity,int viewId){
        return (T) activity.findViewById(viewId);
    }
}
