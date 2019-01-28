package com.example.a10056.newvideozbdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import io.agora.AgoraAPIOnlySignal;

public class CallActivity extends AppCompatActivity {

    String account;
    EditText etCallAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        account = getIntent().getStringExtra("account");

        etCallAccount = (EditText) findViewById(R.id.et_call);

    }

    public void click(View view) {
        switch (view.getId()) {
            case R.id.btn_call:
                call(account, etCallAccount.getText().toString().trim());
                break;
        }
    }


    /**
     * 发起呼叫
     *
     * @param myAccount   我的唯一帐号标识
     * @param peerAccount 对方的唯一帐号标识
     *                    先发送呼叫邀请，对方接受后再加入频道
     */
    private void call(String myAccount, String peerAccount) {
        //约定频道名称id 为 主叫方+被叫方的 拼接吧
        String channelName = myAccount + peerAccount;
        AgoraAPIOnlySignal m_agoraAPI = MyApp.instance.m_agoraAPI;
        m_agoraAPI.channelInviteUser(channelName, peerAccount, 0);// (int)Long.parseLong(peerUid));
    }
}
