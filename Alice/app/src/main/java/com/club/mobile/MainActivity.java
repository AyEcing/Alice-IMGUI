package com.club.mobile;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.club.mobile.okhttp.CallBackUtil;
import com.club.mobile.okhttp.OkhttpUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;

import okhttp3.Call;
import android.preference.DialogPreference;
import android.content.DialogInterface;
import android.view.View.OnClickListener;
import android.view.View;

/**
 * Create By.阿夜
 * 2022.09.27
 * AIDE全解 因为用的Android studio写 基本上移植过来到AIDE都有些小bug 勿喷 尽量给大家写详细
 * 对接的是T3验证 java端进行数据验证 请自己混淆和加固APP防止被小白拿到后台
 * 因为只是开源交流 imgui模板用的是读取b.log进行绘制 阿夜给个忠告 有空socket和mmap的传输 读取文件真的浪费了imgui的性能
 * 阿夜也是个小白 所以有问题也不要问我 这份源码会上传到GitHub 有需要的自行下载 源码仅供交流学习 请勿用于非法用途
 * 使用前请手动重新构建一次 不然可能打开闪退
 ***************************************************
 T3配置 
 软件更新 软件公告 安全传输->关闭
 加解密类型->bese64自定义编码集 
 全局数据加解密-＞开启
 请求值 返回值 加密->开启
 时间戳校验->关闭
 返回值格式->json
 json-code->string
 需要其他配置自己解决
 **/
public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("Sea");//导入静态库
    }

    int VERSIONCODE = 1001;//当前版本号
	//会与后台版本进行数据验证 后台如果不是10001就会弹出更新提示 而不是比较大小 
    public static String appkey = "7d74c09c4da2171ef75a371aace5d666";//APP秘钥
    public static String basekey = "ED5wuK/Vgaz69nqQFSphm1IrMsWZYJCfO+XeoAH3Gbv7BTN8j4ylPRtiLcx02dUk";//base64秘钥
    public static String 登录api = "http://w.t3yanzheng.com/7915CA357C091CE8";//登录
    public static String 解绑api = "http://w.t3yanzheng.com/FB497F0C172F73F5";//解绑
	public static String 公告api = "http://w.t3yanzheng.com/36056A14C734A820";//公告
	public static String 更新api = "http://w.t3yanzheng.com/23FF369543F428A1";//解绑

    Intent intent ;//悬浮窗intent
    public static native void setDir(String files);//传入软件目录 说明一下 因为imgui不能直接调用二进制 所以采取的是jni反射调用java 执行二进制 需要传输一个软件目录地址 

    private static final String[] NEEDED_PERMISSIONS = new String[]{//定义权限数值
		Manifest.permission.WRITE_SETTINGS,//写入设置
		Manifest.permission.WRITE_EXTERNAL_STORAGE,//写入外部存储
		Manifest.permission.READ_EXTERNAL_STORAGE,//读取文件
		Manifest.permission.READ_PHONE_STATE//读取手机信息
    };
    private String canOver = Settings.ACTION_MANAGE_OVERLAY_PERMISSION;//悬浮窗权限

    @SuppressLint({"RtlHardcoded", "ClickableViewAccessibility"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = new Intent(MainActivity.this, MyService.class);//初四话悬浮窗intent
        setContentView(R.layout.activity_main);//设置主界面布局

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//沉浸式状态栏

        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);//禁止软件首页截屏

        getWindow().setBackgroundDrawable(null);//设置背景透明

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//隐藏状态栏

        setDir(getFilesDir() + "/assets/");//写成文件路径

        /*            写出二进制           */
		MethodALL.OutFiles(MainActivity.this, getFilesDir() + "/assets", "cgj");//调用封装方法写出二进制

        MethodALL.OutFiles(MainActivity.this, getFilesDir() + "/assets", "draw");//调用封装方法写出二进制
		//有其他二进制一样要手动写入 不是lua那种自动给你写入的 自己动手，丰衣足食

		init();//这个是不显示更新和公告
		startService(intent); //这个是启用悬浮窗 如果要体验卡密系统 请注释掉本行 如果要体验完整Alice 请 把init()和startService(intent) 都注释掉 把UpAPP打开

		
        //UpAPP();//检测更新APP 如果是新版本就弹公告 不是新版本就更新 相当于必须验证是新版本才能用软件
		if (MethodALL.isRoot()) {//判断设备环境
			//判断为Root环境
			Toast.makeText(MainActivity.this, "Root", Toast.LENGTH_SHORT).show();
			MethodALL.Sshell("su -c chmod -R 7777 " + getFilesDir() + "/assets");//chmod -R 给予文件夹下所有二进制执行权限
		} else {
			//判断为框架环境
			Toast.makeText(MainActivity.this, "框架", Toast.LENGTH_SHORT).show();
			MethodALL.Sshell("chmod -R 7777 " + getFilesDir() + "/assets");//chmod -R 给予文件夹下所有二进制执行权限
		}
    }

    private void init() {

        if (!Settings.canDrawOverlays(getApplicationContext())) {//判断悬浮窗权限

            Log.w("Alice-", canOver + "权限未获取！");//打印日志

            Intent intent = new Intent(canOver);//创建悬浮窗Intent
            intent.setData(Uri.parse("package:" + getPackageName()));//得到目标包名
            startActivity(intent);//跳转悬获取悬浮窗
            Toast.makeText(MainActivity.this, "权限获取后请重新启动！", Toast.LENGTH_SHORT).show();

        }
        Button button = findViewById(R.id.activitymainButtonDL);//获取登录控件ID
        TextView textView = findViewById(R.id.activitymainTextViewJB);//绑定解绑
        final EditText editText = findViewById(R.id.activitymainEditTextKM);//输入框

        //设置解绑监听
        textView.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1) {
					MainActivity.this.LOGIN(解绑api, editText.getText().toString());//传入卡密和解绑api
				}
			});

        //设置登录监听
        button.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1) {
					MainActivity.this.LOGIN(登录api, editText.getText().toString());//传入卡密和登录api
				}
			});

        try {
            @SuppressLint("SdCardPath") String file = "/sdcard/km";//卡密路径
            if (new File(file).exists()) {
                try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {//此方法不用关闭文件流
                    String km = bufferedReader.readLine();//读取一行
                    editText.setText(km);//将读取的卡密写入编辑框
                }
            }
        } catch (Exception exception) {Log.d("Alice-", exception + "");}//打印异常返回

        ActivityCompat.requestPermissions(MainActivity.this, NEEDED_PERMISSIONS, 23);//调用ActivityCompat.requestPermissions方法获取权限
    }

    /**
     * jni 反射
     *
     * @param name jni 反射拿到执行路径
     */
    public void call(String name) {//jni 反射 不知道如何实现就不要乱动 具体实现方法请百度
        //jni反射调用，传递二进制路径
        if (MethodALL.isRoot()) {//调用封装方法获取设备环境
            MethodALL.Sshell("su -c " + name);//执行反射出的二进制路径
        } else {
            MethodALL.Sshell(name);//同上 框架执行二进制区别就是一个有su -c 一个没用
        }
        Log.w("Alice-", "ROOT:" + MethodALL.isRoot() + " cmd->" + name);//打印执行日志 有magisk就可以去直接看 这个也是方便测试用

    }

    private void LOGIN(String webapi, final String km) {//自定义登录方法 传入登录或者解绑api 和卡密
        String kami = km;//变量需要临时存储一下
        Base64New.setbase(basekey);//传入解密base64
        kami = Base64New.str2HexStr(Base64New.encodeBase64Str(kami));//对卡密进行加密
        String imei = Settings.System.getString(MainActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);//获取IMEI 这个准确来说不是IMEI 应该是手机ID 刷机就会变 真正的IMEI是系统一直都不会变的 因为有时间获取不到IMEI 所以采用这种方法 
        imei = Base64New.str2HexStr(Base64New.encodeBase64Str(imei));//对IMEI加密
        OkhttpUtil.okHttpPost(webapi + "&kami=" + kami + "&imei=" + imei + "&print=json", new CallBackUtil.CallBackString() {//post提交拿到返回json
				@Override
				public void onFailure(Call call, Exception e) {
					Toast.makeText(MainActivity.this, "网络异常:" + e, Toast.LENGTH_LONG).show();//网络异常处理 这就是OKhttp的好处 然后封装好了
				}

				@Override
				public void onResponse(String fh) {//post成功拿到返回值fh 对其进行解密
					final String jfh = Base64New.decodeBase64str(fh);//解密
					try {
						JSONObject jsonObject = new JSONObject(jfh);//将解密的数据转成json
						String code = jsonObject.getString("code");//是否登录成功
						String id = jsonObject.optString("id");//卡密ID
						String endtime = jsonObject.optString("end_time");//到期时间
						String token = jsonObject.optString("token");//校验密钥
						String date = jsonObject.optString("date");//服务器时间
						Long timee = jsonObject.optLong("time");//服务器时间戳
						if (code.equals("200")) {//登录成功 
							String jmcs = id + appkey + endtime + date;//对其md5进行校验
							if (token.equals(MethodALL.stringToMD5(jmcs))) {
								Long starts = System.currentTimeMillis();
								starts = starts / 10000;
								timee = timee / 10;
								if (String.valueOf(starts).equals(String.valueOf(timee))) {
									//这里就是登录成功后的处理 包括了获取到期时间 等等
									Toast.makeText(MainActivity.this, "到期时间：" + endtime, Toast.LENGTH_LONG).show();
									//这里放登录成功事件
									startService(intent);
									@SuppressLint("SdCardPath") FileWriter fileWriter = new FileWriter("/sdcard/km");//创建文件输入流
									fileWriter.write(km);//写入卡密
									fileWriter.close();//关闭输入流

								} else {
									Toast.makeText(MainActivity.this, "数据过期", Toast.LENGTH_LONG).show();
								}
							} else {
								Toast.makeText(MainActivity.this, "校验失败，数据异常", Toast.LENGTH_LONG).show();
							}
						} else {
							String ms = jsonObject.getString("msg");//这里打印其他数据返回结果 主要用于返回解绑成功的数据 还有其他的code不是200的
							Toast.makeText(MainActivity.this, ms, Toast.LENGTH_LONG).show();
						}
					} catch (Exception e) {
						Toast.makeText(MainActivity.this, jfh, Toast.LENGTH_LONG).show();
					}
				}
			});
    }

    /**
     * 显示更新公告
     */
    private void ShowGG() {//封装公告
        OkhttpUtil.okHttpGet(公告api, new CallBackUtil.CallBackString() {//okhttp协议post数据
				@Override
				public void onFailure(Call call, Exception e) {//返回失败结果
					Toast.makeText(MainActivity.this, "网络异常:" + e, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onResponse(String response) {//post成功
					Log.d("Alice-", "公告：" + response);//打印日志
					try {
						JSONObject jsonObject = new JSONObject(response);//将数据转换为json
						String Code = jsonObject.getString("code");//获取成功code
						String gg = jsonObject.getString("msg");//获取公告信息
						if (Code.equals("200")) {
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);//创建弹窗 这个弹窗你们可以自定义 我比较懒 用的系统的 丑不拉几不过能用就行 看自己DIY 
							builder.setTitle("软件公告");//设置弹窗标题
							builder.setMessage(gg);//设置公告内容 设置获取到的公告内容
							builder.setPositiveButton("确定", new DialogInterface.OnClickListener(){
									//设置点击事件
									@Override
									public void onClick(DialogInterface p1, int p2) {
										p1.dismiss();//取消弹窗 也可以是其他操作
									}
								});

							builder.setCancelable(true);//设置外部可以点击
							AlertDialog dialog = builder.create();//创建弹窗
							dialog.show();//显示弹窗
							init();//前面的main函数有是因为让小白能明白软件运行原理 如需使用更新和公告 就要把前面的init();注释掉
							//可能有点好奇为什么初四话放这 因为要让软件确定自己是最新版才加载配置 这样才能够防止简单的破解和其他的操作 比如断网进 然后又开 过更新检测 反正有一个思路 就可以，其他的看自己怎么写

						} else {
							Toast.makeText(MainActivity.this, "数据异常", Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});
    }
    private void UpAPP() {//更新软件

        OkhttpUtil.okHttpPost(更新api + "&ver=" + VERSIONCODE, new CallBackUtil.CallBackString() {//同样是post提交 需要提交的是ver=版本号 然后如果是最新就返回已经是最新版本 如果不是就返回更新公告和更新链接什么的 
				@Override
				public void onFailure(Call call, Exception e) {//异常处理
					Toast.makeText(MainActivity.this, "数据异常:" + e, Toast.LENGTH_SHORT).show();
				}

				@Override
				public void onResponse(String response) {//post成功 拿到返回string
					Log.d("Alice-", "最新版本：" + response);//打印最新版本 debug调试用
					try {
						JSONObject jsonObject = new JSONObject(response);//将字符串转换为json数据
						String Code = jsonObject.getString("code");//获取code
						if (Code.equals("200")) {
							String gg = jsonObject.getString("uplog");//获取更新公告
							final String ggurl = jsonObject.getString("upurl");//获取更新链接
							AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);//创建弹窗 
							builder.setTitle("软件更新");//设置标题
							builder.setMessage(gg);//设置更新公告

							builder.setPositiveButton("点击更新", new DialogInterface.OnClickListener(){//设置点击更新的操作
									@Override
									public void onClick(DialogInterface p1, int p2) {
										//以下就点击更新就跳转更新链接 一般在后台放蓝奏云链接，如果有直链更好
										Intent intent = new Intent();
										intent.setAction("android.intent.action.VIEW");
										intent.setData(Uri.parse(ggurl));
										startActivity(intent);
									}
								});

							builder.setCancelable(false);//设置不可取消 返回键也禁止用
							AlertDialog dialog = builder.create();//创建弹窗
							dialog.show();//显示弹窗
						} else {
							ShowGG();//意思就是更新判断为最新版本才显示公告 公告显示完了才加载配置
							String msg = jsonObject.getString("msg");//获取msg
							Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
						}
					} catch (Exception e) {
						Log.d("Alice-", "更新数据异常:" + e);
						Toast.makeText(MainActivity.this, "数据异常:" + e, Toast.LENGTH_SHORT).show();
					}
				}
			});
    }
}
