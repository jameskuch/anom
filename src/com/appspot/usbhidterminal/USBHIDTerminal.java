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
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.SeekBar;

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

	private CheckBox chk_AnomAttached;
	private EditText txtHidInput;
	private Button btnSend;
	private Button btnSelectHIDDevice;
	private RadioButton rbSendDataType;

	private SeekBar sbLED0int;
	private SeekBar sbLED1int;
	private SeekBar sbLED2int;
	private SeekBar sbRG0int;
	private SeekBar sbRG1int;
	private SeekBar sbRG2int;


	private EditText txt_P0;
	private EditText txt_P1;
	private EditText txt_P2;
	private EditText txt_L0;
	private EditText txt_L1;
	private EditText txt_L2;
	private String settingsDelimiter;

	private String receiveDataFormat;
	private String delimiter;

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

		btnSelectHIDDevice = (Button) findViewById(R.id.btnSelectHIDDevice);
		btnSelectHIDDevice.setOnClickListener(this);

		txtHidInput = (EditText) findViewById(R.id.edtxtHidInput);
		txt_L0 = (EditText) findViewById(R.id.txt_LED0);
		txt_L1 = (EditText) findViewById(R.id.txt_LED1);
		txt_L2 = (EditText) findViewById(R.id.txt_LED2);
		txt_P0 = (EditText) findViewById(R.id.txt_Patch0);
		txt_P1 = (EditText) findViewById(R.id.txt_Patch1);
		txt_P2 = (EditText) findViewById(R.id.txt_Patch2);
		rbSendDataType = (RadioButton) findViewById(R.id.rbSendData);
		chk_AnomAttached = (CheckBox) findViewById(R.id.chk_USBattached);
		//rbSendDataType.setOnClickListener(this);
		btnSend = (Button) findViewById(R.id.btnSendText);
		btnSend.setOnClickListener(this);

		mLog("Initialized\nPlease select your USB HID device\n", false);
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		txtHidInput.setText("0 0 0");

		sbLED0int = (SeekBar) findViewById(R.id.sld_LED0int);
		sbLED0int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar sbLED0int, int progress,
										  boolean fromUser) {
				String t = String.valueOf(progress);
				String msg = "0 " + t + " 0";
				txtHidInput.setText(msg);
				txt_L0.setText(t);
				eventBus.post(new USBDataSendEvent(txtHidInput.getText().toString()));
			}
		});

		sbLED1int = (SeekBar) findViewById(R.id.sld_LED1int);
		sbLED1int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar sbLED1int, int progress,
										  boolean fromUser) {
				String t = String.valueOf(progress);
				String msg = "1 " + t + " 0";
				txtHidInput.setText(msg);
				txt_L1.setText(t);
				eventBus.post(new USBDataSendEvent(txtHidInput.getText().toString()));
			}
		});

		sbLED2int = (SeekBar) findViewById(R.id.sld_LED2int);
		sbLED2int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar sbLED2int, int progress,
										  boolean fromUser) {
				String t = String.valueOf(progress);
				String msg = "2 " + t + " 0";
				txtHidInput.setText(msg);
				txt_L2.setText(t);
				eventBus.post(new USBDataSendEvent(txtHidInput.getText().toString()));
			}
		});


		sbRG0int = (SeekBar) findViewById(R.id.sld_RG1);
		sbRG0int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar sbRG0int, int progress, boolean fromUser) {

				ImageView ivcircleA = (ImageView) findViewById(R.id.iv_circleA);
				ivcircleA.setColorFilter(Color.rgb(255 - progress, progress, 0));
				String msg = String.valueOf(255 - progress);
				txt_P0.setText(msg);
			}
		});


		sbRG1int = (SeekBar) findViewById(R.id.sld_RG2);
		sbRG1int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar sbRG1int, int progress, boolean fromUser) {

				ImageView ivcircleB = (ImageView) findViewById(R.id.iv_circleB);
				ivcircleB.setColorFilter(Color.rgb(255 - progress, progress, 0));
				String msg = String.valueOf(255 - progress);
				txt_P1.setText(msg);
			}
		});


		sbRG2int = (SeekBar) findViewById(R.id.sld_RG3);
		sbRG2int.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar sbRG2int, int progress, boolean fromUser) {

				ImageView ivcircleC = (ImageView) findViewById(R.id.iv_circleC);
				ivcircleC.setColorFilter(Color.rgb(255 - progress, progress, 0));
				String msg = String.valueOf(255 - progress);
				txt_P2.setText(msg);
			}
		});
		rbSendDataType.setChecked(true);
		//sendToUSBService(Consts.ACTION_USB_DATA_TYPE, rbSendDataType.isChecked());

		//DisplayMetrics metrics = new DisplayMetrics();
		//getWindowManager().getDefaultDisplay().getMetrics(metrics);
		//Bitmap.Config conf = Bitmap.Config.ARGB_4444;

		//Bitmap mNewBitmap = Bitmap.createBitmap(metrics, Color.RED, w, h, conf);
		//iv.setImageBitmap(mNewBitmap);
		//iv.setImageBitmap(mNewBitmap);
	}

	private void changeBitmapColor(Bitmap sourceBitmap, ImageView iv, int color) {


		Bitmap workingBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, sourceBitmap.getWidth(), sourceBitmap.getHeight());
		Bitmap resultBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
		Paint p = new Paint();
		ColorFilter filter = new LightingColorFilter(color, 1);
		p.setColorFilter(filter);
		iv.setImageBitmap(resultBitmap);

		Canvas canvas = new Canvas(resultBitmap);
		canvas.drawBitmap(resultBitmap, 0, 0, p);
	}


	public void onClick(View v) {
		if (v == btnSend) {
			eventBus.post(new USBDataSendEvent(txtHidInput.getText().toString()));
		//} else if (v == rbSendDataType) {

			//sendToUSBService(Consts.ACTION_USB_DATA_TYPE, rbSendDataType.isChecked());
		} else if (v == btnSelectHIDDevice) {

			eventBus.post(new PrepareDevicesListEvent());
		}
	}

	void showListOfDevices(CharSequence devicesName[]) {


        if (devicesName.length == 0) {
            chk_AnomAttached.setChecked(false);
        }
        else
        {
            eventBus.post(new SelectDeviceEvent(0));
            chk_AnomAttached.setChecked(true);
        }

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
		btnSend.setEnabled(true);
	}

	public void onEvent(DeviceDetachedEvent event) {
		btnSend.setEnabled(false);
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
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
	}
}