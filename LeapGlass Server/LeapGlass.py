"""
This is the PC application for the LeapGlass Project.
Code by Thomas Suarez, Chief Engineer @ CarrotCorp
"""
import socket, Leap, sys, datetime
from Leap import SwipeGesture

conn = 0
sock = 0
PORT = 1202
lastTime = datetime.datetime.now()

class LeapListener(Leap.Listener):
    def on_init(self, controller):
        print "Initialized"

    def on_connect(self, controller):
        print "Connected"

        # Enable gestures
        controller.enable_gesture(Leap.Gesture.TYPE_SCREEN_TAP);
        controller.enable_gesture(Leap.Gesture.TYPE_SWIPE);

    def on_disconnect(self, controller):
        # Note: not dispatched when running in a debugger.
        print "Disconnected"

    def on_exit(self, controller):
        print "Exited"

    def on_frame(self, controller):
        # Get the most recent frame and report some basic information
        frame = controller.frame()
        
        global lastTime

        if not frame.hands.is_empty:
            # Gestures
            for gesture in frame.gestures():
                # Check whether a gesture has been preformed in the last half of a second.
                # If it hasn't, continue the gesture. This is done to make sure that
                # the gesture log does not have more than one gesture at a time.
                diff = datetime.datetime.now() - lastTime
                if diff.seconds > .5:
               	    # Set current time
                    lastTime = datetime.datetime.now()
                    if gesture.type == Leap.Gesture.TYPE_SWIPE:
                        # Get the swipe gesture, its direction, and its speed
                        swipe = SwipeGesture(gesture)
                        #print "Swipe. direction: %s, speed: %f" % (swipe.direction, swipe.speed)
                        # Calculate direction
                        direction = "none"
                        if abs(float(swipe.direction.x)) > abs(float(swipe.direction.y)):
                        	if swipe.direction.x > 0:
                        		direction = "right"
                        	else:
                        		direction = "left"
                        else:
                        	if swipe.direction.y > 0:
                        		direction = "up"
                        	else:
                        		direction = "down"
                        print "direction: " + direction
                        conn.send(direction + "\n")
                        
                    if gesture.type == Leap.Gesture.TYPE_SCREEN_TAP:
                    	print "screen tap"
                    	conn.send("tap\n")

    def state_string(self, state):
    	if state == Leap.Gesture.STATE_START:
        	return "STATE_START"

    	if state == Leap.Gesture.STATE_UPDATE:
        	return "STATE_UPDATE"

    	if state == Leap.Gesture.STATE_STOP:
     	   return "STATE_STOP"

    	if state == Leap.Gesture.STATE_INVALID:
     	   return "STATE_INVALID"

def initSocket():
    global conn
    global sock

    # Listen for WiFi connections
    sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    sock.bind(("192.168.0.6", PORT))
    sock.listen(1)

    # Accept the first incoming connection
    conn, addr = sock.accept()
    print "Connected by", addr

    # Send the start command to Glass
    conn.send("start\n")

def main():
    # Create a LEAP Motion listener and controller
    listener = LeapListener()
    controller = Leap.Controller()

    # Register the listener to receive motion events
    controller.add_listener(listener)

    # Set up WiFi socket communication
    initSocket()

    # Keep this app running until the Enter key is pressed
    print "Press Enter to quit..."
    sys.stdin.readline()

    # Remove the listener when app is terminated
    controller.remove_listener(listener)

    # Close Socket connection
    conn.close()
    sock.close()

if __name__ == "__main__":
    main()
