package com.example.a10056.newvideozbdemo.manager;

import android.util.Log;
import android.view.SurfaceView;

import com.example.a10056.newvideozbdemo.MyApp;
import com.example.a10056.newvideozbdemo.content.NvContent;

import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

public class AgoraManager {
    private static AgoraManager agoraManager;
    private RtcEngine m_agoraMedia;//通话 进入直播间的API
    public boolean isMicDisable = false;
    private OnPartyListener mOnPartyListener;


    private AgoraManager() {

    }

    public static AgoraManager getInstance() {
        if (agoraManager == null) {
            agoraManager = new AgoraManager();
        }
        return agoraManager;
    }

    /**
     * 初始化RtcEngine
     */
    public void init() {
        //创建RtcEngine对象，mRtcEventHandler为RtcEngine的回调
        try {
            m_agoraMedia = RtcEngine.create(MyApp.instance, NvContent.APP_ID, mRtcEventHandler);
            //开启视频功能
            m_agoraMedia.enableVideo();
            //m_agoraMedia，设置为360P
            m_agoraMedia.setVideoProfile(Constants.VIDEO_PROFILE_360P, false);
            m_agoraMedia.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);//设置为通信模式（默认）
            m_agoraMedia.setDefaultAudioRoutetoSpeakerphone(false);
            m_agoraMedia.setVideoQualityParameters(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private IRtcEngineEventHandler mRtcEventHandler = new IRtcEngineEventHandler() {

        /**
         * 当获取用户uid的远程视频的回调  导入在线媒体流。
         */
        @Override
        public void onFirstRemoteVideoDecoded(int uid, int width, int height, int elapsed) {
            if (mOnPartyListener != null) {
                mOnPartyListener.onGetRemoteVideo(uid);
            }
        }


       // 删除导入的在线媒体流。
        @Override
        public void onUserOffline(int uid, int reason) { // Tutorial Step 7
            if (mOnPartyListener != null) {
                mOnPartyListener.onLeaveChannelSuccess();
            }
        }



        @Override
        public void onUserMuteVideo(final int uid, final boolean muted) { // Tutorial Step 10
            if (mOnPartyListener != null) {
                mOnPartyListener.onJoinChannelSuccess(uid,muted);
            }
        }
    };

    //设置本地视图
    public void setupLocalVideo(SurfaceView surfaceView) {
        Log.e("设置本地视屏", "");
        try {
            m_agoraMedia.setupLocalVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, 10));
        } catch (Exception e) {
            Log.e("biwei", "agoraManager setup erro " + e.toString());
        }
    }

    //选择远端视图
    public void setupRemoteVideo(SurfaceView surfaceView, int uid) {
        Log.e("设置远端视屏", "");
        try {
            m_agoraMedia.setupRemoteVideo(new VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        } catch (Exception e) {
            Log.e("biwei", "agoraManager setup erro " + e.toString());
        }
    }

    //预览
    public void preview() {
        ensurePreviewReady();
        m_agoraMedia.startPreview();
        Log.e("预览", "");
    }

    //停止预览
    public void stopPreview() {
        Log.e("停止预览", "");
        m_agoraMedia.stopPreview();
    }

    //准备完成
    public void ensurePreviewReady() {
        Log.e("确保 预览准备好了", "");
        enableVideo();
        m_agoraMedia.setVideoProfile(Constants.VIDEO_PROFILE_360P_9, false);
    }


    //启用视频模块。
    public void enableVideo() {
        m_agoraMedia.enableVideo();
    }


    //关闭视频模块。
    public void disableVideo() {
        m_agoraMedia.disableVideo();
    }


    //闭麦
    public void setMicDiaAbled(boolean micEnable) {
        isMicDisable = micEnable;
        if (m_agoraMedia != null) {//出现空指针
            m_agoraMedia.muteLocalAudioStream(micEnable);
        }

    }

    private long switchCameraTime = 0;

    //切换相机
    public void switchCamera() {
        if (System.currentTimeMillis() - switchCameraTime < 2000) {
            return;
        }
        switchCameraTime = System.currentTimeMillis();
        m_agoraMedia.switchCamera();
    }


    public void setSpeakerEnable(boolean enable) {
        if (m_agoraMedia != null) {
            m_agoraMedia.setEnableSpeakerphone(enable);
        }
    }

    public void destroyStreamId() {
    }

    //对静态变量的释放 否则退出应用之后 仍有可能(不清楚其内部代码是否有对application的引用)保留对Application对象的引用 造成内存泄漏
    public void onDestroy() {
        RtcEngine.destroy();
        m_agoraMedia = null;
    }


    //退出频道
    public void leaveChannel() {
        m_agoraMedia.leaveChannel();
    }


    //关闭相机
    public void closeCamera() {
        //m_agoraMedia.startPreview();
        m_agoraMedia.disableVideo();

    }

    //打开相机
    public void openCamera() {
        //m_agoraMedia.startPreview();
        m_agoraMedia.enableVideo();
    }

    //进入频道   //1. 在 App 服务器端生成的用于鉴权的 Token
    //2.标识通话的频道名称，
    //3.开发者需加入的任何附加信息。一般可设置为空字符串，或频道相关信息。该信息不会传递给频道内的其他用户
    //4.optionalUid用户id
    public AgoraManager joinChannel(String channel, int uid) {
        m_agoraMedia.joinChannel(null, channel, null, uid);
        return this;
    }


    public interface OnPartyListener {

        void onGetRemoteVideo(int uid);

        void onLeaveChannelSuccess();

        void onJoinChannelSuccess(int uid, boolean muted);
    }

    public AgoraManager setOnPartyListener(OnPartyListener listener) {
        mOnPartyListener = listener;
        return this;
    }
}
