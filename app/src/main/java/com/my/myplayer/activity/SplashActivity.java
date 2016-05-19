package com.my.myplayer.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.my.myplayer.R;
import com.my.myplayer.service.PlayService;

public class SplashActivity extends Activity {

    private static final int START_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);    //去标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //去标题栏
        setContentView(R.layout.activity_splash);

        startService(new Intent(this, PlayService.class));  //在闪屏页面就启动服务

        handler.sendEmptyMessageDelayed(START_ACTIVITY,3000);   //闪屏时间3s
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case START_ACTIVITY:
                    //从闪屏页跳转到PagerSlidingTabStrip滑动页面
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                    break;
            }
        }
    };
}
