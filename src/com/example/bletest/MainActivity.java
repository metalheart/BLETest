package com.example.bletest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements BluetoothAdapter.LeScanCallback {

    private DeviceListAdapter mDeviceListAdapter = null;
    private final ArrayList<BLEDevice> mDeviceList = new ArrayList<BLEDevice>();
    private BluetoothAdapter mBluetoothAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(this, "No bluetooth LE, closing...", Toast.LENGTH_SHORT)
                    .show();
            finish();
            return;
        }

        mDeviceListAdapter = new DeviceListAdapter(this, mDeviceList);
        ListView listView = (ListView)findViewById(R.id.device_list);
        listView.setAdapter(mDeviceListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                connectDevice(mDeviceListAdapter.getItem(i));
            }
        });

        mBluetoothAdapter.startLeScan(this);
    }

    private void connectDevice(BLEDevice bluetoothDevice) {
        bluetoothDevice.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onLeScan(final BluetoothDevice bluetoothDevice, int i, byte[] bytes) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceList.add(new BLEDevice(bluetoothDevice));
                mDeviceListAdapter.notifyDataSetChanged();
            }
        });
    }

    private class BLEDevice {
        private final BluetoothDevice mDevice;
        private BluetoothGatt mGatt = null;

        BLEDevice(BluetoothDevice device) {
            super();
            mDevice = device;
        }

        public void connect() {
            mGatt = mDevice.connectGatt(MainActivity.this, false,
                    new BluetoothGattCallback() {
                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt,
                                                            int status, int newState) {
                        }
            });
        }
    }
    //Device list adapter
    public class DeviceListAdapter extends ArrayAdapter<BLEDevice> {

        private final List<BLEDevice> list;
        private final Activity context;

        public DeviceListAdapter(Activity context, List<BLEDevice> list) {
            super(context, R.layout.device_item_layout, list);
            this.context = context;
            this.list = list;
        }

        class ViewHolder {
            protected TextView name;
            protected TextView address;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            if (convertView == null) {
                LayoutInflater inflator = context.getLayoutInflater();
                view = inflator.inflate(R.layout.device_item_layout, null);
                final ViewHolder viewHolder = new ViewHolder();
                viewHolder.name = (TextView) view.findViewById(R.id.device_item_name);
                viewHolder.address = (TextView) view.findViewById(R.id.device_item_address);

                view.setTag(viewHolder);
                //viewHolder.checkbox.setTag(list.get(position));
            } else {
                view = convertView;
               //((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
            }
            ViewHolder holder = (ViewHolder) view.getTag();

            holder.name.setText(list.get(position).mDevice.getName());
            holder.address.setText(list.get(position).mDevice.getAddress());

            return view;
        }
    }
    
}
