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

	public static final int PA_RED_START_LOW = 50;
	public static final int PA_RED_START_HIGH = 245;
	public static final int DA_RED_START_LOW = 50;
	public static final int DA_RED_START_HIGH = 245;
	public static final int NO_RED_START_LOW = 140;
	public static final int NO_RED_START_HIGH = 219;
	public static final int INITIAL_SEPARATION = 20;
	public static final int PPE_LED = 10;
	public static final int PPE_PATCH = 255;
	public static final int DPE_LED = 255;
	public static final int DPE_PATCH = 20;
	public static final int LED_INT_NORMAL = 200;
	public static final int LED_INT_DA = 200;
	public static final int LED_INT_PA = 200;
	public static final int PATCH_RED_NORMAL = 179;
	public static final int PATCH_RED_DA = 139; //deuteranomolous trichromats are less sensitive to GRN
	public static final int PATCH_RED_PA = 229; //protanomolous trichromats are less sensitive to RED
	public static final int NUMBER_OF_PRE_TRIALS = 3;
	public static final int MAX_TRIALS = 500;
	public static final int NUMBER_OF_SIMULTANEOUS_STAIRCASES = 2; //Keep number even
	public static final int NUMBER_OF_TRIALS_BETWEEN_TRAINING_TRIAL  = 5;
	public static final int NUMBER_OF_TRIALS_BETWEEN_TRAINING_TRIAL_PLUS_MINUS = 1;

	private Consts() {
	}
}