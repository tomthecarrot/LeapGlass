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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Set;
import java.util.UUID;

/**
 * This is the background service of the Glass application for the LeapGlass Project.
 * Code by Thomas Suarez, Chief Engineer @ CarrotCorp
 */
public class LeapGlassService extends Service {
    private String appName; // the name of this app
    private boolean connected = false;
    private String serverIpAddress = "192.168.0.6"; // ip address of Python server
    private int serverPort = 1202;

    // TODO: change the 'serverIpAddress' variable to your PC's IP address.

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

        // Connect to the device through a network socket.
        new Thread(new ClientThread()).start();
        setDesc("Connecting to PC...");

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(appName, "service destroyed!");
    }

    public class ClientThread implements Runnable {

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                Log.d("ClientActivity", "Socket Connecting");
                Socket socket = new Socket(serverAddr, serverPort);
                connected = true;
                while (connected) {
                    try {
                        // Initialize out & in buffers
                        PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                                .getOutputStream())), true);
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                        // Read from the input stream
                        String line;
                        while ((line = in.readLine()) != null) {
                            //try {
                                if (line.indexOf("start") == 0) {
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
                                    setDesc(line);

                                    int key = -1;
                                    if (line.contains("down")) {
                                        key = KeyEvent.KEYCODE_BACK;
                                    }
                                    else if (line.contains("left")) {
                                        key = KeyEvent.KEYCODE_DPAD_LEFT;
                                    }
                                    else if (line.contains("right")) {
                                        key = KeyEvent.KEYCODE_DPAD_RIGHT;
                                    }
                                    else if (line.contains("tap")) {
                                        key = KeyEvent.KEYCODE_DPAD_CENTER;
                                    }

                                    injectKey(key);
                                }

                                Log.i(appName, "line: " + line);
                            /*} catch (IOException e) {
                                e.printStackTrace();
                                break;
                            }*/
                        }

                    } catch (Exception e) {
                        Log.e("ClientActivity", "Socket Error", e);
                    }
                }

                // Close connection
                socket.close();
                Log.d("ClientActivity", "Socket Closed.");

                // Give touchpad interface back to the user (see onKeyDown() for details)
                setSlide(5);

                // Alert the user that Bluetooth has disconnected
                setDesc("Disconnected from PC\nTap to exit");

            } catch (Exception e) {
                Log.e("ClientActivity", "Socket Error", e);
                connected = false;
            }
        }
    }

    /**
     * Sets the text of the UI description text view
     * @param desc The new desired description
     */
    private void setDesc(String desc) {
        Log.i(appName, "desc: " + desc);

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
}
