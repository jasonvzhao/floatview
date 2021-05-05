package com.jasonvzhao.floatview;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.jasonvzhao.core.AbsFloatView;
import com.jasonvzhao.core.FloatViewLayoutParams;
import com.jasonvzhao.core.FloatViewManager;
import com.jasonvzhao.core.ScreenUtils;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;

public class WaitMatchFloatView extends AbsFloatView {
    private int width;
    private int height;

    @Override
    public void onCreate(Context context) {
        Log.d(getClass().getSimpleName(), "onCreate: ");
        width = ScreenUtils.dp2px(120);
        height = ScreenUtils.dp2px(120);
    }

    @Override
    public View onCreateView(Context context, FrameLayout rootView) {
        Log.d(getClass().getSimpleName(), "onCreateView: ");
        SVGAImageView imageView = new SVGAImageView(context);
        SVGAParser.Companion.shareParser().decodeFromAssets("game_match_float_view.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(SVGAVideoEntity svgaVideoEntity) {
                SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                imageView.setImageDrawable(drawable);
                imageView.startAnimation();
            }

            @Override
            public void onError() {

            }
        });
        imageView.setOnClickListener(v -> Toast.makeText(context.getApplicationContext(), "点击了图片", Toast.LENGTH_SHORT).show());
        View view = LayoutInflater.from(context).inflate(R.layout.test_latyout, null, false);
        view.findViewById(R.id.btn1).setOnClickListener(v -> Toast.makeText(context.getApplicationContext(), "点击了图片1", Toast.LENGTH_SHORT).show() );
        view.findViewById(R.id.btn2).setOnClickListener(v -> Toast.makeText(context.getApplicationContext(), "点击了图片2", Toast.LENGTH_SHORT).show() );
        return view;
    }


    @Override
    public boolean canDrag() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        FrameLayout.LayoutParams params = getNormalLayoutParams();
        params.width = width;
        params.height = height;
        invalidate();
    }

    @Override
    public void onViewCreated(FrameLayout rootView) {
        Log.d(getClass().getSimpleName(), "onViewCreated: ");
    }

    @Override
    public void initDokitViewLayoutParams(FloatViewLayoutParams params) {
        params.x = screenWidth - width;
        params.y = screenHeight - height;
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
        ValueAnimator animator = ValueAnimator.ofInt(mFrameLayoutParams.leftMargin, x > screenWidth / 2 ? screenWidth - mFrameLayoutParams.width : 0);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFrameLayoutParams.leftMargin = (int) animation.getAnimatedValue();
                updateViewLayout(mTag, false);
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                //保存在内存中
                FloatViewManager.getInstance().saveDokitViewPos(mTag, mFrameLayoutParams.leftMargin, mFrameLayoutParams.topMargin);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.start();

    }
}
