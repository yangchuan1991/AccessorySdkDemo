package com.yangc.accessory;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wiseasy.communication.accessory.AccessoryCommunication;
import com.wiseasy.communication.listener.OpenListener;
import com.wiseasy.communication.listener.ReciverMessageListener;

public class MainActivity extends Activity implements View.OnClickListener {
    private TextView mLog;
    private EditText mMessage;
    private Button mSend, receive, open;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initUsb();
    }

    private void initView() {
        mLog = findViewById(R.id.log);
        mMessage = findViewById(R.id.message);
        open = findViewById(R.id.open);
        receive = findViewById(R.id.receive);
        mSend = findViewById(R.id.send);
        receive.setEnabled(false);
        mSend.setEnabled(false);
        open.setOnClickListener(this);
        receive.setOnClickListener(this);
        mSend.setOnClickListener(this);
    }

    private AccessoryCommunication accessoryCommunication;

    private void initUsb() {
        accessoryCommunication = AccessoryCommunication.getInstance(this);
        openAccessory();
    }

    private void openAccessory(){
        accessoryCommunication.openCommunication(new OpenListener() {
            @Override
            public void Success() {
                receive.setEnabled(true);
                mSend.setEnabled(true);
                Toast.makeText(MainActivity.this, "usb连接成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void Failed(String msg) {
                Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open:
                openAccessory();
                break;

            case R.id.send:
                String message = mMessage.getText().toString().trim();
                if(TextUtils.isEmpty(message)){
                    Toast.makeText(MainActivity.this, "请输入要发送的消息", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(accessoryCommunication.sendMessage(message.getBytes()) == 0){
                    Toast.makeText(MainActivity.this, "消息发送成功", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(MainActivity.this, "消息发送失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.receive:
                accessoryCommunication.receiveMessage(new ReciverMessageListener() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        String msg = new String(bytes);
                        if(!TextUtils.isEmpty(msg)){
                            mLog.setText(msg);
                        }
                    }

                    @Override
                    public void onFailed(String msg) {
                        Toast.makeText(MainActivity.this, "消息接收失败", Toast.LENGTH_SHORT).show();
                    }
                });
                break;
        }

    }
}
