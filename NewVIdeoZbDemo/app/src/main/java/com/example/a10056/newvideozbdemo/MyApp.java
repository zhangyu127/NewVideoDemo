package com.example.a10056.newvideozbdemo;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;

import com.example.a10056.newvideozbdemo.content.NvContent;
import com.example.a10056.newvideozbdemo.manager.AgoraManager;
import com.example.a10056.newvideozbdemo.service.SignalingService;

import io.agora.AgoraAPIOnlySignal;

public class MyApp extends Application {

    public AgoraAPIOnlySignal m_agoraAPI;
    public static MyApp instance;


    @Override
    public void onCreate() {
        super.onCreate();
        instance=this;
        AgoraManager.getInstance().init();
        m_agoraAPI = AgoraAPIOnlySignal.getInstance(this, NvContent.APP_ID);

        Intent intentSignalingService = new Intent(this, SignalingService.class);
        startService(intentSignalingService);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        AgoraManager.getInstance().onDestroy();
    }
}
