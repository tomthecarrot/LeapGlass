package com.carrotcorp.leapglass;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * This is the main activity of the Glass application for the LeapGlass Project.
 * Code by Thomas Suarez, Chief Engineer @ CarrotCorp
 */
public class LeapGlassMain extends Activity {
    private String appName; // the name of this app
    private TextView descView; // description text view
    private ListView listView; // dynamic list view of Bluetooth device names and addresses
    private ArrayAdapter<String> list; // dynamic list object of Bluetooth device names and address
    private ArrayList<BluetoothDevice> listDevices; //dynamic list object of Bluetooth device objects
    private int currentSlide = 0; // current slide of view information
    final public static String ACTION_SET_DESC = "com.carrotcorp.leapglass.action.SET_DESC";
    final public static String ACTION_SET_SLIDE = "com.carrotcorp.leapglass.action.SET_SLIDE";
    final public static String ACTION_PUT_DEVICE = "com.carrotcorp.leapglass.action.PUT_DEVICE";
    //////////
    final public static String ACTION_BT_SCAN = "com.carrotcorp.leapglass.action.BT_SCAN";
    final public static String ACTION_BT_CONNECT = "com.carrotcorp.leapglass.action.BT_CONNECT";
    final public static String ACTION_BT_CANCEL = "com.carrotcorp.leapglass.action.BT_CANCEL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set global variables
        appName = (String) getResources().getText(R.string.app_name);
        list = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new ArrayList<String>());
        listDevices = new ArrayList<BluetoothDevice>();

        // Set up view
        setTheme(android.R.style.Theme_Holo_NoActionBar_Fullscreen);
        setContentView(R.layout.activity_main);
        descView = (TextView) findViewById(R.id.descView);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(list);
        list.setNotifyOnChange(true);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long length) {
                // Get the address of the device
                BluetoothDevice device = listDevices.get(index);
                String btAddress = device.getAddress();

                Log.i(appName, "list item clicked: " + btAddress);

                // Connect to the device
                descView.setText("Connecting to " + device.getName() + "...");

                Intent intent = new Intent(ACTION_BT_CONNECT);
                intent.putExtra("device", device);
                sendBroadcast(intent);
            }
        });

        // Register action event listener with filters
        IntentFilter brFilter1 = new IntentFilter(LeapGlassMain.ACTION_SET_DESC);
        IntentFilter brFilter2 = new IntentFilter(LeapGlassMain.ACTION_SET_SLIDE);
        IntentFilter brFilter3 = new IntentFilter(LeapGlassMain.ACTION_PUT_DEVICE);
        registerReceiver(br, brFilter1);
        registerReceiver(br, brFilter2);
        registerReceiver(br, brFilter3);

        // Start LeapGlassService to handle LEAP Motion events AND Bluetooth events
        Intent intent = new Intent(getApplicationContext(), LeapGlassService.class);
        startService(intent);

        // Launch demo (temporary!)
        Intent intent2 = new Intent(getApplicationContext(), com.carrotcorp.leapglass.LeapGlassDemo.class);
        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //getApplicationContext().startActivity(intent2);

        // Prevent Glass from going to sleep while the app is active
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(br);
    }

    /**
     * Handles LeapGlassService actions
     */
    private final BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // Set UI description text view
            if (ACTION_SET_DESC.equals(action)) {
                String newDesc = intent.getStringExtra("desc");
                descView.setText(newDesc);
            }
            // Set UI slide number
            else if (ACTION_SET_SLIDE.equals(action)) {
                int newSlide = intent.getIntExtra("slide", 0);
                currentSlide = newSlide;
            }
            // Put device on device list view
            else if (ACTION_PUT_DEVICE.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra("device");
                list.add(device.getName() + "\n" + device.getAddress());
                listDevices.add(device);
            }
        }
    };

    /**
     * Forwards touch events from a non-Glass device (phone/tablet)
     * to onKeyDown()
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        if (event.getAction() == MotionEvent.ACTION_UP) {
            onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, null);

            return true;
        }
        /*else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            onKeyDown(KeyEvent.KEYCODE_BACK, null);

            return true;
        }*/
        return false;
    }

    /**
     * Handles touch down event from the Glass device
     */
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        super.onKeyDown(keycode, event);

        if (currentSlide == 3) {
            return false; // make sure user can choose an option from list, and the touch is not intercepted by this function
        }

        // If the user taps the touchpad, either go to the
        // next slide, or do something, based on whatever is needed
        if (keycode == KeyEvent.KEYCODE_DPAD_CENTER) {
            String newStatus = "";

            switch (currentSlide) {
                case 0: // from welcome slide
                    newStatus = (String) getResources().getText(R.string.instruct_info);
                    break;
                case 1: // from info instruction slide
                    newStatus = (String) getResources().getText(R.string.instruct_bt);
                    break;
                case 2: // from bluetooth instruction slide
                    newStatus = ""; // clear message to make room for list view
                    sendBroadcast(new Intent(ACTION_BT_SCAN));
                    break;
                case 4: // from connected slide
                    sendBroadcast(new Intent(ACTION_BT_CANCEL));
                case 5: // from disconnected slide
                    finish();
            }

            descView.setText(newStatus);
            currentSlide++;

            return true;
        }
        // If the user swipes down, cancel any current Bluetooth connections and exit
        else if (keycode == KeyEvent.KEYCODE_BACK) {
            sendBroadcast(new Intent(ACTION_BT_CANCEL));
            finish();
        }

        return false;
    }

}