package com.example.a10056.newvideozbdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.example.a10056.newvideozbdemo.service.SignalingService;

public class LoginActivity extends AppCompatActivity {

    EditText account, pwd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        account = (EditText) findViewById(R.id.et_account);
        pwd = (EditText) findViewById(R.id.et_pwd);

    }


    public void click(View view) {
        if (TextUtils.isEmpty(account.getText().toString().trim())) return;
        setTitle(account.getText().toString().trim());
        /**
         * 登录自己服务成功后回调里的逻辑
         */
        Intent intentSignaling = new Intent(this, SignalingService.class);
        if (MyApp.instance.m_agoraAPI.isOnline() == 1) {//已经登录在线,先登出原帐号
            intentSignaling.putExtra(SignalingService.KEY_COMMAND, SignalingService.COMMAND_RELOGIN);
            intentSignaling.putExtra("newAccount", account.getText().toString().trim());
        } else {
            intentSignaling.putExtra(SignalingService.KEY_COMMAND, SignalingService.COMMAND_LOGIN);
            intentSignaling.putExtra("account", account.getText().toString().trim());
        }
        startService(intentSignaling);

        Intent intent = new Intent(this,CallActivity.class);
        intent.putExtra("account", account.getText().toString().trim());
        startActivity(intent);
        finish();
    }
}
