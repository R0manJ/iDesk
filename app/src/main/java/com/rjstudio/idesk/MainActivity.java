package com.rjstudio.idesk;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity{

    private BluetoothAdapter bluetoothAdapter;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("Handler", "handleMessage: "+msg.obj.toString());
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        initView();

    }

    private void initView() {
        ListView listView = (ListView) findViewById(R.id.lv_bluetooth_address);
        final List bluetoothAddress = getBluetoothDeviceAddress(BluetoothAdapter.getDefaultAdapter());
        listView.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,bluetoothAddress));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(),MainPager.class);
                intent.putExtra("address",bluetoothAddress.get(position).toString());
                startActivity(intent);
            }
        });
    }

    public List<String> getBluetoothDeviceAddress(BluetoothAdapter bluetoothAdapter)
    {
        Set<BluetoothDevice> bluetoothDeviceSet ;
        bluetoothDeviceSet = bluetoothAdapter.getBondedDevices(); // 添加权限
        List<String> bluetoothAddress = new ArrayList<>();
        for (BluetoothDevice bluetoothDevice : bluetoothDeviceSet)
        {
            bluetoothAddress.add(bluetoothDevice.getAddress());
        }
        return bluetoothAddress;
    }
}
