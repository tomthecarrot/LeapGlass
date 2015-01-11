LeapGlass
=========

An app that relays Leap Motion data to Google Glass.

**How to Use:**  
1. Download and install the Leap Motion driver from http://leapmotion.com/setup.  
2. Change the IP address in 'LeapGlassService.java' to your computer's IP address.
3. Compile the LeapGlass Client and install it on your Glass via adb.  
4. Plug in your Leap Motion device through USB.  
5. Run the LeapGlass Server with Python: ```python LeapGlass.py```  
6. Say “ok glass, hand motion” to start.  
7. Wait for connection to PC.  
8. Swipe your hand in mid-air (over the LEAP controller) to the left or right to scroll the cards on Glass.  

**If you are using a Chromebook as the server**:  
I have tested the LeapGlass Server on an HP Chromebook 14, with the LEAP controller plugged in through the USB 3 (superspeed) port. This may work on other Chromebooks as well.  
1.  Put Chromebook in Developer Mode.  
2.  Install Ubuntu with [crouton](http://www.howtogeek.com/162120/how-to-install-ubuntu-linux-on-your-chromebook-with-crouton/).  
3.  [Disable Chromebook TCP firewall](http://stackoverflow.com/a/15555948/2617124).  
4.  Download the LeapGlass Server and run it as described above.  

**To uninstall**:  
Open a terminal console or command line and type: ```adb uninstall com.carrotcorp.leapglass```

**CAUTION**: This app is an experiment. Some things may not run smoothly. I am not responsible for anything that may happen as a result of downloading, installing, or using this app. That said, if you want to try out Leap Motion with Google Glass, go ahead!  
Please submit a report through the issue tracker if you find any bugs.

This app is inspired by [Laen from OSH Park](https://twitter.com/laen/status/464843890722226176).

Licensed under the CarrotCorp Open Source License 1.0.1. See LICENSE file for more information.
