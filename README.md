LeapGlass
=========

An app that relays Leap Motion data to Glass.

Tutorial:  
1. Download and install the Leap Motion software.  
2. Compile and install the Glass part of LeapGlass.  
3. Plug in your Leap Motion device through USB.  
4. Set your computer’s Bluetooth settings to discoverable.  
5. Open a terminal console or command line and type: ```python LeapGlass.py```  
6. Say “ok glass, hand motion” to start.  
7. Follow the menus as directed.  
8. Swipe your hand in mid-air to the left or right to scroll the cards on Glass.  
[optional] Replace the “my PC” variable value (in the Android code) to your computer’s Bluetooth name to expedite discovery.  

To uninstall:  
Open a terminal console or command line and type: ```adb uninstall com.carrotcorp.leapglass```

CAUTION: This app is not well-tested. Many things may not work. I am not responsible for anything that may happen as a result of installing and using this app. That being said, if you want to try out Leap Motion with Google Glass, go ahead!  
Please feel free to submit a report through the issue tracker if you find any bugs.

This app is inspired by Laen from OSH Park.

Licensed under the CarrotCorp Open Source License 1.0. See LICENSE file for more information.
