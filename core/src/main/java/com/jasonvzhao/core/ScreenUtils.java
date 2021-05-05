package com.jasonvzhao.core;

import android.app.Activity;
import android.app.Application;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;

import androidx.annotation.NonNull;

/**
 * <pre>
 *     desc  : 屏幕相关工具类
 * </pre>
 */
public final class ScreenUtils {

    private static Application sApplication;

    private ScreenUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static void  install(Application application){
        sApplication = application;
    }


    /**
     * 获取屏幕的高度（单位：px）
     *
     * @return 屏幕高
     */
    public static int getScreenHeight() {
        WindowManager wm = (WindowManager) sApplication.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return sApplication.getResources().getDisplayMetrics().heightPixels;
        }
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.y;
    }

    /**
     * 获取屏幕的宽度（单位：px）
     *
     * @return 屏幕宽
     */
    public static int getScreenWidth() {
        WindowManager wm = (WindowManager) sApplication.getSystemService(Context.WINDOW_SERVICE);
        if (wm == null) {
            return sApplication.getResources().getDisplayMetrics().widthPixels;
        }
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.x;
    }


    /**
     * 获取屏幕密度
     *
     * @return 屏幕密度
     */
    public static float getScreenDensity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    /**
     * 获取屏幕密度 DPI
     *
     * @return 屏幕密度 DPI
     */
    public static int getScreenDensityDpi(Context context) {
        return context.getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * 设置屏幕为全屏
     *
     * @param activity activity
     */
    public static void setFullScreen(@NonNull final Activity activity) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
    }

    /**
     * 设置屏幕为横屏
     * <p>还有一种就是在 Activity 中加属性 android:screenOrientation="landscape"</p>
     * <p>不设置 Activity 的 android:configChanges 时，
     * 切屏会重新调用各个生命周期，切横屏时会执行一次，切竖屏时会执行两次</p>
     * <p>设置 Activity 的 android:configChanges="orientation"时，
     * 切屏还是会重新调用各个生命周期，切横、竖屏时只会执行一次</p>
     * <p>设置 Activity 的 android:configChanges="orientation|keyboardHidden|screenSize"
     * （4.0 以上必须带最后一个参数）时
     * 切屏不会重新调用各个生命周期，只会执行 onConfigurationChanged 方法</p>
     *
     * @param activity activity
     */
    public static void setLandscape(@NonNull final Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    /**
     * 设置屏幕为竖屏
     *
     * @param activity activity
     */
    public static void setPortrait(@NonNull final Activity activity) {
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    /**
     * 判断是否横屏
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isLandscape() {
        return sApplication.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * 判断是否竖屏
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isPortrait() {
        return sApplication.getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * 获取屏幕旋转角度
     *
     * @param activity activity
     * @return 屏幕旋转角度
     */
    public static int getScreenRotation(@NonNull final Activity activity) {
        switch (activity.getWindowManager().getDefaultDisplay().getRotation()) {
            default:
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 90;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 270;
        }
    }

    /**
     * 截屏
     *
     * @param activity activity
     * @return Bitmap
     */
    public static Bitmap screenShot(@NonNull final Activity activity) {
        return screenShot(activity, false);
    }

    /**
     * 截屏
     *
     * @param activity activity
     * @return Bitmap
     */
    public static Bitmap screenShot(@NonNull final Activity activity, boolean isDeleteStatusBar) {
        View decorView = activity.getWindow().getDecorView();
        decorView.setDrawingCacheEnabled(true);
        decorView.buildDrawingCache();
        Bitmap bmp = decorView.getDrawingCache();
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        Bitmap ret;
        if (isDeleteStatusBar) {
            Resources resources = activity.getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            int statusBarHeight = resources.getDimensionPixelSize(resourceId);
            ret = Bitmap.createBitmap(
                    bmp,
                    0,
                    statusBarHeight,
                    dm.widthPixels,
                    dm.heightPixels - statusBarHeight
            );
        } else {
            ret = Bitmap.createBitmap(bmp, 0, 0, dm.widthPixels, dm.heightPixels);
        }
        decorView.destroyDrawingCache();
        return ret;
    }

    /**
     * 获取一个 View 的缓存视图
     *
     * @param view
     * @return
     */
    public static Bitmap getCacheBitmapFromView(View view) {
        final boolean drawingCacheEnabled = true;
        view.setDrawingCacheEnabled(drawingCacheEnabled);
        view.buildDrawingCache(drawingCacheEnabled);
        final Bitmap drawingCache = view.getDrawingCache();
        Bitmap bitmap;
        if (drawingCache != null) {
            bitmap = Bitmap.createBitmap(drawingCache);
            view.setDrawingCacheEnabled(false);
        } else {
            bitmap = null;
        }
        return bitmap;
    }

    public static String saveBitMap(Bitmap bitmap, Context context) {
        if (bitmap != null) {
            try {
                // 首先保存图片
                File appDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "siyu");
                if (!appDir.exists()) {
                    appDir.mkdir();
                }
                // 图片文件路径
                String filename = "siyu_screenshot" + ".png";
                File file = new File(appDir, filename);
                FileOutputStream os = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                os.flush();
                os.close();
                return file.getAbsolutePath();
            } catch (Exception e) {
            }
        }
        return null;
    }


    /**
     * 获取图片截取的路径
     *
     * @return 存在返回path，不存在返回null
     */
    public static String getScreenPath() {
        File appDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "siyu");
        String filename = "siyu_screenshot" + ".png";
        File file = new File(appDir, filename);
        if (file.exists()) {
            return file.getAbsolutePath();
        } else {
            return null;
        }
    }


    /**
     * 分享
     **/
    public static void ShareImage(Context context, String imagePath) {
        if (imagePath != null) {
            Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送的属性
            File file = new File(imagePath);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));// 分享的内容
            intent.setType("image/*");// 分享发送的数据类型
            Intent chooser = Intent.createChooser(intent, "Share screen shot");
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(chooser);
            }
        }
    }

    /**
     * 判断是否锁屏
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isScreenLock(Context context) {
        KeyguardManager km =
                (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        return km != null && km.inKeyguardRestrictedInputMode();
    }

    /**
     * 设置进入休眠时长
     * <p>需添加权限 {@code <uses-permission android:name="android.permission.WRITE_SETTINGS" />}</p>
     *
     * @param duration 时长
     */
    public static void setSleepDuration(Context context, final int duration) {
        Settings.System.putInt(
                context.getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT,
                duration
        );
    }

    /**
     * 获取进入休眠时长
     *
     * @return 进入休眠时长，报错返回-123
     */
    public static int getSleepDuration(Context context) {
        try {
            return Settings.System.getInt(
                    context.getContentResolver(),
                    Settings.System.SCREEN_OFF_TIMEOUT
            );
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return -123;
        }
    }

    /**
     * 判断是否是平板
     *
     * @return {@code true}: 是<br>{@code false}: 否
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * dp 转 px
     *
     * @param dpValue dp 值
     * @return px 值
     */
    public static int dp2px(final float dpValue) {
        final float scale = sApplication.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px 转 dp
     *
     * @param pxValue px 值
     * @return dp 值
     */
    public static int px2dp(final float pxValue) {
        final float scale = sApplication.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * 判断是否存在NavigationBar
     *
     * @param context -
     * @return -
     */
    public static boolean checkDeviceHasNavigationBar(Context context) {
        boolean hasNavigationBar = false;
        Resources rs = context.getResources();
        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavigationBar = rs.getBoolean(id);
        }
        try {
            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
            Method m = systemPropertiesClass.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavigationBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavigationBar = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasNavigationBar;
    }

    public static int getDeviceNavigationBarHeight(Context activity) {
        if (checkDeviceIsHasNavigationBar(activity)) {
            return getNavigationBarHeight(activity);
        } else {
            return 0;
        }
    }

    public static boolean checkDeviceIsHasNavigationBar(Context activity) {
        // 特效手机判断
        if (Build.MODEL != null && (Build.MODEL.equals("NEM-AL10") || Build.MODEL.equals("H60-L02"))) {
            return true;
        }

        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        boolean hasMenuKey = ViewConfiguration.get(activity).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
        if (!hasMenuKey && !hasBackKey) {
            // 有导航栏
            return true;
        }
        return false;
    }

    public static int m_NavigationBarHeight = -1;

    /**
     * 获取NavigationBar的高度
     */
    public static int getNavigationBarHeight(Context context) {
        if (m_NavigationBarHeight == -1) {
            m_NavigationBarHeight = 0;
            Resources rs = context.getResources();
            int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
            if (id != 0 && checkDeviceHasNavigationBar(context)) {
                m_NavigationBarHeight = rs.getDimensionPixelSize(id);
            }
        }
        return m_NavigationBarHeight;
    }

    //============== 注册ContentResolver#ContentObserver时使用 ===============
    /**
     * 是否展示了 navigationbar
     *
     * @return
     */
    public static boolean checkNavigation(Context context) {
        String navigationbarTag = initDeviceInfo();
        int navigationBarIsMin = 0;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            navigationBarIsMin = Settings.System.getInt(context.getContentResolver(),
                    navigationbarTag, 0);
        } else {
            if (Build.BRAND.equalsIgnoreCase("VIVO") || Build.BRAND.equalsIgnoreCase("OPPO")) {
                navigationBarIsMin = Settings.Secure.getInt(context.getContentResolver(),
                        navigationbarTag, 0);
            } else {
                navigationBarIsMin = Settings.Global.getInt(context.getContentResolver(),
                        navigationbarTag, 0);
            }
        }
        return navigationBarIsMin > 1;
    }

    private static String initDeviceInfo() {
        String brand = Build.BRAND;
        String navigationbarTag;
        if (brand.equalsIgnoreCase("HUAWEI")) {
            navigationbarTag = "navigationbar_is_min";
        } else if (brand.equalsIgnoreCase("XIAOMI")) {
            navigationbarTag = "force_fsg_nav_bar";
        } else if (brand.equalsIgnoreCase("VIVO")) {
            navigationbarTag = "navigation_gesture_on";
        } else if (brand.equalsIgnoreCase("OPPO")) {
            navigationbarTag = "navigation_gesture_on";
        } else {
            navigationbarTag = "navigationbar_is_min";
        }
        return navigationbarTag;
    }
}