package com.jasonvzhao.core;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.lang.ref.WeakReference;

import androidx.annotation.IdRes;
import androidx.annotation.StringRes;

/**
 * ================================================
 * 页面浮标抽象类 一般的悬浮窗都需要继承该抽象接口
 * ================================================
 */
public abstract class AbsFloatView implements FloatView, FloatFrameLayout.OnTouchEventListener{
    private String TAG = this.getClass().getSimpleName();

    /**
     * 创建FrameLayout#LayoutParams 内置悬浮窗调用
     */
    protected FrameLayout.LayoutParams mFrameLayoutParams;
    /**
     * 创建FrameLayout#LayoutParams 系统悬浮窗调用
     */
    private WindowManager.LayoutParams mWindowLayoutParams;
    private Handler mHandler;

    /**
     * 当前dokitViewName 用来当做map的key 和dokitViewIntent的tag一致
     */
    protected String mTag = TAG;

    private Bundle mBundle;
    /**
     * weakActivity attach activity
     */
    private WeakReference<Activity> mAttachActivity;

    private FloatFrameLayout mRootView;
    /**
     * rootView的直接子View 一般是用户的xml布局 被添加到mRootView中
     */
    private View mChildView;

    private FloatViewLayoutParams mFloatViewLayoutParams;
    /**
     * 上一次DoKitview的位置信息
     */
    private LastFloatViewPosInfo mLastFloatViewPosInfo;
    /**
     * 根布局的实际宽
     */
    private int mDokitViewWidth = 0;
    /**
     * 根布局的实际高
     */
    private int mDokitViewHeight = 0;
    private ViewTreeObserver mViewTreeObserver;
    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (mRootView != null) {
                //每次布局发生变动的时候重新赋值
                mDokitViewWidth = mRootView.getMeasuredWidth();
                mDokitViewHeight = mRootView.getMeasuredHeight();
                if (mLastFloatViewPosInfo != null) {
                    mLastFloatViewPosInfo.setDokitViewWidth(mDokitViewWidth);
                    mLastFloatViewPosInfo.setDokitViewHeight(mDokitViewHeight);
                }
            }
        }
    };
    /**
     * 页面启动模式
     */
    private int mode;

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    protected int screenHeight;
    protected int screenWidth;

    /**
     * 构造函数
     */
    public AbsFloatView() {
        TAG = this.getClass().getSimpleName();
        if (FloatViewManager.getInstance().getLastDokitViewPosInfo(mTag) == null) {
            mLastFloatViewPosInfo = new LastFloatViewPosInfo();
            FloatViewManager.getInstance().saveLastDokitViewPosInfo(mTag, mLastFloatViewPosInfo);
        } else {
            mLastFloatViewPosInfo = FloatViewManager.getInstance().getLastDokitViewPosInfo(mTag);
        }
        //创建主线程handler
        mHandler = new Handler(Looper.myLooper());
        screenHeight = ScreenUtils.getScreenHeight()-StatusBarUtil.getStatusBarHeight();
        screenWidth = ScreenUtils.getScreenWidth();
    }

    /**
     * 执行floatPage create
     *
     * @param context 上下文环境
     */
    @SuppressLint("ClickableViewAccessibility")
    void performCreate(Context context) {
        try {
            //调用onCreate方法
            onCreate(context);
            mRootView = new FloatFrameLayout(context);
            //添加根布局的layout回调
            addViewTreeObserverListener();
            //调用onCreateView抽象方法
            mChildView = onCreateView(context, mRootView);
            //将子View添加到rootview中
            mRootView.addView(mChildView);
            //设置根布局的手势拦截
            mRootView.setEventListener(this);
//            mRootView.setOnTouchListener(new View.OnTouchListener() {
//                @Override
//                public boolean onTouch(View v, MotionEvent event) {
//                    if (getRootView() != null) {
//                        return mTouchProxy.onTouchEvent(v, event);
//                    } else {
//                        return false;
//                    }
//                }
//            });
            //调用onViewCreated回调
            onViewCreated(mRootView);

            mFloatViewLayoutParams = new FloatViewLayoutParams();
            //分别创建对应的LayoutParams
            mFrameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
            mFrameLayoutParams.gravity = Gravity.BOTTOM | Gravity.END;
            mFloatViewLayoutParams.gravity = Gravity.TOP | Gravity.LEFT;
            initDokitViewLayoutParams(mFloatViewLayoutParams);
            onNormalLayoutParamsCreated(mFrameLayoutParams);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    void performDestroy() {
        //移除布局监听
        removeViewTreeObserverListener();
        mHandler = null;
        mRootView = null;
        onDestroy();
    }

    private void addViewTreeObserverListener() {
        if (mViewTreeObserver == null && mRootView != null && mOnGlobalLayoutListener != null) {
            mViewTreeObserver = mRootView.getViewTreeObserver();
            mViewTreeObserver.addOnGlobalLayoutListener(mOnGlobalLayoutListener);
        }

    }

    private void removeViewTreeObserverListener() {
        if (mViewTreeObserver != null && mOnGlobalLayoutListener != null) {
            if (mViewTreeObserver.isAlive()) {
                mViewTreeObserver.removeOnGlobalLayoutListener(mOnGlobalLayoutListener);
            }
        }

    }


    /**
     * 确定普通浮标的初始位置
     * LayoutParams创建完以后调用
     * 调用时建议放在实现下方
     *
     * @param params
     */
    private void onNormalLayoutParamsCreated(FrameLayout.LayoutParams params) {
        //如果有上一个页面的位置记录 这更新位置
        params.width = mFloatViewLayoutParams.width;
        params.height = mFloatViewLayoutParams.height;
        params.gravity = mFloatViewLayoutParams.gravity;
        params.leftMargin = mFloatViewLayoutParams.x;
        params.topMargin = mFloatViewLayoutParams.y;
        portraitOrLandscape(params);
    }

    /**
     * 用于普通模式下的横竖屏切换
     */
    private void portraitOrLandscape(FrameLayout.LayoutParams params) {
        Point point = FloatViewManager.getInstance().getDokitViewPos(mTag);
        if (point != null) {
            //横竖屏切换兼容
            if (ScreenUtils.isPortrait()) {
                if (mLastFloatViewPosInfo.isPortrait()) {
                    params.leftMargin = point.x;
                    params.topMargin = point.y;
                } else {
                    params.leftMargin = (int) (point.x * mLastFloatViewPosInfo.getLeftMarginPercent());
                    params.topMargin = (int) (point.y * mLastFloatViewPosInfo.getTopMarginPercent());
                }
            } else {
                if (mLastFloatViewPosInfo.isPortrait()) {
                    params.leftMargin = (int) (point.x * mLastFloatViewPosInfo.getLeftMarginPercent());
                    params.topMargin = (int) (point.y * mLastFloatViewPosInfo.getTopMarginPercent());
                } else {
                    params.leftMargin = point.x;
                    params.topMargin = point.y;
                }
            }
        } else {
            //横竖屏切换兼容
            if (ScreenUtils.isPortrait()) {
                if (mLastFloatViewPosInfo.isPortrait()) {
                    params.leftMargin = mFloatViewLayoutParams.x;
                    params.topMargin = mFloatViewLayoutParams.y;
                } else {
                    params.leftMargin = (int) (mFloatViewLayoutParams.x * mLastFloatViewPosInfo.getLeftMarginPercent());
                    params.topMargin = (int) (mFloatViewLayoutParams.y * mLastFloatViewPosInfo.getTopMarginPercent());
                }
            } else {
                if (mLastFloatViewPosInfo.isPortrait()) {
                    params.leftMargin = (int) (mFloatViewLayoutParams.x * mLastFloatViewPosInfo.getLeftMarginPercent());
                    params.topMargin = (int) (mFloatViewLayoutParams.y * mLastFloatViewPosInfo.getTopMarginPercent());
                } else {
                    params.leftMargin = mFloatViewLayoutParams.x;
                    params.topMargin = mFloatViewLayoutParams.y;
                }
            }
        }
        mLastFloatViewPosInfo.setPortrait();
        mLastFloatViewPosInfo.setLeftMargin(params.leftMargin);
        mLastFloatViewPosInfo.setTopMargin(params.topMargin);
    }


    @Override
    public void onDestroy() {
        FloatViewManager.getInstance().removeLastDokitViewPosInfo(mTag);
        mAttachActivity = null;
    }

    /**
     * 默认实现为true
     *
     * @return
     */
    @Override
    public boolean canDrag() {
        return true;
    }

    /**
     * 搭配shouldDealBackKey使用
     */
    @Override
    public boolean onBackPressed() {
        return false;
    }

    /**
     * 默认不自己处理返回按键
     *
     * @return
     */
    @Override
    public boolean shouldDealBackKey() {
        return false;
    }

    @Override
    public void onEnterBackground() {

    }

    @Override
    public void onEnterForeground() {

    }

    @Override
    public void onMove(int x, int y, int dx, int dy) {
        if (!canDrag()) {
            return;
        }
        mFrameLayoutParams.leftMargin += dx;
        mFrameLayoutParams.topMargin += dy;
        //更新图标位置
        updateViewLayout(mTag, false);

    }


    /**
     * 手指弹起时保存当前浮标位置
     *
     * @param x
     * @param y
     */
    @Override
    public void onUp(int x, int y) {
        if (!canDrag()) {
            return;
        }

        //保存在内存中
        FloatViewManager.getInstance().saveDokitViewPos(mTag, mFrameLayoutParams.leftMargin, mFrameLayoutParams.topMargin);

    }

    /**
     * 手指按下时的操作
     *
     * @param x
     * @param y
     */
    @Override
    public void onDown(int x, int y) {
        if (!canDrag()) {
            return;
        }
    }










    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    /**
     * 系统悬浮窗需要调用
     *
     * @return
     */
    public Context getContext() {
        if (mRootView != null) {
            return mRootView.getContext();
        } else {
            return null;
        }
    }


    public Resources getResources() {
        if (getContext() == null) {
            return null;
        }
        return getContext().getResources();
    }

    public String getString(@StringRes int resId) {
        if (getContext() == null) {
            return null;
        }
        return getContext().getString(resId);
    }

    public boolean isShow() {
        return mRootView.isShown();
    }

    protected <T extends View> T findViewById(@IdRes int id) {
        return mRootView.findViewById(id);
    }

    public View getRootView() {
        return mRootView;
    }

    public FrameLayout.LayoutParams getNormalLayoutParams() {
        return mFrameLayoutParams;
    }

    public WindowManager.LayoutParams getSystemLayoutParams() {
        return mWindowLayoutParams;
    }

    /**
     * 将当前dokitView于activity解绑
     */
    public void detach() {
        FloatViewManager.getInstance().detach(this);
    }

    /**
     * 操作DecorView的直接子布局
     * 测试专用
     */
    public void dealDecorRootView(FrameLayout decorRootView) {
        if (decorRootView == null) {
            return;
        }
    }

    /**
     * 更新view的位置
     *
     * @param isActivityResume 是否是从其他页面返回时更新的位置
     */
    public void updateViewLayout(String tag, boolean isActivityResume) {
        if (mRootView == null || mChildView == null || mFrameLayoutParams == null) {
            return;
        }
        if (isActivityResume) {
            Point point = FloatViewManager.getInstance().getDokitViewPos(tag);
            if (point != null) {
                mFrameLayoutParams.leftMargin = point.x;
                mFrameLayoutParams.topMargin = point.y;
            }
        } else {
            //非页面切换的时候保存当前位置信息
            mLastFloatViewPosInfo.setPortrait();
            mLastFloatViewPosInfo.setLeftMargin(mFrameLayoutParams.leftMargin);
            mLastFloatViewPosInfo.setTopMargin(mFrameLayoutParams.topMargin);
        }
        mFrameLayoutParams.width = mDokitViewWidth;
        mFrameLayoutParams.height = mDokitViewHeight;

        //portraitOrLandscape(mFrameLayoutParams);
        resetBorderline(mFrameLayoutParams);
        mRootView.setLayoutParams(mFrameLayoutParams);
    }

    /**
     * 限制边界 调用的时候必须保证是在控件能获取到宽高德前提下
     */
    private void resetBorderline(FrameLayout.LayoutParams normalFrameLayoutParams) {
        //如果是系统模式或者手动关闭动态限制边界
        if (!restrictBorderline()) {
            return;
        }
        //LogHelper.i(TAG, "topMargin==>" + normalFrameLayoutParams.topMargin + "  leftMargin====>" + normalFrameLayoutParams.leftMargin);
        if (normalFrameLayoutParams.topMargin <= 0) {
            normalFrameLayoutParams.topMargin = 0;
        }

        if (ScreenUtils.isPortrait()) {
            if (normalFrameLayoutParams.topMargin >= getScreenLongSideLength() - mDokitViewHeight) {
                normalFrameLayoutParams.topMargin = getScreenLongSideLength() - mDokitViewHeight;
            }
        } else {
            if (normalFrameLayoutParams.topMargin >= getScreenShortSideLength() - mDokitViewHeight) {
                normalFrameLayoutParams.topMargin = getScreenShortSideLength() - mDokitViewHeight;
            }
        }


        if (normalFrameLayoutParams.leftMargin <= 0) {
            normalFrameLayoutParams.leftMargin = 0;
        }

        if (ScreenUtils.isPortrait()) {
            if (normalFrameLayoutParams.leftMargin >= getScreenShortSideLength() - mDokitViewWidth) {
                normalFrameLayoutParams.leftMargin = getScreenShortSideLength() - mDokitViewWidth;
            }
        } else {
            if (normalFrameLayoutParams.leftMargin >= getScreenLongSideLength() - mDokitViewWidth) {
                normalFrameLayoutParams.leftMargin = getScreenLongSideLength() - mDokitViewWidth;
            }
        }

    }


    /**
     * 是否限制布局边界
     *
     * @return
     */
    public boolean restrictBorderline() {
        return true;
    }

    public String getTag() {
        return mTag;
    }

    public void setTag(String mTag) {
        this.mTag = mTag;
    }

    public Bundle getBundle() {
        return mBundle;
    }

    public void setBundle(Bundle mBundle) {
        this.mBundle = mBundle;
    }

    public Activity getActivity() {
        if (mAttachActivity != null) {
            return mAttachActivity.get();
        }
        return ActivityUtils.getTopActivity();
    }

    public void setActivity(Activity activity) {
        this.mAttachActivity = new WeakReference<>(activity);
    }

    public void post(Runnable r) {
        mHandler.post(r);
    }

    public void postDelayed(Runnable r, long delayMillis) {
        mHandler.postDelayed(r, delayMillis);
    }

    /**
     * 设置当前kitView不响应触摸事件
     * 控件默认响应触摸事件
     * 需要在子view的onViewCreated中调用
     */
    public void setDokitViewNotResponseTouchEvent(View view) {
        if (view != null) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return false;
                }
            });
        }

    }


    /**
     * 获取屏幕短边的长度 不包含statusBar
     *
     * @return
     */
    public int getScreenShortSideLength() {
        if (ScreenUtils.isPortrait()) {
            return ScreenUtils.getScreenWidth();
        } else {
            return ScreenUtils.getScreenHeight();
        }
    }

    /**
     * 获取屏幕长边的长度 不包含statusBar
     *
     * @return
     */
    public int getScreenLongSideLength() {
        if (ScreenUtils.isPortrait()) {
            //ScreenUtils.getScreenHeight(); 包含statusBar
            //ScreenUtils.getAppScreenHeight(); 不包含statusBar
            return ScreenUtils.getScreenHeight();
        } else {
            return ScreenUtils.getScreenWidth();
        }
    }


    /**
     * 是否是普通的浮标模式
     *
     * @return
     */
    public boolean isNormalMode() {
        return true;
    }

    /**
     * 强制刷新当前dokitview
     */
    public void invalidate() {
        if (mRootView != null && mFrameLayoutParams != null) {
            mRootView.setLayoutParams(mFrameLayoutParams);
        }
    }

}
