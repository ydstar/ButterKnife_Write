package com.butterknife;


import android.support.annotation.UiThread;

/**
 * Author: 信仰年轻
 * Date: 2021-06-18 14:48
 * Email: hydznsqk@163.com
 * Des:
 */
public interface Unbinder {
    @UiThread
    void unbind();

    Unbinder EMPTY = new Unbinder() {
        @Override
        public void unbind() {
        }
    };
}
