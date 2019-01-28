package com.example.a10056.newvideozbdemo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.a10056.newvideozbdemo.manager.AgoraManager;
import com.example.a10056.newvideozbdemo.utils.DisplayUtil;
import com.example.a10056.newvideozbdemo.weight.SelfDialog;

import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.RtcEngine;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AgoraManager.OnPartyListener {

    /**
     * 收到呼叫邀请回调
     */
    public static final int ON_INVITE_RECEIVED = 1001;
    /**
     * 远端已收到呼叫回调
     */
    public static final int ON_INVITE_RECEIVED_BY_PEER = 1002;
    /**
     * 远端已接受呼叫回调
     */
    public static final int ON_INVITE_ACCEPTED_BY_PEER = 1003;
    /**
     * 对方已拒绝呼叫回调
     */
    public static final int ON_INVITE_REFUSED_BY_PEER = 1004;

    public static final String KEY_SIGNALING = "key_signaling";

    private String channelID;
    /**
     * 主叫人帐号
     */
    private String accountFrom;
    /**
     * 被叫人帐号
     */
    private String accountTo;


    private FrameLayout frameLayout1;
    private FrameLayout frameLayout2;
    private ImageView imageView1;
    private ImageView imageView2;
    private ImageView tvCamera;
    private TextView tvOn;
    private SurfaceView localSurface;
    private SurfaceView remoteSurface;
    private boolean isShow;


    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,
                        permissions.toArray(new String[0]),
                        100);
                return false;
            }
        }
        return true;
    }


    //声网sdk服务回调     参数
    private void function(Intent intent) {
        int functionType = intent.getIntExtra(KEY_SIGNALING, -1);
        switch (functionType) {
            case ON_INVITE_RECEIVED:
                channelID = intent.getStringExtra("channelID");
                accountFrom = intent.getStringExtra("account");
                break;
            case ON_INVITE_RECEIVED_BY_PEER:
                channelID = intent.getStringExtra("channelID");
                accountTo = intent.getStringExtra("account");
                imageView1.setVisibility(View.GONE);
                break;
            case ON_INVITE_ACCEPTED_BY_PEER:
                channelID = intent.getStringExtra("channelID");
                imageView1.setVisibility(View.GONE);
                if (hasPermission()) {
                    startView();
                    AgoraManager.getInstance().disableVideo();
                    AgoraManager.getInstance().enableVideo();
                }
                break;
            case ON_INVITE_REFUSED_BY_PEER:
                accountTo = intent.getStringExtra("account");
                Toast.makeText(this, accountTo + "已经拒绝通话", Toast.LENGTH_LONG).show();
                finish();
                break;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //如果判断有刘海屏不让填充到状态栏
        if (DisplayUtil.hasNotchScreen(this)) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.activity_main);
        initView();
        function(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        function(intent);
    }


    private void initView() {
        frameLayout1 = (FrameLayout) findViewById(R.id.remote_video_view_container);
        frameLayout2 = (FrameLayout) findViewById(R.id.local_video_view_container);
        imageView1 = (ImageView) findViewById(R.id.btn_answer);   //接听
        imageView2 = (ImageView) findViewById(R.id.btn_hang_up);   //挂断
        tvCamera = (ImageView) findViewById(R.id.is_camera);
        tvOn = (TextView) findViewById(R.id.tv_on);
        imageView1.setOnClickListener(this);
        imageView2.setOnClickListener(this);
        tvCamera.setOnClickListener(this);
        tvOn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /**
     * 返回时退出频道
     */
    @Override
    public void onBackPressed() {
        AgoraManager.getInstance().leaveChannel();
        finish();
    }

    private boolean isHavePermissionCamra = false;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                for (int i = 0; i < permissions.length; i++) {
                    if (permissions[i].equals(Manifest.permission.CAMERA) && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        isHavePermissionCamra = true;
                        final SelfDialog selfDialog = new SelfDialog(this, true);
                        selfDialog.setTitle("");
                        selfDialog.setMessage(getString(R.string.camera_permission_context));
                        selfDialog.setYesOnclickListener(getString(R.string.positive_button_live_level_limit), new SelfDialog.onYesOnclickListener() {
                            @Override
                            public void onYesClick() {
                                selfDialog.cancel();
                            }
                        });
                        selfDialog.show();
                        break;
                    }
                }
                startView();
                break;
            default:
                break;
        }
    }

    private void startView() {
        AgoraManager.getInstance().setMicDiaAbled(false);
        AgoraManager.getInstance().setSpeakerEnable(false);
        AgoraManager.getInstance().joinChannel(channelID, 0);
        initVideoView();
    }


    /**
     * 初始化视频视图
     */
    private void initVideoView() {
        localSurface = RtcEngine.CreateRendererView(getBaseContext()); //创建渲染视图
        localSurface.setZOrderOnTop(false);
        localSurface.setZOrderMediaOverlay(false);
        AgoraManager.getInstance().preview();
        AgoraManager.getInstance().setupLocalVideo(localSurface);
        AgoraManager.getInstance().setOnPartyListener(this);
        addLocalSurface();
    }


    //本地视图
    private void addLocalSurface() {
        try {
            frameLayout1.removeAllViews();
            frameLayout1.addView(localSurface);
        } catch (Exception e) {
        }
    }


    //远端视图
    private void addRemoteSurface() {
        try {
            frameLayout2.removeAllViews();
            frameLayout2.addView(remoteSurface);
        } catch (Exception e) {
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_answer:   //接听
                MyApp.instance.m_agoraAPI.channelInviteAccept(channelID, accountFrom, 0, "");
                imageView1.setVisibility(View.GONE);
                if (hasPermission()) {
                    startView();
                }
                break;
            case R.id.btn_hang_up:   //挂断
                MyApp.instance.m_agoraAPI.channelInviteRefuse(channelID, accountFrom, 0, "");
                AgoraManager.getInstance().leaveChannel();
                finish();
                break;
            case R.id.is_camera:
                if (isHavePermissionCamra) {
                    return;
                }
                AgoraManager.getInstance().switchCamera();
                break;
            case R.id.tv_on:
                if (!isShow) {

                } else {

                }
                isShow = !isShow;
                break;
        }
    }


    /**
     * 当获取用户uid的远程视频的回调  导入在线媒体流。  完成远端视频首帧解码回调。
     */
    @Override
    public void onGetRemoteVideo(final int uid) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (remoteSurface == null) {
                    remoteSurface = RtcEngine.CreateRendererView(getBaseContext());
                }
                remoteSurface.setZOrderOnTop(true);
                remoteSurface.setZOrderMediaOverlay(true);
                remoteSurface.setTag(uid);
                AgoraManager.getInstance().setupRemoteVideo(remoteSurface, uid);
                addRemoteSurface();
            }
        });
    }


    // 远端用户（通信模式）/主播（直播模式）离开当前频道回调。
    @Override
    public void onLeaveChannelSuccess() {

    }



    // 远端用户暂停/重新发送视频流回调。
    @Override
    public void onJoinChannelSuccess(final int uid, final boolean muted) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                remoteSurface = (SurfaceView) frameLayout2.getChildAt(0);
                Object tag = remoteSurface.getTag();
                if (tag != null && (Integer) tag == uid) {
                    remoteSurface.setVisibility(muted ? View.GONE : View.VISIBLE);
                }
            }
        });

    }
}
