package com.appspot.usbhidterminal;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import java.util.Random;


import com.appspot.usbhidterminal.core.Consts;
import com.appspot.usbhidterminal.core.events.DeviceAttachedEvent;
import com.appspot.usbhidterminal.core.events.DeviceDetachedEvent;
import com.appspot.usbhidterminal.core.events.LogMessageEvent;
import com.appspot.usbhidterminal.core.events.PrepareDevicesListEvent;
import com.appspot.usbhidterminal.core.events.SelectDeviceEvent;
import com.appspot.usbhidterminal.core.events.ShowDevicesListEvent;
import com.appspot.usbhidterminal.core.events.USBDataReceiveEvent;
import com.appspot.usbhidterminal.core.events.USBDataSendEvent;
import com.appspot.usbhidterminal.core.services.SocketService;
import com.appspot.usbhidterminal.core.services.USBHIDService;
import com.appspot.usbhidterminal.core.services.WebServerService;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.EventBusException;

public class USBHIDTerminal extends Activity implements View.OnClickListener {

	private SharedPreferences sharedPreferences;

	private Intent usbService;

	private EditText txtHidInput;

	private ImageButton btn_0;
	private ImageButton btn_1;
	private ImageButton btn_2;
	private ImageButton btn_3;
	private ImageButton btn_4;
	private ImageButton btn_begin;
	private ImageButton btn_end;


	private String settingsDelimiter;

	private String receiveDataFormat;
	private String delimiter;
	private CheckBox chk_AnomAttached;

	protected EventBus eventBus;


	private boolean called0 = false;
	private boolean called1 = false;
	long startTime = 0;
	Handler timerHandler = new Handler();
	Runnable timerRunnable = new Runnable() {

		@Override
		public void run() {
			long millis = System.currentTimeMillis() - startTime;
			//int seconds = (int) (millis / 1000);
			//int minutes = seconds / 60;
			//seconds = seconds % 60;

			//txt_TimerView.setText(String.format("%d:%02d", minutes, seconds));
			timerHandler.postDelayed(this, 25);

			// here is where I will check for the USB
			if (millis >= 50 && millis < 300)
			{
				// this one only needs to happen once. I do it once, called0 gets set to true, and
				// it should never be set again.
				if (!called0) {
					called0 = true;
					sendToUSBService(Consts.ACTION_USB_DATA_TYPE, true);
				}


			}
			else if (millis >= 300 && millis <= 750)
			{
				if (!called1) {
					called1 = true;
					eventBus.post(new PrepareDevicesListEvent());

				}

			}
			else if (millis > 2000)
			{
				if (chk_AnomAttached.isChecked())
				{
					//this stops the timer.
					// I thought about trying to restart the timer if the device de or reattached
					// ...it caused weird behavior. Therefore, as is, you have to have the device
					// connected before you go to the screen. This shouldn't be a problem
					timerHandler.removeCallbacks(timerRunnable);
				}
				else
				{
					//Device is not connected
					//Continue looking for it by restarting the timer
					called1 = false;
					startTime = System.currentTimeMillis();
					timerHandler.postDelayed(timerRunnable, 0);

				}
			}

		}
	};

	private void SetLED(int LED, int value)
	{
		String USBout = "";
		String whichLED = "";

		// top, position 0 is LED 2
		// middle, position 1 is LED 0
		// bottom, position 2 is LED 1
		// 2 is position 1
		// 1 is position 0
		// 1 is position 2
		if (LED==2) {
			whichLED = "1 ";
		}
		else if (LED==1)
		{
			whichLED = "0 ";
		}
		else if (LED==0)
		{
			whichLED = "2 ";
		}
		//String msg = "1 " + t + " 0";
		USBout = whichLED + Integer.toString(value) + " 0";

		eventBus.post(new USBDataSendEvent(USBout));

	}

	private SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if ("enable_socket_server".equals(key) || "socket_server_port".equals(key)) {
				socketServiceIsStart(false);
				socketServiceIsStart(sharedPreferences.getBoolean("enable_socket_server", false));
			} else if ("enable_web_server".equals(key) || "web_server_port".equals(key)) {
				webServerServiceIsStart(false);
				webServerServiceIsStart(sharedPreferences.getBoolean("enable_web_server", false));
			}
		}
	};

	private void prepareServices() {
		usbService = new Intent(this, USBHIDService.class);
		startService(usbService);
		webServerServiceIsStart(sharedPreferences.getBoolean("enable_web_server", false));
		socketServiceIsStart(sharedPreferences.getBoolean("enable_socket_server", false));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		try {
			eventBus = EventBus.builder().logNoSubscriberMessages(false).sendNoSubscriberEvent(false).installDefaultEventBus();
		} catch (EventBusException e) {
			eventBus = EventBus.getDefault();
		}
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
		initUI();
		startTime = System.currentTimeMillis();
		timerHandler.postDelayed(timerRunnable, 0);
	}

	private void initUI() {
		setVersionToTitle();

		txtHidInput = (EditText) findViewById(R.id.edtxtHidInput);
		//rbSendDataType = (RadioButton) findViewById(R.id.rbSendData);

		//rbSendDataType.setOnClickListener(this);
		//rbSendText.setOnClickListener(this);

		mLog("Initialized\nPlease select your USB HID device\n", false);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		btn_0 = (ImageButton) findViewById((R.id.btn_0));
		btn_1 = (ImageButton) findViewById((R.id.btn_1));
		btn_2 = (ImageButton) findViewById((R.id.btn_2));
		btn_3 = (ImageButton) findViewById((R.id.btn_3));
		btn_4 = (ImageButton) findViewById((R.id.btn_4));
		btn_begin = (ImageButton) findViewById((R.id.btn_Begin));

		chk_AnomAttached = (CheckBox) findViewById((R.id.chk_USBattached));
		chk_AnomAttached.setEnabled(false);

		btn_0.setOnClickListener(this);
		btn_1.setOnClickListener(this);
		btn_2.setOnClickListener(this);
		btn_3.setOnClickListener(this);
		btn_4.setOnClickListener(this);
		btn_begin.setOnClickListener(this);


		btn_0.setVisibility(View.INVISIBLE);
		btn_1.setVisibility(View.INVISIBLE);
		btn_2.setVisibility(View.INVISIBLE);
		btn_3.setVisibility(View.INVISIBLE);
		btn_4.setVisibility(View.INVISIBLE);

		//sbLED0int = (SeekBar) findViewById(R.id.sld_LED0int);
		//sbLED0int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//			public void onStopTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onStartTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onProgressChanged(SeekBar sbLED0int, int progress,
//										  boolean fromUser) {
//				String t = String.valueOf(progress);
//				String msg = "0 " + t + " 0";
//				txtHidInput.setText(msg);
//				eventBus.post(new USBDataSendEvent(txtHidInput.getText().toString()));
//			}
//		});
//
//		sbLED1int = (SeekBar) findViewById(R.id.sld_LED1int);
//		sbLED1int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//			public void onStopTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onStartTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onProgressChanged(SeekBar sbLED1int, int progress,
//										  boolean fromUser) {
//				String t = String.valueOf(progress);
//				String msg = "1 " + t + " 0";
//				txtHidInput.setText(msg);
//				eventBus.post(new USBDataSendEvent(txtHidInput.getText().toString()));
//			}
//		});
//
//		sbLED2int = (SeekBar) findViewById(R.id.sld_LED2int);
//		sbLED2int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//			public void onStopTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onStartTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onProgressChanged(SeekBar sbLED2int, int progress,
//										  boolean fromUser) {
//				String t = String.valueOf(progress);
//				String msg = "2 " + t + " 0";
//				txtHidInput.setText(msg);
//				eventBus.post(new USBDataSendEvent(txtHidInput.getText().toString()));
//			}
//		});
//
//
//		sbRG0int = (SeekBar) findViewById(R.id.sld_RG1);
//		sbRG0int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//			public void onStopTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onStartTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onProgressChanged(SeekBar sbRG0int, int progress, boolean fromUser) {
//
//				ImageView ivcircleB = (ImageView) findViewById(R.id.iv_circleA);
//				ivcircleB.setColorFilter(Color.rgb(255 - progress, progress, 0));
//			}
//		});
//
//
//		sbRG1int = (SeekBar) findViewById(R.id.sld_RG2);
//		sbRG1int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//			public void onStopTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onStartTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onProgressChanged(SeekBar sbRG1int, int progress, boolean fromUser) {
//
//				ImageView ivcircleB = (ImageView) findViewById(R.id.iv_circleB);
//				ivcircleB.setColorFilter(Color.rgb(255 - progress, progress, 0));
//			}
//		});
//
//
//		sbRG2int = (SeekBar) findViewById(R.id.sld_RG3);
//		sbRG2int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//			public void onStopTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onStartTrackingTouch(SeekBar seekBar) {
//			}
//
//			public void onProgressChanged(SeekBar sbRG2int, int progress, boolean fromUser) {
//
//				ImageView ivcircle = (ImageView) findViewById(R.id.iv_circleC);
//				ivcircle.setColorFilter(Color.rgb(255 - progress, progress, 0));
//			}
//		});

		//sendToUSBService(Consts.ACTION_USB_DATA_TYPE, true);

	}
	@Override
	public void onPause() {
		super.onPause();
		timerHandler.removeCallbacks(timerRunnable);
	}

	public void onClick(View v) {

		if (v == btn_0) {
			txtHidInput.setText("btn0");
			SetLED(0, 28);
		} else if (v == btn_1) {
			txtHidInput.setText("btn1");
			SetLED(1, 128);
		} else if (v == btn_2) {
			txtHidInput.setText("btn2");
			SetLED(2, 255);
		} else if (v == btn_3) {
			txtHidInput.setText("btn3");
			SetLED(1, 0);
		} else if (v == btn_4) {
			txtHidInput.setText("btn4");
			SetLED(2, 0);
		} else if (v == btn_begin) {
			txtHidInput.setText("btnbegin");
			experiment();

		}

	}
	private int iteration = 0;

	void experiment()
	{
		if (chk_AnomAttached.isChecked()) {
			btn_0.setVisibility(View.VISIBLE);
			btn_1.setVisibility(View.VISIBLE);
			btn_2.setVisibility(View.VISIBLE);
			btn_3.setVisibility(View.VISIBLE);
			btn_4.setVisibility(View.VISIBLE);


			//This program is going to work by having certain prescribed presentations of colors
			//First, we need to separate anomalous trichromats from

			if (iteration == 0)
			{
				iteration ++;
				Random r = new Random();
				int i1 = r.nextInt(2);
			}


		}

	}


	void showListOfDevices(CharSequence devicesName[]) {


		//So here's my thought. I know that when the "prepare Device list Event" is called,
		//even though I don't know how it all gets back here, this is where we ultimately build the
		//pop up menu. The "Builder.show" below pops it up.

		//I'm going to select that integer which is 1 or 0...we'll try both and I'm going to try to
		//automatically set them.

		//it's either a 0 or a 1...don't know yet.
		if (devicesName.length == 0) {
			chk_AnomAttached.setChecked(false);
		}
		else
		{
			eventBus.post(new SelectDeviceEvent(0));
			chk_AnomAttached.setChecked(true);
		}



//		AlertDialog.Builder builder = new AlertDialog.Builder(this);
//
//		if (devicesName.length == 0) {
//			builder.setTitle(Consts.MESSAGE_CONNECT_YOUR_USB_HID_DEVICE);
//		} else {
//			builder.setTitle(Consts.MESSAGE_SELECT_YOUR_USB_HID_DEVICE);
//		}
//		builder.setItems(devicesName, new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//				eventBus.post(new SelectDeviceEvent(which));
//				txtHidInput.setText(Integer.toString(which));
//			}
//		});
//		builder.setCancelable(true);
//		builder.show();
	}

	public void onEvent(USBDataReceiveEvent event) {
		mLog(event.getData() + " \nReceived " + event.getBytesCount() + " bytes", true);
	}

	public void onEvent(LogMessageEvent event) {
		mLog(event.getData(), true);
	}

	public void onEvent(ShowDevicesListEvent event) {
		showListOfDevices(event.getCharSequenceArray());
	}

	public void onEvent(DeviceAttachedEvent event) {
		//startTime = System.currentTimeMillis();
		//timerHandler.postDelayed(timerRunnable, 0);
		//called1 = false;
	}

	public void onEvent(DeviceDetachedEvent event)
	{
		//startTime = System.currentTimeMillis();
		//timerHandler.postDelayed(timerRunnable, 0);
		//called1 = false;
	}

	@Override
	protected void onStart() {
		super.onStart();
		receiveDataFormat = sharedPreferences.getString(Consts.RECEIVE_DATA_FORMAT, Consts.TEXT);
		prepareServices();
		setDelimiter();
		eventBus.register(this);
	}

	@Override
	protected void onStop() {
		eventBus.unregister(this);
		super.onStop();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		setSelectedMenuItemsFromSettings(menu);
		return true;
	}

	private void setSelectedMenuItemsFromSettings(Menu menu) {
		receiveDataFormat = sharedPreferences.getString(Consts.RECEIVE_DATA_FORMAT, Consts.TEXT);
		if (receiveDataFormat != null) {
			if (receiveDataFormat.equals(Consts.BINARY)) {
				menu.findItem(R.id.menuSettingsReceiveBinary).setChecked(true);
			} else if (receiveDataFormat.equals(Consts.INTEGER)) {
				menu.findItem(R.id.menuSettingsReceiveInteger).setChecked(true);
			} else if (receiveDataFormat.equals(Consts.HEXADECIMAL)) {
				menu.findItem(R.id.menuSettingsReceiveHexadecimal).setChecked(true);
			} else if (receiveDataFormat.equals(Consts.TEXT)) {
				menu.findItem(R.id.menuSettingsReceiveText).setChecked(true);
			}
		}

		setDelimiter();
		if (settingsDelimiter.equals(Consts.DELIMITER_NONE)) {
			menu.findItem(R.id.menuSettingsDelimiterNone).setChecked(true);
		} else if (settingsDelimiter.equals(Consts.DELIMITER_NEW_LINE)) {
			menu.findItem(R.id.menuSettingsDelimiterNewLine).setChecked(true);
		} else if (settingsDelimiter.equals(Consts.DELIMITER_SPACE)) {
			menu.findItem(R.id.menuSettingsDelimiterSpace).setChecked(true);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences.Editor editor = sharedPreferences.edit();
		item.setChecked(true);
		switch (item.getItemId()) {
		case R.id.menuSettings:
			Intent i = new Intent(this, SettingsActivity.class);
			startActivityForResult(i, Consts.RESULT_SETTINGS);
			break;
		case R.id.menuSettingsReceiveBinary:
			editor.putString(Consts.RECEIVE_DATA_FORMAT, Consts.BINARY).apply();
			break;
		case R.id.menuSettingsReceiveInteger:
			editor.putString(Consts.RECEIVE_DATA_FORMAT, Consts.INTEGER).apply();
			break;
		case R.id.menuSettingsReceiveHexadecimal:
			editor.putString(Consts.RECEIVE_DATA_FORMAT, Consts.HEXADECIMAL).apply();
			break;
		case R.id.menuSettingsReceiveText:
			editor.putString(Consts.RECEIVE_DATA_FORMAT, Consts.TEXT).apply();
			break;
		case R.id.menuSettingsDelimiterNone:
			editor.putString(Consts.DELIMITER, Consts.DELIMITER_NONE).apply();
			break;
		case R.id.menuSettingsDelimiterNewLine:
			editor.putString(Consts.DELIMITER, Consts.DELIMITER_NEW_LINE).apply();
			break;
		case R.id.menuSettingsDelimiterSpace:
			editor.putString(Consts.DELIMITER, Consts.DELIMITER_SPACE).apply();
			break;
		}

		receiveDataFormat = sharedPreferences.getString(Consts.RECEIVE_DATA_FORMAT, Consts.TEXT);
		setDelimiter();
		return true;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String action = intent.getAction();
		if (action == null) {
			return;
		}
		switch (action) {
			case Consts.WEB_SERVER_CLOSE_ACTION:
				stopService(new Intent(this, WebServerService.class));
				break;
			case Consts.USB_HID_TERMINAL_CLOSE_ACTION:
				stopService(new Intent(this, SocketService.class));
				stopService(new Intent(this, WebServerService.class));
				stopService(new Intent(this, USBHIDService.class));
				((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(Consts.USB_HID_TERMINAL_NOTIFICATION);
				finish();
				break;
			case Consts.SOCKET_SERVER_CLOSE_ACTION:
				stopService(new Intent(this, SocketService.class));
				sharedPreferences.edit().putBoolean("enable_socket_server", false).apply();
				break;
		}
	}

	private void setDelimiter() {
		settingsDelimiter = sharedPreferences.getString(Consts.DELIMITER, Consts.DELIMITER_NEW_LINE);
		if (settingsDelimiter != null) {
			if (settingsDelimiter.equals(Consts.DELIMITER_NONE)) {
				delimiter = "";
			} else if (settingsDelimiter.equals(Consts.DELIMITER_NEW_LINE)) {
				delimiter = Consts.NEW_LINE;
			} else if (settingsDelimiter.equals(Consts.DELIMITER_SPACE)) {
				delimiter = Consts.SPACE;
			}
		}
		usbService.setAction(Consts.RECEIVE_DATA_FORMAT);
		usbService.putExtra(Consts.RECEIVE_DATA_FORMAT, receiveDataFormat);
		usbService.putExtra(Consts.DELIMITER, delimiter);
		startService(usbService);
	}

	void sendToUSBService(String action) {
		usbService.setAction(action);
		startService(usbService);
	}

	void sendToUSBService(String action, boolean data) {
		usbService.putExtra(action, data);
		sendToUSBService(action);
	}

	void sendToUSBService(String action, int data) {
		usbService.putExtra(action, data);
		sendToUSBService(action);
	}

	private void mLog(String log, boolean newLine) {

	}

	private void webServerServiceIsStart(boolean isStart) {
		if (isStart) {
			Intent webServerService = new Intent(this, WebServerService.class);
			webServerService.setAction("start");
			webServerService.putExtra("WEB_SERVER_PORT", Integer.parseInt(sharedPreferences.getString("web_server_port", "7799")));
			startService(webServerService);
		} else {
			stopService(new Intent(this, WebServerService.class));
		}
	}

	private void socketServiceIsStart(boolean isStart) {
		if (isStart) {
			Intent socketServerService = new Intent(this, SocketService.class);
			socketServerService.setAction("start");
			socketServerService.putExtra("SOCKET_PORT", Integer.parseInt(sharedPreferences.getString("socket_server_port", "7899")));
			startService(socketServerService);
		} else {
			stopService(new Intent(this, SocketService.class));
		}
	}

	private void setVersionToTitle() {
		try {
			this.setTitle(Consts.SPACE + this.getTitle() + Consts.SPACE + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
			//this.setTitle("Neitz Anomaloscope");
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}