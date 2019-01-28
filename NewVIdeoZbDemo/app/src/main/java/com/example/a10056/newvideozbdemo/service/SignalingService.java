package com.example.a10056.newvideozbdemo.service;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.example.a10056.newvideozbdemo.MainActivity;
import com.example.a10056.newvideozbdemo.MyApp;
import com.example.a10056.newvideozbdemo.content.NvContent;
import com.example.a10056.newvideozbdemo.receiver.SignalingReceiver;


import java.util.Arrays;
import java.util.Date;

import io.agora.AgoraAPIOnlySignal;
import io.agora.NativeAgoraAPI;

import static com.example.a10056.newvideozbdemo.utils.Md5Utils.md5hex;


/**
 * 信令系统的全局监听回调服务
 */
public class SignalingService extends Service {

    private static final String TAG = "SignalingService";

    public static final String KEY_COMMAND = "key_command";

    /**
     * 切换帐号，重新登录
     */
    public static final int COMMAND_RELOGIN = 1001;
    /**
     * 正常登录
     */
    public static final int COMMAND_LOGIN = 1002;

    private AgoraAPIOnlySignal m_agoraAPI;

    private LocalBroadcastManager localBroadcastManager;

    /**
     * 在已经登录的情况下，切换账号重新登录
     */
    private boolean reLogin;

    /**
     * 在已经登录的情况下，登录切换的新账号
     */
    private String newAccount;


    /**
     * 在已经登录的情况下，切换帐号重新登录
     */
    private void reLogin() {
        reLogin = true;
        m_agoraAPI.logout();
    }

    /**
     * 登录信令系统
     *
     * @param account 用户唯一标识
     */
    private void loginSignaling2(String account) {
        m_agoraAPI.login2(NvContent.APP_ID, account, "_no_need_token", 0, "", 30, 3);
    }


    public SignalingService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        final Intent intentReceiver = new Intent(this, SignalingReceiver.class);
        m_agoraAPI = MyApp.instance.m_agoraAPI;
        m_agoraAPI.callbackSet(new NativeAgoraAPI.CallBack() {

            /**
             * 离开频道回调
             * @param channelID 频道名
             * @param ecode 错误码
             */
            @Override
            public void onChannelLeaved(String channelID, int ecode) {
                super.onChannelLeaved(channelID, ecode);
                Log.d(TAG, "离开频道 channelID=" + channelID);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 其他用户加入频道回调
             * @param account 用户登录厂商 app 的账号。
             * @param uid 废弃字段
             */
            @Override
            public void onChannelUserJoined(String account, int uid) {
                super.onChannelUserJoined(account, uid);
                Log.d(TAG, "account" + account + "加入频道");

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 其他用户离开频道回调
             * @param account 用户登录厂商 app 的账号。
             * @param uid 废弃字段
             */
            @Override
            public void onChannelUserLeaved(String account, int uid) {
                super.onChannelUserLeaved(account, uid);
                Log.d(TAG, "account" + account + "离开频道");

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 获取频道内用户列表回调
             * @param accounts 用户账号列表
             * @param uids 废弃字段
             */
            @Override
            public void onChannelUserList(String[] accounts, int[] uids) {
                super.onChannelUserList(accounts, uids);
                Log.d(TAG, "频道用户 accounts = " + Arrays.toString(accounts));

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             *返回查询的用户数量回调
             * @param channelID 频道名
             * @param ecode 错误码
             * @param num 查询结果
             */
            @Override
            public void onChannelQueryUserNumResult(String channelID, int ecode, int num) {
                super.onChannelQueryUserNumResult(channelID, ecode, num);
                Log.d(TAG, "查询的用户数量 channelID=" + channelID + ",num=" + num);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 频道属性发生变化回调
             * @param channelID 频道名
             * @param name 属性名
             * @param value 属性值
             * @param type 变化类型：“update” ：更新 “del”: 删除 clear”: 全部删除
             */
            @Override
            public void onChannelAttrUpdated(String channelID, String name, String value, String type) {
                super.onChannelAttrUpdated(channelID, name, value, type);
                Log.d(TAG, "频道属性发生变化 channelID=" + channelID + ",name=" + name + ",value=" + value + ",type=" + type);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 收到呼叫邀请回调
             * @param channelID 频道名
             * @param account 用户登录厂商 app 的账号。
             * @param uid 废弃字段
             * @param extra
             */
            @Override
            public void onInviteReceived(String channelID, String account, int uid, String extra) {
                super.onInviteReceived(channelID, account, uid, extra);
                Log.d(TAG, "onInviteReceived : channelID=" + channelID + ",account=" + account + ",extra=" + extra);
                //todo  确保已经视频通话中的不会挤掉线
                Intent jump = new Intent(SignalingService.this, MainActivity.class);
                jump.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                jump.putExtra(MainActivity.KEY_SIGNALING, MainActivity.ON_INVITE_RECEIVED);
                jump.putExtra("channelID", channelID);
                jump.putExtra("account", account);
                jump.putExtra("uid", uid);
                jump.putExtra("extra", extra);
                startActivity(jump);
                //localBroadcastManager.sendBroadcast(intent);
            }

            /**
             * 远端已收到呼叫回调
             * @param channelID 频道名
             * @param account 用户登录厂商 app 的账号。
             * @param uid 废弃字段
             */
            @Override
            public void onInviteReceivedByPeer(String channelID, String account, int uid) {
                super.onInviteReceivedByPeer(channelID, account, uid);
                Log.d(TAG, "远端已收到呼叫回调 onInviteReceivedByPeer : channelID=" + channelID + ",account=" + account);

                Intent jump = new Intent(SignalingService.this, MainActivity.class);
                jump.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                jump.putExtra(MainActivity.KEY_SIGNALING, MainActivity.ON_INVITE_RECEIVED_BY_PEER);
                jump.putExtra("channelID", channelID);
                jump.putExtra("account", account);
                jump.putExtra("uid", uid);
                startActivity(jump);

                //localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 远端已接受呼叫回调
             * @param channelID 频道名
             * @param account 用户登录厂商 app 的账号。
             * @param uid 废弃字段
             * @param extra
             */
            @Override
            public void onInviteAcceptedByPeer(String channelID, String account, int uid, String extra) {
                super.onInviteAcceptedByPeer(channelID, account, uid, extra);
                Log.d(TAG, "远端已接受呼叫回调 onInviteAcceptedByPeer : channelID=" + channelID + ",account=" + account + ",extra=" + extra);

                Intent jump = new Intent(SignalingService.this, MainActivity.class);
                jump.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                jump.putExtra(MainActivity.KEY_SIGNALING, MainActivity.ON_INVITE_ACCEPTED_BY_PEER);
                jump.putExtra("channelID", channelID);
                jump.putExtra("account", account);
                jump.putExtra("uid", uid);
                jump.putExtra("extra", extra);
                startActivity(jump);

                //localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 对方已拒绝呼叫回调
             * @param channelID 频道名
             * @param account 用户登录厂商 app 的账号。
             * @param uid 废弃字段
             * @param extra
             */
            @Override
            public void onInviteRefusedByPeer(String channelID, String account, int uid, String extra) {
                super.onInviteRefusedByPeer(channelID, account, uid, extra);
                Log.d(TAG, "对方已拒绝呼叫回调 onInviteRefusedByPeer : channelID=" + channelID + ",account=" + account + ",extra=" + extra);

                Intent jump = new Intent(SignalingService.this, MainActivity.class);
                jump.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                jump.putExtra(MainActivity.KEY_SIGNALING, MainActivity.ON_INVITE_REFUSED_BY_PEER);
                jump.putExtra("channelID", channelID);
                jump.putExtra("account", account);
                jump.putExtra("uid", uid);
                jump.putExtra("extra", extra);
                startActivity(jump);

                //localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 呼叫失败回调
             * @param channelID 频道名
             * @param account 用户登录厂商 app 的账号。
             * @param uid 废弃字段
             * @param ecode
             * @param extra
             */
            @Override
            public void onInviteFailed(String channelID, String account, int uid, int ecode, String extra) {
                super.onInviteFailed(channelID, account, uid, ecode, extra);
                Log.d(TAG, "onInviteFailed : channelID=" + channelID + ",account=" + account + ",extra=" + extra);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 对方已结束呼叫回调
             * @param channelID 频道名
             * @param account 用户登录厂商app的账号
             * @param uid
             * @param extra
             */
            @Override
            public void onInviteEndByPeer(String channelID, String account, int uid, String extra) {
                super.onInviteEndByPeer(channelID, account, uid, extra);
                Log.d(TAG, "onInviteEndByPeer : channelID=" + channelID + ",account=" + account + ",extra=" + extra);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 本地已结束呼叫回调
             * @param channelID 频道名
             * @param account 用户登录厂商app的账号
             * @param uid 废弃字段
             */
            @Override
            public void onInviteEndByMyself(String channelID, String account, int uid) {
                super.onInviteEndByMyself(channelID, account, uid);
                Log.d(TAG, "onInviteEndByMyself : channelID=" + channelID + ",account=" + account);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            @Override
            public void onInviteMsg(String channelID, String account, int uid, String msgType, String msgData, String extra) {
                super.onInviteMsg(channelID, account, uid, msgType, msgData, extra);
                Log.d(TAG, "onInviteMsg : channelID=" + channelID + ",account=" + account + ",extra=" + extra + ",msgData=" + msgData);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }


            /**
             * 消息发送失败回调
             * @param messageID 消息ID
             * @param ecode 错误码
             */
            @Override
            public void onMessageSendError(String messageID, int ecode) {
                super.onMessageSendError(messageID, ecode);
                Log.d(TAG, "onMessageSendError : messageID=" + messageID);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 消息已发送成功回调
             * @param messageID 消息ID
             */
            @Override
            public void onMessageSendSuccess(String messageID) {
                super.onMessageSendSuccess(messageID);
                Log.d(TAG, "onMessageSendSuccess : messageID=" + messageID);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            @Override
            public void onMessageAppReceived(String msg) {
                super.onMessageAppReceived(msg);
                Log.d(TAG, "onMessageAppReceived : msg=" + msg);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 收到用户消息回调
             * @param account 用户登录厂商app的账号
             * @param uid 废弃接口
             * @param msg 消息正文
             */
            @Override
            public void onMessageInstantReceive(String account, int uid, String msg) {
                super.onMessageInstantReceive(account, uid, msg);
                Log.d(TAG, "onMessageInstantReceive : msg=" + msg + ",account=" + account);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }


            /**
             * 收到频道消息回调
             * @param channelID 频道名
             * @param account 用户登录厂商app的账号
             * @param uid 废弃字段
             * @param msg 消息正文
             */
            @Override
            public void onMessageChannelReceive(String channelID, String account, int uid, String msg) {
                super.onMessageChannelReceive(channelID, account, uid, msg);
                Log.d(TAG, "onMessageChannelReceive channelID=" + channelID + ",account=" + account + ",msg=" + msg);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 已打印日志回调
             * @param txt 一行日志的内容
             */
            @Override
            public void onLog(String txt) {
                super.onLog(txt);
                //Log.i(TAG, "onLog : log=" + txt);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            @Override
            public void onInvokeRet(String callID, String err, String resp) {
                super.onInvokeRet(callID, err, resp);
//                Log.d(TAG, "onInvokeRet name=" + name + ",reason=" + reason + ",resp=" + resp);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            //            @Override
//            public void onInvokeRet(String name, int ofu, String reason, String resp) {
//                super.onInvokeRet(name, ofu, reason, resp);
//                Log.d(TAG, "onInvokeRet name=" + name + ",reason=" + reason + ",resp=" + resp);
//
//                localBroadcastManager.sendBroadcast(intentReceiver);
//            }

            @Override
            public void onMsg(String from, String t, String msg) {
                super.onMsg(from, t, msg);
                Log.d(TAG, "onMsg : msg=" + msg + ",from=" + from);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 已获取用户属性查询结果回调
             * @param account 用户登录厂商app的账号
             * @param name 属性名
             * @param value 所有属性的json值
             */
            @Override
            public void onUserAttrResult(String account, String name, String value) {
                super.onUserAttrResult(account, name, value);
                Log.d(TAG, "onUserAttrResult account=" + account + ",name=" + name + ",value=" + value);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 已获取所有用户属性查询结果回调
             * @param account 用户登录厂商app的账号
             * @param value 所有属性的json值
             */
            @Override
            public void onUserAttrAllResult(String account, String value) {
                super.onUserAttrAllResult(account, value);
                Log.d(TAG, "onUserAttrAllResult account=" + account + ",value=" + value);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            @Override
            public void onError(String name, int ecode, String desc) {
                super.onError(name, ecode, desc);
                Log.d(TAG, "onError name=" + name + ",desc=" + desc);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            @Override
            public void onQueryUserStatusResult(String name, String status) {
                super.onQueryUserStatusResult(name, status);
                Log.d(TAG, "onQueryUserStatusResult name=" + name + ",status=" + status);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            @Override
            public void onDbg(String a, byte[] b) {
                super.onDbg(a, b);
                localBroadcastManager.sendBroadcast(intentReceiver);
            }


            //            @Override
//            public void onDbg(String a, String b) {
//                super.onDbg(a, b);
//
//                localBroadcastManager.sendBroadcast(intentReceiver);
//            }

            /**
             * 连接丢失回调
             * @param nretry 当前重连的次数
             */
            @Override
            public void onReconnecting(int nretry) {
                super.onReconnecting(nretry);
                Log.d(TAG, "onReconnecting nretry=" + nretry);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 重连成功回调
             * @param fd 仅供Agora内部使用
             */
            @Override
            public void onReconnected(int fd) {
                super.onReconnected(fd);
                Log.d(TAG, "onReconnected  重连成功回调");

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 登录成功回调
             * @param uid
             * @param fd 仅供Agora内部使用
             */
            @Override
            public void onLoginSuccess(int uid, int fd) {
                super.onLoginSuccess(uid, fd);
                Log.d(TAG, " 登录成功回调 onLoginSuccess uid=" + uid);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 退出登录回调
             * @param ecode 错误码
             */
            @Override
            public void onLogout(int ecode) {
                super.onLogout(ecode);
                Log.d(TAG, " 退出登录回调 onLogout ecode=" + ecode);
                if (reLogin) {
                    reLogin = false;
                    Log.d(TAG, "切换帐号 开始再登录");
                    loginSignaling2(newAccount);
                }
                //localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 登录失败回调
             * @param ecode 错误码
             */
            @Override
            public void onLoginFailed(int ecode) {
                super.onLoginFailed(ecode);
                Log.d(TAG, " 登录失败回调 onLoginFailed ecode=" + ecode);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 加入频道回调
             * @param channelID 频道名
             */
            @Override
            public void onChannelJoined(String channelID) {
                super.onChannelJoined(channelID);
                Log.d(TAG, " 加入频道回调 onChannelJoined channelID=" + channelID);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }

            /**
             * 加入频道失败回调
             * @param channelID 频道名
             * @param ecode 错误码
             */
            @Override
            public void onChannelJoinFailed(String channelID, int ecode) {
                super.onChannelJoinFailed(channelID, ecode);
                Log.d(TAG, " 加入频道失败回调 onChannelJoinFailed channelID=" + channelID);

                localBroadcastManager.sendBroadcast(intentReceiver);
            }
        });
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra(KEY_COMMAND, -1);
        switch (command) {
            case COMMAND_RELOGIN:
                newAccount = intent.getStringExtra("newAccount");
                reLogin();
                break;
            case COMMAND_LOGIN:
                String account = intent.getStringExtra("account");
                loginSignaling2(account);
                break;
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "signalingService onDestroy");
        //todo  do something

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


}
