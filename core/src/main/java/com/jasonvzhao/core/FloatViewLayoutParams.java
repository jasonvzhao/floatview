package com.jasonvzhao.core;

/**
 * ================================================
 * 描    述：FloatView的初始化位置
 * ================================================
 */
public class FloatViewLayoutParams {




    /**
     * 只针对系统悬浮窗起作用 值基本上为Gravity
     */
    public int gravity;
    public int x;
    public int y;
    public int width;
    public int height;

    @Override
    public String toString() {
        return "DokitViewLayoutParams{" +
                "gravity=" + gravity +
                ", x=" + x +
                ", y=" + y +
                ", width=" + width +
                ", height=" + height +
                '}';
    }
}
