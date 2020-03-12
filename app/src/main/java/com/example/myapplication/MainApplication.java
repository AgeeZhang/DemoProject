package com.example.myapplication;

import android.content.Intent;

import com.jess.arms.base.BaseApplication;
import com.zcitc.updatelibrary.BaseUpdateController;

public class MainApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        UpdateController.getInstance().init(this, DownloadService.class);
    }

}
