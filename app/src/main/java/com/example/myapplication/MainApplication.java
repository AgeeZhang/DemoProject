package com.example.myapplication;

import android.app.Application;
import android.content.Intent;

import com.zcitc.updatelibrary.UpdateController;
import com.zcitc.updatelibrary.service.DownloadService;
import com.zcitc.updatelibrary.thread.UpdateThread;

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        UpdateController.getInstance().init(this);
    }

}
