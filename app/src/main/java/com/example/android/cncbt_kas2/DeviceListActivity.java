package com.example.android.cncbt_kas2;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import java.util.Set;
import com.example.android.cnccommander.C0013R;

public class DeviceListActivity extends Activity {
    private static final boolean f1D = true;
    public static String EXTRA_DEVICE_ADDRESS = null;
    private static final String TAG = "DeviceListActivity";
    private BluetoothAdapter mBtAdapter;
    private OnItemClickListener mDeviceClickListener;
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
    private final BroadcastReceiver mReceiver;

    /* renamed from: com.example.android.cncbt_kas2.DeviceListActivity.1 */
    class C00101 implements OnItemClickListener {
        C00101() {
        }

        public void onItemClick(AdapterView<?> adapterView, View v, int arg2, long arg3) {
            DeviceListActivity.this.mBtAdapter.cancelDiscovery();
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);
            Intent intent = new Intent();
            intent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, address);
            DeviceListActivity.this.setResult(-1, intent);
            DeviceListActivity.this.finish();
        }
    }

    /* renamed from: com.example.android.cncbt_kas2.DeviceListActivity.2 */
    class C00112 extends BroadcastReceiver {
        C00112() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.bluetooth.device.action.FOUND".equals(action)) {
                BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                if (device.getBondState() != 12) {
                    DeviceListActivity.this.mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
            } else if ("android.bluetooth.adapter.action.DISCOVERY_FINISHED".equals(action)) {
                DeviceListActivity.this.setProgressBarIndeterminateVisibility(false);
                DeviceListActivity.this.setTitle(C0013R.string.select_device);
                if (DeviceListActivity.this.mNewDevicesArrayAdapter.getCount() == 0) {
                    DeviceListActivity.this.mNewDevicesArrayAdapter.add(DeviceListActivity.this.getResources().getText(C0013R.string.none_found).toString());
                }
            }
        }
    }

    /* renamed from: com.example.android.cncbt_kas2.DeviceListActivity.3 */
    class C00123 implements OnClickListener {
        C00123() {
        }

        public void onClick(View v) {
            DeviceListActivity.this.doDiscovery();
            v.setVisibility(8);
        }
    }

    public DeviceListActivity() {
        this.mDeviceClickListener = new C00101();
        this.mReceiver = new C00112();
    }

    static {
        EXTRA_DEVICE_ADDRESS = "device_address";
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(5);
        setContentView(C0013R.layout.device_list);
        setResult(0);
        ((Button) findViewById(C0013R.id.button_scan)).setOnClickListener(new C00123());
        this.mPairedDevicesArrayAdapter = new ArrayAdapter(this, C0013R.layout.device_name);
        this.mNewDevicesArrayAdapter = new ArrayAdapter(this, C0013R.layout.device_name);
        ListView pairedListView = (ListView) findViewById(C0013R.id.paired_devices);
        pairedListView.setAdapter(this.mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(this.mDeviceClickListener);
        ListView newDevicesListView = (ListView) findViewById(C0013R.id.new_devices);
        newDevicesListView.setAdapter(this.mNewDevicesArrayAdapter);
        newDevicesListView.setOnItemClickListener(this.mDeviceClickListener);
        registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.device.action.FOUND"));
        registerReceiver(this.mReceiver, new IntentFilter("android.bluetooth.adapter.action.DISCOVERY_FINISHED"));
        this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = this.mBtAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            findViewById(C0013R.id.title_paired_devices).setVisibility(0);
            for (BluetoothDevice device : pairedDevices) {
                this.mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
            return;
        }
        this.mPairedDevicesArrayAdapter.add(getResources().getText(C0013R.string.none_paired).toString());
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mBtAdapter != null) {
            this.mBtAdapter.cancelDiscovery();
        }
        unregisterReceiver(this.mReceiver);
    }

    private void doDiscovery() {
        setProgressBarIndeterminateVisibility(f1D);
        setTitle(C0013R.string.scanning);
        findViewById(C0013R.id.title_new_devices).setVisibility(0);
        if (this.mBtAdapter.isDiscovering()) {
            this.mBtAdapter.cancelDiscovery();
        }
        this.mBtAdapter.startDiscovery();
    }
}
