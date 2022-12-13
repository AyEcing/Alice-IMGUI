package com.club.mobile;


import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class MethodALL {
    /**
     * 2022/9/11
     * BY.阿夜
     * 方法汇总
     */

    public static WindowManager.LayoutParams getAttributes(boolean isWindow) {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        params.flags = WindowManager.LayoutParams.FLAG_FULLSCREEN | WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        if (isWindow) {
            params.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        params.format = PixelFormat.RGBA_8888;            // 设置图片格式，效果为背景透明
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            params.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        params.gravity = Gravity.LEFT | Gravity.TOP;        // 调整悬浮窗显示的停靠位置为左侧置顶
        params.x = params.y = 0;
        params.width = params.height = isWindow ? WindowManager.LayoutParams.MATCH_PARENT : 0;
        return params;
    }

    public static void Sshell(final String shell) {
        Log.d("Alice-", shell);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Process proc = Runtime.getRuntime().exec("sh");
                    DataOutputStream ous = new DataOutputStream(proc.getOutputStream());
                    ous.write(shell.getBytes());
                    ous.writeBytes("\n");
                    ous.flush();
                    ous.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    public static boolean OutFiles(Context context, String outPath, String fileName) {
        File file = new File(outPath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e("--Method--", "copyAssetsSingleFile: cannot create directory.");
                return false;
            }
        }
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            File outFile = new File(file, fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(outFile);
            // Transfer bytes from inputStream to fileOutputStream
            byte[] buffer = new byte[1024];
            int byteRead;
            while (-1 != (byteRead = inputStream.read(buffer))) {
                fileOutputStream.write(buffer, 0, byteRead);
            }
            inputStream.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isRoot() {
        String[] rootRelatedDirs = new String[]{"/su", "/su/bin/su", "/sbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/data/local/su", "/system/xbin/su", "/system/bin/su", "/system/sd/xbin/su", "/system/bin/failsafe/su", "/system/bin/cufsdosck", "/system/xbin/cufsdosck", "/system/bin/cufsmgr", "/system/xbin/cufsmgr", "/system/bin/cufaevdd", "/system/xbin/cufaevdd", "/system/bin/conbb", "/system/xbin/conbb"};
        boolean hasRootDir = false;//初始为没有root权限
        String[] rootDirs;
        int dirCount = (rootDirs = rootRelatedDirs).length;

        for (int i = 0; i < dirCount; ++i) {    //for循环遍历数组
            String dir = rootDirs[i];
            if ((new File(dir)).exists()) {
                hasRootDir = true;
                break;
            }
        }
        return Build.TAGS != null && Build.TAGS.contains("test-keys") || hasRootDir;

    }

    public static String stringToMD5(String plainText) {
        byte[] secretBytes = null;
        try {
            secretBytes = MessageDigest.getInstance("md5").digest(
                    plainText.getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("没有这个md5算法！");
        }
        String md5code = new BigInteger(1, secretBytes).toString(16);
        for (int i = 0; i < 32 - md5code.length(); i++) {
            md5code = "0" + md5code;
        }
        return md5code;
    }


	//没得okhttp用这个post和get吧 需要自己写new一个多线程就可以了 反正没OKhttp好用我感觉

    public static String Post(String ur,String byteString){
        String fh="";
        try {
            URL url=new URL(ur);
            HttpURLConnection HttpURLConnection=(HttpURLConnection) url.openConnection();
            HttpURLConnection.setReadTimeout(9000);
            HttpURLConnection.setRequestMethod("POST");
            OutputStream outputStream = HttpURLConnection.getOutputStream();

            outputStream.write(byteString.getBytes());
            BufferedReader BufferedReader=new BufferedReader(new InputStreamReader(HttpURLConnection.getInputStream()));
            String String="";
            StringBuffer StringBuffer=new StringBuffer();
            while ((String = BufferedReader.readLine()) != null) {
                StringBuffer.append(String);
            }
            fh=StringBuffer.toString();
        }
        catch (IOException e){}
        return fh;
    }
    public static String Get(String http) {
        String msg = "";
        try {
            // HttpURLConnection
            // 1.实例化一个URL对象
            URL url = new URL(http);

            // 2.获取HttpURLConnection实例
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 3.设置和请求相关的属性
            /// 请求方式
            conn.setRequestMethod("GET");
            /// 请求超时时长
            conn.setConnectTimeout(6000);

            // 4.获取响应码  200：成功  404：未请求到指定资源    500：服务器异常
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // 5.判断响应码并获取响应数据（响应正文）
                /// 获取响应的流
                InputStream in = conn.getInputStream();
                byte[] b = new byte[1024];
                int len = 0;
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                /// 在循环中读取输入流
                while ((len = in.read(b)) > -1) {// in.read()返回是int类型数据，代表实际读到的数据长度
                    // 将字节数组里面的内容写入缓存流
                    // 参数1：待写入的数组   参数2：起点  参数3：长度
                    baos.write(b, 0, len);
                }

                msg = baos.toString();
                Log.e("Alice", msg + "======");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return msg;
    }

}
