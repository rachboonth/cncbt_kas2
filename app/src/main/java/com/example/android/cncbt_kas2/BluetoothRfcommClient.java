package com.example.android.cncbt_kas2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothRfcommClient {
    private static final UUID MY_UUID;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_NONE = 0;
    private final boolean f0D;
    private final BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    public ConnectedThread mConnectedThread;
    private final Handler mHandler;
    private int mState;

    private class ConnectThread extends Thread {
        private final BluetoothDevice mmDevice;
        private final BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device) {
            this.mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(BluetoothRfcommClient.MY_UUID);
            } catch (IOException e) {
            }
            this.mmSocket = tmp;
        }

        public void run() {
            setName("ConnectThread");
            BluetoothRfcommClient.this.mAdapter.cancelDiscovery();
            try {
                this.mmSocket.connect();
                synchronized (BluetoothRfcommClient.this) {
                    BluetoothRfcommClient.this.mConnectThread = null;
                }
                BluetoothRfcommClient.this.connected(this.mmSocket, this.mmDevice);
            } catch (IOException e) {
                BluetoothRfcommClient.this.connectionFailed();
                try {
                    this.mmSocket.close();
                } catch (IOException e2) {
                }
                BluetoothRfcommClient.this.start();
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    public class ConnectedThread extends Thread {
        public final InputStream mmInStream;
        private final OutputStream mmOutStream;
        private final BluetoothSocket mmSocket;
        private BufferedReader reader;

        public ConnectedThread(BluetoothSocket socket) {
            this.mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }
            this.mmInStream = tmpIn;
            this.mmOutStream = tmpOut;
            this.reader = new BufferedReader(new InputStreamReader(this.mmInStream), 256);
        }

        public void run() {
            while (MainActivity.mRfcommClient.getState() == BluetoothRfcommClient.STATE_CONNECTED) {
                try {
                    String line = this.reader.readLine();
                    if (!line.contains("ok")) {
                        line = new StringBuilder(String.valueOf(line)).append("\n").toString();
                    }
                    BluetoothRfcommClient.this.mHandler.obtainMessage(BluetoothRfcommClient.STATE_CONNECTING, line).sendToTarget();
                } catch (IOException e) {
                    BluetoothRfcommClient.this.connectionLost();
                    return;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                this.mmOutStream.write(buffer);
            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                this.mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    static {
        MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    }

    public BluetoothRfcommClient(Context context, Handler handler) {
        this.f0D = false;
        this.mAdapter = BluetoothAdapter.getDefaultAdapter();
        this.mState = 0;
        this.mHandler = handler;
    }

    private synchronized void setState(int state) {
        this.mState = state;
        this.mHandler.obtainMessage(1, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return this.mState;
    }

    public synchronized void start() {
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        setState(0);
    }

    public synchronized void connect(BluetoothDevice device) {
        if (this.mState == STATE_CONNECTING && this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        try {
            this.mConnectThread = new ConnectThread(device);
            this.mConnectThread.start();
            setState(STATE_CONNECTING);
        } catch (Exception e) {
            connectionFailed();
        }
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        this.mConnectedThread = new ConnectedThread(socket);
        this.mConnectedThread.start();
        Message msg = this.mHandler.obtainMessage(4);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        if (this.mConnectThread != null) {
            this.mConnectThread.cancel();
            this.mConnectThread = null;
        }
        if (this.mConnectedThread != null) {
            this.mConnectedThread.cancel();
            this.mConnectedThread = null;
        }
        setState(0);
    }

    public void write(byte[] out) {
        synchronized (this) {
            if (this.mState != STATE_CONNECTED) {
                return;
            }
            ConnectedThread r = this.mConnectedThread;
            r.write(out);
        }
    }

    private void connectionFailed() {
        setState(0);
        Message msg = this.mHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "BT connection failed");
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        setState(0);
        Message msg = this.mHandler.obtainMessage(5);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "BlueTooth connection was lost");
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }
}
