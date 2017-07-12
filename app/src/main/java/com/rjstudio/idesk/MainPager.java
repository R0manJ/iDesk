package com.rjstudio.idesk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

/**
 * Created by r0man on 2017/7/11.
 */

public class MainPager extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    Handler handler = new Handler()
    {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private Intent intent;
    private String address;
    private BlueToothSock blueToothSock;
    private boolean isBreatheMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.idesk_main);
        initVariable();
        initView();


    }

    private void initVariable()
    {
        intent = getIntent();
        address = intent.getStringExtra("address");
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        blueToothSock = new BlueToothSock(bluetoothAdapter,address,handler);
    }
    private void initView()
    {
        TextView tv_time = (TextView) findViewById(R.id.textView4);
        Switch s_led1 = (Switch) findViewById(R.id.switch1);
        Switch s_led2 = (Switch) findViewById(R.id.switch2);
        Switch s_led3 = (Switch) findViewById(R.id.switch3);
        Switch s_led4 = (Switch) findViewById(R.id.switch4);
        Switch s_led5 = (Switch) findViewById(R.id.switch5);
        Switch s_lamp = (Switch) findViewById(R.id.switch6);

        s_led1.setOnCheckedChangeListener(this);
        s_led2.setOnCheckedChangeListener(this);
        s_led3.setOnCheckedChangeListener(this);
        s_led4.setOnCheckedChangeListener(this);
        s_led5.setOnCheckedChangeListener(this);
        s_lamp.setOnCheckedChangeListener(this);

        CheckBox ck_breathe = (CheckBox) findViewById(R.id.ck_breathe);
        ck_breathe.setOnCheckedChangeListener(this);
    }



    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int selectViewId = buttonView.getId();
        switch (selectViewId)
        {
            case R.id.switch1:
                sendOrder(isChecked,"11","10");
                break;
            case R.id.switch2:
                sendOrder(isChecked,"21","20");
                break;
            case R.id.switch3:
                sendOrder(isChecked,"31","30");
                break;
            case R.id.switch4:
                sendOrder(isChecked,"41","40");
                break;
            case R.id.switch5:
                sendOrder(isChecked,"51","50");
                break;
            case R.id.switch6:
                sendOrder(isChecked,"60","61");
                break;
            case R.id.ck_breathe:
                isBreatheMode = isChecked;
                break;

        }


    }
    public void sendOrder(boolean isChecked,String positiveOrder,String negativeOrder)
    {
        if (isBreatheMode)
        {
            positiveOrder += "1#";
            negativeOrder += "1#";
        }
        else
        {
            positiveOrder += "0#";
            negativeOrder += "0#";
        }
        Log.d("---------", "sendOrder: "+positiveOrder);
        if (isChecked)
        {
            blueToothSock.sendOrder(positiveOrder);
        }
        else
        {
            blueToothSock.sendOrder(negativeOrder);
        }
    }
}
