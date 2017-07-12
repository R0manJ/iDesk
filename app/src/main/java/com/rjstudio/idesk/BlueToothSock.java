package com.rjstudio.idesk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Created by r0man on 2017/7/11.
 */

public class BlueToothSock extends Thread {
    //作为客户端
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothDevice bluetoothDevice;
    private BluetoothSocket bluetoothSocket;
    private Context context;
    private ListView listView;
    private List<String> bluetoothAddress;
    private UUID uuid;
    private String MY_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private Handler handler;
    private OutputStream outputStream = null;
    private ReadInfoThread readInfoThread;
    private BlueToothSock b1;
    private String deviceAddress;

    public BlueToothSock(BluetoothSocket bluetoothSocket,Handler hanndler)
    {
        this.bluetoothSocket = bluetoothSocket;
        this.handler = hanndler;

    }
    public BlueToothSock(BluetoothDevice bluetoothDevice,BluetoothAdapter bluetoothAdapter) {
        this.bluetoothDevice = bluetoothDevice ;
        this.bluetoothAdapter = bluetoothAdapter;

    }



    public BlueToothSock(BluetoothAdapter bluetoothAdapter,String address,Handler hander)
    {
        this.bluetoothAdapter = bluetoothAdapter;
        this.deviceAddress = address;
        this.handler = hander;
        init();
    }

    private void init()
    {

        BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        try
        {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString(MY_UUID));
            b1 = new BlueToothSock(bluetoothSocket , handler);
            b1.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {

        try
        {
            bluetoothSocket.connect();
            InputStream inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();

            //开启读取信息的线程
            readInfoThread = new ReadInfoThread(inputStream,outputStream,handler);
            readInfoThread.start();
            //TODO : 如何套用try catch模板

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    //发送信息的方法
    public void sendOrder(String content)
    {
        byte[] order = content.getBytes();
        b1.readInfoThread.sendOreder(order);
    }


}

//一直读取信息
class ReadInfoThread extends Thread
{

    private Handler handler;
    private InputStream inputStream;
    private int readIndex;
    private byte[] buff;
    private String content;
    private Message msg;
    private OutputStream outputStream;

    public ReadInfoThread(InputStream inputStream , OutputStream outputStream,Handler handler) {
        this.inputStream = inputStream;
        this.handler = handler;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        buff = new byte[1024];
        content = null;
        while(true) {
            try {
                while ((readIndex = inputStream.read(buff)) != -1) {
                    content = new String(buff, readIndex, 0);
                }
                if (content.endsWith("#")) {
                    msg = new Message();
                    msg.obj = content;
                    handler.sendMessage(msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void sendOreder(byte[] order)
    {
        try
        {
            outputStream.write(order);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
