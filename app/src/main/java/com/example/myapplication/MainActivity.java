package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.zcitc.updatelibrary.DownloadTags;
import com.zcitc.updatelibrary.UpdateController;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        intent.putExtra(DownloadTags.MUST_BE_UPDATED,107);
        intent.putExtra(DownloadTags.NEW_VERSION,"1.2.3");
        intent.putExtra(DownloadTags.APK_URL,"https://dldir1.qq.com/weixin/android/weixin7010android1580.apk");
        intent.putExtra(DownloadTags.FILE_MD5,"414ead6cfed19db527894f05ace44702");
        intent.putExtra(DownloadTags.ICON_ID,R.mipmap.ic_launcher);
        UpdateController.getInstance().NeedUpdate(this,intent);
        UpdateController.getInstance().showDialog();
    }
}
