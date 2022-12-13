package com.club.mobile;

import static android.view.WindowManager.*;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

//悬浮窗服务不讲 就是一般怎么开悬浮窗就怎么开 需要用到的无非是jni和java的交互 不懂就不改 
public class MyService extends Service {

    static {
        System.loadLibrary("Sea");
    }

    private final static String NOTIFICATION_CHANNEL_ID = "CHANNEL_ID";
    private final static String NOTIFICATION_CHANNEL_NAME = "阿夜守护进程";
    private final static int FOREGROUND_ID = 1;
    public static boolean isShow = true;
    public static native void Resolution(float px, float py);

    public static WindowManager windowManager;
    public int type;

    static GLES3JNIView display;
    @SuppressLint("StaticFieldLeak")
    static View vTouch;
    private static LayoutParams vParams;
    static LayoutParams params;
    static DisplayMetrics dm = new DisplayMetrics();

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void RemoveXFC()
    {
        vTouch.setVisibility(View.GONE);
        display.setVisibility(View.GONE);
        Log.d("Alice-","view GONE");
    }
    public static void StartXFC()
    {
        vTouch.setVisibility(View.VISIBLE);
        display.setVisibility(View.VISIBLE);
        Log.d("Alice-","view VISIBLE");

    }
	//remove悬浮窗和打开悬浮窗 我写上了 虽然一般用不到 不过万一呢 („ಡωಡ„)栓Q
    @SuppressLint({"WrongConstant", "RtlHardcoded", "ClickableViewAccessibility", "ObsoleteSdkInt"})
    @Override
    public void onCreate() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);//双悬浮窗
        windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        params = new LayoutParams();

        params.alpha = 0.8f;//设置透明的
        vParams = MethodALL.getAttributes(false);
        display = new GLES3JNIView(this);
        vTouch = new View(this);
        vTouch.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        GLES3JNIView.MotionEventClick(action != MotionEvent.ACTION_UP, event.getRawX(), event.getRawY());
                        String date = String.valueOf(action != MotionEvent.ACTION_UP);
                        Log.d("Alice-", date + "X" + event.getRawX() + "Y" + event.getRawY());
                        break;
                    default:
                        break;
                }
                return false;
            }
        });

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] rect = GLES3JNIView.getWindowRect().split("\\|");
                    vParams.x = Integer.parseInt(rect[0]);
                    vParams.y = Integer.parseInt(rect[1]);
                    vParams.width = Integer.parseInt(rect[2]);
                    vParams.height = Integer.parseInt(rect[3]);
                    windowManager.updateViewLayout(vTouch, vParams);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.postDelayed(this, 1000);
            }
        }, 20);

        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        params.type = Build.VERSION_CODES.O <= Build.VERSION.SDK_INT ? LayoutParams.TYPE_APPLICATION_OVERLAY : LayoutParams.TYPE_SYSTEM_ALERT;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        params.format = PixelFormat.TRANSPARENT;
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        params.flags = //WindowManager.LayoutParams.FLAG_SECURE |防截屏
                LayoutParams.FLAG_NOT_TOUCHABLE |//不接受触控
                        LayoutParams.FLAG_NOT_FOCUSABLE |
                        LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        LayoutParams.FLAG_SPLIT_TOUCH |
                        LayoutParams.FLAG_HARDWARE_ACCELERATED |//硬件加速
                        LayoutParams.FLAG_FULLSCREEN |//隐藏状态栏导航栏以全屏(貌似没什么用)
                        LayoutParams.FLAG_LAYOUT_NO_LIMITS |//忽略屏幕边界
                        LayoutParams.FLAG_LAYOUT_ATTACHED_IN_DECOR |//显示在状态栏上方(貌似高版本无效
                        LayoutParams.FLAG_LAYOUT_IN_SCREEN;//布局充满整个屏幕 忽略应用窗口限制

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;//覆盖刘海
        }
        Log.w("Alice-", "Draw is Begin!");
        //滑动监听

        windowManager.addView(display,params);
        windowManager.addView(vTouch,vParams);
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Log.w("Alice-", "Draw is Stop!");
        super.onDestroy();
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        NotificationChannel notificationChannel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
        );
        notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(notificationChannel);

        intent = new Intent(getApplicationContext(), MainActivity.class);  //点击通知栏后想要被打开的页面MainActivity.class
        @SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);  //点击通知栏触发跳转
        Notification notification = new Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Alice")
                .setContentText("进程守护中...")
                .setContentIntent(pendingIntent)
                .build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(FOREGROUND_ID, notification);
        return Service.START_STICKY;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (params != null && display != null) {
            windowManager.getDefaultDisplay().getRealMetrics(dm);
            params.width = dm.widthPixels;
            params.height = dm.heightPixels;
            windowManager.updateViewLayout(display, params);
        }
        if (Build.VERSION.SDK_INT>30)
        {
            if (dm.heightPixels>dm.widthPixels) {
                Resolution(dm.heightPixels, dm.widthPixels);
            }
            else{
                Resolution(dm.widthPixels, dm.heightPixels);
            }
        }
        else {
            Resolution(dm.widthPixels, dm.heightPixels);
        }
        GLES3JNIView.real(dm.widthPixels, dm.heightPixels);
        Log.e("Alice-", "Width= " + dm.widthPixels + " Height= " + dm.heightPixels);

    }

}
