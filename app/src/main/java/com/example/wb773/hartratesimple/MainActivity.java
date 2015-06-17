package com.example.wb773.hartratesimple;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private Handler mHandler;

    public static final int OPTION_MENU_SELECT_GET＿DEVICE = 0;
    private static final long SCAN_PERIOD = 10000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初期設定
        reLoadDeviceInfo();

    }




    //プリファレンスを読み込んで保存されているデバイスを表示する
    private void reLoadDeviceInfo(){

        //プリファレンスの読み込み
        SharedPreferences pref = this.getSharedPreferences( "deviceinfo", this.MODE_PRIVATE );

        String name = pref.getString("name", getString(R.string.no_device));//Key,DefaultValue
        String address = pref.getString("address", "");


        //TextViewの取得
        TextView textViewId = (TextView)findViewById(R.id.current_device_id);
        TextView textViewAddress = (TextView)findViewById(R.id.current_device_address);

        //TextViewの変更
        textViewId.setText(name);
        textViewAddress.setText(address);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.add(0, OPTION_MENU_SELECT_GET＿DEVICE, 1, getString(R.string.optionmenu_get_device));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {

            case OPTION_MENU_SELECT_GET＿DEVICE:

                initBLE();

                //
                //処理：メッセージダイアログの表示
                //
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("リストのサンプル")
                        .setAdapter(mLeDeviceListAdapter, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //選択したアイテムをトースト表示
                                Toast.makeText(
                                        MainActivity.this,
                                        "選択したアイテム: " + which + " , " + mLeDeviceListAdapter.getItem(which),
                                        Toast.LENGTH_LONG).show();
                            }
                        });


                dialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initBLE(){

        // BLEのサポートを確認する
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        //BluetoothManagerの準備
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Bluetoothのデバイスがサポートされていない場合、終了する
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        // リストビューの初期化
        mLeDeviceListAdapter = new LeDeviceListAdapter();
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                invalidateOptionsMenu();
            }
        }, SCAN_PERIOD);

        mBluetoothAdapter.startLeScan(mLeScanCallback);

    }


    // Adapter for holding devices found through scanning.
    private class LeDeviceListAdapter extends BaseAdapter {
        private ArrayList<BluetoothDevice> mLeDeviceList;
        private LayoutInflater mInflator;

        //コンストラクタ
        public LeDeviceListAdapter() {
            super();
            mLeDeviceList = new ArrayList<BluetoothDevice>();
            mInflator = MainActivity.this.getLayoutInflater();
        }

        public void addDevice(BluetoothDevice device) {
            //存在しない場合追加する
            if(!mLeDeviceList.contains(device)) {
                mLeDeviceList.add(device);
            }
        }

        public BluetoothDevice getDevice(int position) {
            return mLeDeviceList.get(position);
        }

        public void clear() {
            mLeDeviceList.clear();
        }

        @Override
        public int getCount() {
            return mLeDeviceList.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeDeviceList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflator.inflate(R.layout.listitem_device, null);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeDeviceList.get(i);
            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0)
                viewHolder.deviceName.setText(deviceName);
            else
                viewHolder.deviceName.setText(R.string.unknown_device);
            viewHolder.deviceAddress.setText(device.getAddress());

            return view;
        }
    }

    static class ViewHolder {
        TextView deviceName;
        TextView deviceAddress;
    }


    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mLeDeviceListAdapter.addDevice(device);
                            mLeDeviceListAdapter.notifyDataSetChanged();
                        }
                    });
                }
            };
}
