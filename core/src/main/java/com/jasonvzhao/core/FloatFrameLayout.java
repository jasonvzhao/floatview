package com.jasonvzhao.core;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * ================================================
 * 描    述：自定义FrameLayout 用来区分原生FrameLayout
 * ================================================
 */
public class FloatFrameLayout extends FrameLayout implements FloatViewInterface {

    private int lastMoveX;
    private int lastMoveY;
    private int lastX;
    private int lastY;
    private volatile int dx;
    private volatile int dy;
    private volatile int left;
    private volatile int top;
    private volatile int right;
    private volatile int bottom;
    private int parentWidth;
    private int parentHeight;
    private boolean draging = false;
    private int touchSlop;

    public FloatFrameLayout(@NonNull Context context) {
        this(context, null);
    }

    public FloatFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FloatFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    private OnTouchEventListener mEventListener;


    public void setEventListener(OnTouchEventListener eventListener) {
        mEventListener = eventListener;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        if (mEventListener == null) {
            return false;
        }
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                draging = false;
                lastX = (int) x;
                lastY = (int) y;
                if (mEventListener != null) {
                    mEventListener.onDown(lastX, lastY);
                }

                break;
            case MotionEvent.ACTION_MOVE:
                dx = x - lastX;
                dy = y - lastY;
                if (Math.abs(dx) > touchSlop || Math.abs(dy) > touchSlop) {
                    draging = true;
                } else {
                    break;
                }

                left = getLeft() + dx;
                top = getTop() + dy;
                right = getRight() + dx;
                bottom = getBottom() + dy;

                ViewGroup parent = (ViewGroup) getParent();
                if (parent != null) {
                    parentWidth = parent.getWidth();
                    parentHeight = parent.getHeight();
                }

                if (left < 0) {
                    left = 0;
                    right = left + getWidth();
                }
                if (right > parentWidth) {
                    right = parentWidth;
                    left = right - getWidth();
                }
                if (top < 0) {
                    top = 0;
                    bottom = top + getHeight();
                }

                if (bottom > parentHeight) {

                    bottom = parentHeight;
                    top = bottom - getHeight();
                }
                if (mEventListener != null) {
                    mEventListener.onMove(lastX, lastY, dx, dy);
                }

                lastX = x;
                lastY = y;
                break;
            case MotionEvent.ACTION_UP:
                if (draging) {
                    draging = false;
                    if (mEventListener != null) {
                        mEventListener.onUp(x, y);
                    }
                    return true;
                } else {
                    return false;
                }
            default:
                break;
        }
        return super.onInterceptTouchEvent(event);
    }



    public interface OnTouchEventListener {
        void onMove(int x, int y, int dx, int dy);

        void onUp(int x, int y);

        void onDown(int x, int y);
    }

}
