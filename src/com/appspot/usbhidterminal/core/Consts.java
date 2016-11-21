package com.appspot.usbhidterminal.core;

public abstract class Consts {
	public static final String BINARY = "binary";
	public static final String INTEGER = "integer";
	public static final String HEXADECIMAL = "hexadecimal";
	public static final String TEXT = "text";

	public static final String ACTION_USB_PERMISSION = "com.google.android.HID.action.USB_PERMISSION";
	public static final String MESSAGE_SELECT_YOUR_USB_HID_DEVICE = "Please select your USB HID device";
	public static final String MESSAGE_CONNECT_YOUR_USB_HID_DEVICE = "Please connect your USB HID device";
	public static final String RECEIVE_DATA_FORMAT = "receiveDataFormat";
	public static final String DELIMITER = "delimiter";
	public static final String DELIMITER_NONE = "none";
	public static final String DELIMITER_NEW_LINE = "newLine";
	public static final String DELIMITER_SPACE = "space";
	public static final String NEW_LINE = "\n";
	public static final String SPACE = " ";

	public static final String ACTION_USB_SHOW_DEVICES_LIST = "ACTION_USB_SHOW_DEVICES_LIST";
	public static final String ACTION_USB_DATA_TYPE = "ACTION_USB_DATA_TYPE";
	public static final int RESULT_SETTINGS = 7;
	public static final String USB_HID_TERMINAL_CLOSE_ACTION = "USB_HID_TERMINAL_EXIT";
	public static final String WEB_SERVER_CLOSE_ACTION = "WEB_SERVER_EXIT";
	public static final String SOCKET_SERVER_CLOSE_ACTION = "SOCKET_SERVER_EXIT";
	public static final int USB_HID_TERMINAL_NOTIFICATION = 45277991;
	public static final int WEB_SERVER_NOTIFICATION = 45277992;
	public static final int SOCKET_SERVER_NOTIFICATION = 45277993;

	public static final int PA_RED_START_LOW = 200;
	public static final int PA_RED_START_HIGH = 255;
	public static final int DA_RED_START_LOW = 120;
	public static final int DA_RED_START_HIGH = 200;
	public static final int NO_RED_START_LOW = 140;
	public static final int NO_RED_START_HIGH = 219;
	public static final int INITIAL_SEPARATION = 20;
	public static final int FINAL_SEPARATION = 2;
	//public static final int PROTANOPE_LED = 10;
	//public static final int PROTANOPE_PATCH = 255;
	//public static final int DEUTERANOPE_LED = 245;
	//public static final int DEUTERANOPE_PATCH = 30;
	//public static final int DEUTERANOPE_LED = 80;
	//public static final int DEUTERANOPE_PATCH = 174;
	public static final int PROTANOPE_LED = 40;
	public static final int PROTANOPE_PATCH = 100;
	public static final int DEUTERANOPE_LED = 40;
	public static final int DEUTERANOPE_PATCH = 100;

	public static final int LED_INT_NORMAL = 255;
	public static final int LED_INT_DA = 255;
	public static final int LED_INT_PA = 150;
	public static final int PATCH_RED_NORMAL = 177;
	public static final int PATCH_RED_DA = 149; //deuteranomolous trichromats are less sensitive to GRN
	public static final int PATCH_RED_PA = 234; //protanomolous trichromats are less sensitive to RED
	public static final int NUMBER_OF_PRE_TRIALS = 3;
	public static final int MAX_TRIALS = 500;
	public static final int NUMBER_OF_SIMULTANEOUS_STAIRCASES = 2; //Keep number even
	public static final int NUMBER_OF_TRIALS_BETWEEN_TRAINING_TRIAL  = 5;
	public static final int NUMBER_OF_TRIALS_BETWEEN_TRAINING_TRIAL_PLUS_MINUS = 1;
	public static final int NUMBER_OF_REPEATS_BEFORE_MOVE = 2;

	public static final int THRESHOLD_LARGE = 80;
	//public static final boolean DEBUG = false;
	public static final boolean DEBUG_ALTERNATE_0 = false;
	public static final boolean DEBUG_ALTERNATE_1 = false;
	public static final boolean DEBUG_PROCESS_RESPONSE = false;
	public static final boolean DEBUG_NO_DICHROMATIC_PATCH = false;

	//public static final int NUMBER_OF_MISSED_EQUALS_BETWEEN_STOPPING = 5;

	private Consts() {
	}
}