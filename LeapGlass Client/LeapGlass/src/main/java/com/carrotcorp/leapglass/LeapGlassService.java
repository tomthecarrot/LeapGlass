package com.carrotcorp.leapglass;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * This is the background service of the Glass application for the LeapGlass Project.
 * Code by Thomas Suarez, Chief Engineer @ CarrotCorp
 */
public class LeapGlassService extends Service {
    private String appName; // the name of this app
    private ConnectThread connection; // bluetooth connection thread
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // well-known Bluetooth SPP UUID
    private BluetoothAdapter adapter; // bluetooth hardware object
    private String myPC = ""; // preferred "my PC" bluetooth name
    // TODO: type the Bluetooth name of your PC into the variable

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // Get variables from previous activity
        appName = intent.getStringExtra("appName");

        Log.i(appName, "service started!");

        IntentFilter brFilter1 = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        IntentFilter brFilter2 = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        IntentFilter brFilter3 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        IntentFilter brFilter4 = new IntentFilter(LeapGlassMain.ACTION_BT_SCAN);
        IntentFilter brFilter5 = new IntentFilter(LeapGlassMain.ACTION_BT_CONNECT);
        IntentFilter brFilter6 = new IntentFilter(LeapGlassMain.ACTION_BT_CANCEL);

        registerReceiver(br, brFilter1);
        registerReceiver(br, brFilter2);
        registerReceiver(br, brFilter3);
        registerReceiver(br, brFilter4);
        registerReceiver(br, brFilter5);
        registerReceiver(br, brFilter6);

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(appName, "service destroyed!");
        unregisterReceiver(br);
    }

    /**
     * Starts Bluetooth radio and discovery of devices
     */
    private void startBluetooth() {
        Log.i(appName, "starting bluetooth...");
        setDesc("Starting Bluetooth...");

        // Show a ListView to let the user choose the correct device
        //listView.setVisibility(View.VISIBLE);

        // Get the Bluetooth adapter
        adapter = BluetoothAdapter.getDefaultAdapter();
        // Enable Bluetooth
        adapter.enable();
        // Start device discovery
        adapter.startDiscovery();

        // If the preferred "my PC" has been set
        // and is already paired, prevent waiting
        // by adding it to the list.
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        if (devices != null) {
            for (BluetoothDevice device : devices) {
                if (device.getName().equals(myPC)) {
                    Intent sendIntent = new Intent(LeapGlassMain.ACTION_PUT_DEVICE);
                    sendIntent.putExtra("device", device);
                    sendBroadcast(sendIntent);
                }
            }
        }
    }

    /**
     * Handles Bluetooth connection actions AND LeapGlassService actions
     */
    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Get the device name/address and add it to the list
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Intent sendIntent = new Intent(LeapGlassMain.ACTION_PUT_DEVICE);
                sendIntent.putExtra("device", device);
                sendBroadcast(sendIntent);

                Log.i(appName, "bluetooth device found: " + device.getName());
            }
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

                // Remove the device list from the screen as it is no longer needed
                //listView.setVisibility(View.GONE);

                // Wait for the server to send a start command. See ConnectThread below.
            }
            else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {

                // Give touchpad interface back to the user (see onKeyDown() for details)
                setSlide(5);

                // Alert the user that Bluetooth has disconnected
                setDesc("Disconnected from PC\nTap to exit");
            }
            else if (LeapGlassMain.ACTION_BT_SCAN.equals(action)) {
                startBluetooth();
            }
            else if (LeapGlassMain.ACTION_BT_CONNECT.equals(action)) {
                // Get the device
                BluetoothDevice device = intent.getParcelableExtra("device");

                // Start a Bluetooth connection
                connection = new ConnectThread(device);
                connection.start();
            }
            else if (LeapGlassMain.ACTION_BT_CANCEL.equals(action)) {
                // Stop the current Bluetooth connection
                connection.cancel();
            }
        }
    };

    /**
     * Sets the text of the UI description text view
     * @param desc The new desired description
     */
    private void setDesc(String desc) {
        Intent intent = new Intent(LeapGlassMain.ACTION_SET_DESC);
        intent.putExtra("desc", desc);
        sendBroadcast(intent);
    }

    /**
     * Sets the slide number of the UI view
     * @param slide The new desired slide number
     */
    private void setSlide(int slide) {
        Intent intent = new Intent(LeapGlassMain.ACTION_SET_SLIDE);
        intent.putExtra("slide", slide);
        sendBroadcast(intent);
    }

    /**
     * Handles Bluetooth connections and send/receive
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // uuid is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            adapter.cancelDiscovery();

            try {
                // Connect to the device through the socket. This will
                // block until it succeeds or throws an exception.
                mmSocket.connect();

                // Create streams for the BT socket
                OutputStream outStream = mmSocket.getOutputStream();
                InputStream inStream = mmSocket.getInputStream();

                byte[] inBuffer = new byte[1024]; // buffer store for input stream
                int bytes; // bytes returned from read()

                // Keep listening to the input stream
                while (true) {
                    if (inStream.available() > 0) {
                        try {
                            // Read from the input stream
                            inStream.read(inBuffer);
                            String str = new String(inBuffer, 0, inBuffer.length);

                            if (str.indexOf("start") == 0) {
                                // Give touchpad interface back to onKeyDown (see that function for details)
                                setSlide(4);

                                // Launch demo
                                Intent intent = new Intent(getApplicationContext(), com.carrotcorp.leapglass.LeapGlassDemo.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                getApplicationContext().startActivity(intent);

                                Log.i(appName, "connected to bluetooth device");
                                setDesc("Connected to PC!");
                            }
                            else {
                                setDesc(str);

                                int key = -1;
                                if (str.contains("down")) {
                                    key = KeyEvent.KEYCODE_BACK;
                                }
                                else if (str.contains("left")) {
                                    key = KeyEvent.KEYCODE_DPAD_LEFT;
                                }
                                else if (str.contains("right")) {
                                    key = KeyEvent.KEYCODE_DPAD_RIGHT;
                                }
                                else if (str.contains("tap")) {
                                    key = KeyEvent.KEYCODE_DPAD_CENTER;
                                }

                                injectKey(key);
                            }

                            Log.i(appName, "str: " + str);
                        } catch (IOException e) {
                            e.printStackTrace();
                            break;
                        }
                    }
                }


                /*// Create a message to send
                String message = "Hello from Android.\n";
                byte[] outBuffer = message.getBytes();
                while (true) {
                    outStream.write(outBuffer);
                    Thread.sleep(1000);
                }*/
            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                try {
                    setSlide(5);
                    setDesc("Connection Failed\nTap to exit");

                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }
        }

        private void injectKey(int key) {
            try
            {
                if (key != -1) {
                    String keyCommand = "input keyevent " + key;
                    Runtime runtime = Runtime.getRuntime();
                    runtime.exec(keyCommand);
                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                setSlide(5);
                setDesc("Disconnected from PC\nTap to exit");

                mmSocket.close();
            } catch (IOException e) {}
        }
    }
}
