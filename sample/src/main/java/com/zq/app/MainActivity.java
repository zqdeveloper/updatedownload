package com.zq.app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.utils.CrashHandler;
import com.zq.update.UpdateBuilder;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CrashHandler.getInstance().init(this);
    }

    public void onclick(View view) {
        UpdateBuilder.with(this)
                .setTitle("新消息来了")
                .setApkUrl("https://0e441c6f3d592181a7c382673941cf9f.dd.cdntips.com/download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk?mkey=5bd56a176fc1c7ac&f=07b4&cip=111.193.225.89&proto=https")
                .startDownLoad();
    }
}
