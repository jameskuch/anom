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

import java.util.ArrayList;
import java.util.List;
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
	private TextView txt_inst;
	private ImageButton btn_0;
	private ImageButton btn_1;
	private ImageButton btn_2;
	private ImageButton btn_3;
	private ImageButton btn_4;
	private ImageButton btn_begin;
	private CheckBox chk_AnomAttached;

	private ImageView patch_0;
	private ImageView patch_1;
	private ImageView patch_2;
	//private int RG_0 = 255;
	//private int RG_1 = 255;
	//private int RG_2 = 255;

	private String settingsDelimiter;
	private String receiveDataFormat;
	private String delimiter;



	protected EventBus eventBus;

	private int[] sin0;
	private int[] sin1;
	private int[] sin2;

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

	long startTime2 = 0;
	Handler timerHandler2 = new Handler();
	Runnable timerRunnable2 = new Runnable() {

		@Override
		public void run() {
			long millis = System.currentTimeMillis() - startTime2;
			int seconds = (int) (millis / 1000);
			//int minutes = seconds / 60;
			//seconds = seconds % 60;

			//txt_TimerView.setText(String.format("%d:%02d", minutes, seconds));
			timerHandler2.postDelayed(this, 25);

			if (seconds >= 4)
			{
				txt_inst.setText("");
				timerHandler2.removeCallbacks(timerRunnable2);
			}

		}
	};

	long startTime3 = 0;
	Handler timerHandler3 = new Handler();
	Runnable timerRunnable3 = new Runnable() {

		@Override
		public void run() {
			long millis = System.currentTimeMillis() - startTime3;
			//int minutes = seconds / 60;
			//seconds = seconds % 60;

			//txt_TimerView.setText(String.format("%d:%02d", minutes, seconds));
			timerHandler3.postDelayed(this, 0);
			SetLED(0, sin0[(int)millis]);
			SetLED(1, sin1[(int)millis]);
			SetLED(2, sin2[(int)millis]);
			if (millis > 1000)
			{
				startTime3 = System.currentTimeMillis();
			}

		}
	};

	long startTime4 = 0;
	Handler timerHandler4 = new Handler();
	Runnable timerRunnable4 = new Runnable() {

		@Override
		public void run() {
			long millis = System.currentTimeMillis() - startTime4;
			//int minutes = seconds / 60;
			//seconds = seconds % 60;

			//txt_TimerView.setText(String.format("%d:%02d", minutes, seconds));
			timerHandler4.postDelayed(this, 0);
			if (millis > 1000)
			{
				startTime4 = System.currentTimeMillis();
				timerHandler4.removeCallbacks(timerRunnable4);
				if (iteration >= InitialIterations) {
					btn_0.setVisibility(View.VISIBLE);
					btn_1.setVisibility(View.VISIBLE);
					btn_2.setVisibility(View.VISIBLE);
					btn_3.setVisibility(View.VISIBLE);
					btn_4.setVisibility(View.VISIBLE);
				}
				else {
					btn_0.setVisibility(View.VISIBLE);
					btn_2.setVisibility(View.VISIBLE);
					btn_4.setVisibility(View.VISIBLE);
				}


				Experiment();
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
		txt_inst = (TextView) findViewById(R.id.txt_inst);
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

		patch_0 = (ImageView) findViewById(R.id.iv_circleA);
		patch_1 = (ImageView) findViewById(R.id.iv_circleB);
		patch_2 = (ImageView) findViewById(R.id.iv_circleC);
		//Position0.setColorFilter(Color.rgb(255 - RG_0, RG_0, 0));
		//Position1.setColorFilter(Color.rgb(255 - RG_1, RG_1, 0));
		//Position2.setColorFilter(Color.rgb(255 - RG_2, RG_2, 0));

		patch_0.setColorFilter(Color.rgb(0, 0, 0));
		patch_1.setColorFilter(Color.rgb(0, 0, 0));
		patch_2.setColorFilter(Color.rgb(0, 0, 0));
		txt_inst.setText("");

		sin0 = new int[1000];
		sin1 = new int[1000];
		sin2 = new int[1000];
		for (int x = 0; x < 1000; x++)
		{
			double d = 255 * (Math.sin((double)x * 2 * Math.PI / 1000.00000) + 0.5);
			double e = 255 * (Math.sin((double)x * 2 * Math.PI / 1000.00000 + Math.PI / 3.00000) + 0.5);
			double f = 255 * (Math.sin((double)x * 2 * Math.PI / 1000.00000 + 2 * Math.PI / 3.00000) + 0.5);
			sin0[x] = (int)d;
			sin1[x] = (int)e;
			sin2[x] = (int)f;
		}


	}
	@Override
	public void onPause() {
		super.onPause();
		timerHandler.removeCallbacks(timerRunnable);
	}

	public void onClick(View v) {

		if (v == btn_0) {
			//txtHidInput.setText("btn0");
			//RECORD
			lane00[iteration - 1] = 1;
			Blank(true);
		} else if (v == btn_1) {
			//txtHidInput.setText("btn1");
			//RECORD
			lane05[iteration - 1] = 1;
			Blank(true);
		} else if (v == btn_2) {
			//txtHidInput.setText("btn2");
			//RECORD
			lane10[iteration - 1] = 1;
			Blank(true);

		} else if (v == btn_3) {
			//txtHidInput.setText("btn3");
			//timerHandler3.removeCallbacks(timerRunnable3);
			//RECORD
			lane15[iteration - 1] = 1;
			Blank(true);
		} else if (v == btn_4) {
			//txtHidInput.setText("btn4");
			//startTime3 = System.currentTimeMillis();
			//timerHandler3.postDelayed(timerRunnable3, 0);
			//RECORD
			lane20[iteration - 1] = 1;
			Blank(true);

		} else if (v == btn_begin) {
			//txtHidInput.setText("btnbegin");
			if (intest) {
				btn_begin.setImageResource(R.drawable.button_begintest);
				// show the response buttons
				btn_0.setVisibility(View.INVISIBLE);
				btn_1.setVisibility(View.INVISIBLE);
				btn_2.setVisibility(View.INVISIBLE);
				btn_3.setVisibility(View.INVISIBLE);
				btn_4.setVisibility(View.INVISIBLE);
				// set the boolean variable

				Reset();
				Blank(false);

			}
			else {

				if (finished)
				{
					AlertDialog.Builder meowBuilder = new AlertDialog.Builder(this);
					meowBuilder.setTitle("?");
					meowBuilder.setMessage("Clear previous results and continue?");
					meowBuilder.setCancelable(false);
					meowBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							Reset();
							Experiment();
						}
					});
					meowBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int id) {
							// if this button is clicked, just close
							// the dialog box and do nothing
							dialog.cancel();
						}
					});

					AlertDialog meow = meowBuilder.create();
					meow.show();


				}
				else
				{
					btn_begin.setImageResource(R.drawable.button_reset);
					// show the response buttons
					btn_0.setVisibility(View.VISIBLE);
					//btn_1.setVisibility(View.VISIBLE);
					btn_2.setVisibility(View.VISIBLE);
					//btn_3.setVisibility(View.VISIBLE);
					btn_4.setVisibility(View.VISIBLE);
					// set the boolean variable
					Reset();
					intest = true;
					finished = false;
					Experiment();
				}

			}

		}

	}

	private void Blank(boolean Restart)
	{

		btn_0.setVisibility(View.INVISIBLE);
		btn_1.setVisibility(View.INVISIBLE);
		btn_2.setVisibility(View.INVISIBLE);
		btn_3.setVisibility(View.INVISIBLE);
		btn_4.setVisibility(View.INVISIBLE);
		SetLED(0, 0);
		SetLED(1, 0);
		SetLED(2, 0);
		patch_0.setColorFilter(Color.rgb(0, 0, 0));
		patch_1.setColorFilter(Color.rgb(0, 0, 0));
		patch_2.setColorFilter(Color.rgb(0, 0, 0));
		if (Restart) {
			startTime4 = System.currentTimeMillis();
			timerHandler4.postDelayed(timerRunnable4, 0);
		}
	}


	private void Reset()
	{
		for (int x = 0; x < 500; x++)
		{
			lane00[x] = 0;
			lane05[x] = 0;
			lane10[x] = 0;
			lane15[x] = 0;
			lane20[x] = 0;
			position[x] = 0;
		}
		iteration = 0;
		intest = false;
		finished = false;
		Probably_Normal = false;
		Probably_Protanomolous = false;
		Probably_Deuteranomolous = false;
		Probably_Deuteranope = false;
		Probably_Protanope = false;
	}


	// p = patch or position
	// 0, 1, 2 positions , 0 = top, 1 = middle, 2 = bottom
	// R = red, G will always be 255-R
	// dev = deviation
	// jit = jitter (variation away from the deviation (e.g. 50 - 5 or 50 + 2)
	private int p_dev_p = 50;
	private int p_dev_d = 50;
	private int p_jit = 7; // code to make it +/- this
	private int p_0_R;
	private int p_1_R;
	private int p_2_R;
	private int p_normal = 179; // since light is more orange, need to have a tad more red light.
	private int l_normal = 200;
    private int p_normal_jit = 2;
    private int l_normal_jit = 5;
	private int l_pr_0 = 30;
	private int l_pr_1 = 30;
	private int p_pr_0 = 255;
	private int p_pr_1 = 255;

	private int l_de_0 = 50;
	private int l_de_1 = 50;
	private int p_de_0 = 128;
	private int p_de_1 = 128;

    private int InitialIterations = 15;
	private int iteration = 0;
	private int iterR = 0;
	private int iterG = 0;

	private boolean intest = false;
	private boolean finished = false;
	private boolean Probably_Normal = false;
	private boolean Probably_Protanomolous = false;
	private boolean Probably_Deuteranomolous = false;
	private boolean Probably_Deuteranope = false;
	private boolean Probably_Protanope = false;

	private boolean[] Red = new boolean[500];
	private boolean[] Grn = new boolean[500];
	int[] Red_Margin = new int[500];
	int[] Grn_Margin = new int[500];
	int[] Missed_Protanopic = new int[500];
	int[] Missed_Deuteranopic = new int[500];

	private int[] position = new int[500];
	//class ExperimentResponsesAndVariables{
	int[] lane00 = new int[500];
	int[] lane05 = new int[500];
	int[] lane10 = new int[500];
	int[] lane15 = new int[500];
	int[] lane20 = new int[500];


	//}

	//List<ExperimentResponsesAndVariables> e = new ArrayList<ExperimentResponsesAndVariables>();
	private void RecordResponse()
	{

	}

	void Experiment()
	{
		//conventions 0 - 0.5 TopOfNormal
		//if (!chk_AnomAttached.isChecked()) {

			if (iteration >= 0 && iteration < InitialIterations)
			{
				// The first X, currently 15, presentations will be normal in the middle and 1 deuteranomolous and 1 protanomolous
				// presentation. Position of the deut or pro will move to either side randomly

				// Deutan
					// issues with M
				// Deuteranomous
					// issues with M, having L and L' (more sensitive to red light)

				// always increment the iteration variable
				iteration++;
				// position of the
				Random r = new Random();
				double rr = 59 * r.nextDouble();

				int[] pn = new int[3]; int[] pp = new int[3]; int[] pd = new int[3];
				if (rr >= 0 && rr < 10)
				{
					// 0 norm
					// 1 prot
					// 2 deut
					position[iteration - 1] = 0;
 					pn[0] = 1;  pp[0] = 0;  pd[0] = 0;
					pn[1] = 0;  pp[1] = 1;  pd[1] = 0;
					pn[2] = 0;  pp[2] = 0;  pd[2] = 1;

				}
				else if (rr >= 10 && rr < 20)
				{
					// 0 norm
					// 1 deut
					// 2 prot
					position[iteration - 1] = 1;
					pn[0] = 1;  pp[0] = 0;  pd[0] = 0;
					pn[1] = 0;  pp[1] = 0;  pd[1] = 1;
					pn[2] = 0;  pp[2] = 1;  pd[2] = 0;
				}
				else if (rr >= 20 && rr < 30)
				{
					// 0 prot
					// 1 normal
					// 2 deut
					position[iteration - 1] = 2;
					pn[0] = 0;  pp[0] = 1;  pd[0] = 0;
					pn[1] = 1;  pp[1] = 0;  pd[1] = 0;
					pn[2] = 0;  pp[2] = 0;  pd[2] = 1;
				}
				else if (rr >= 30 && rr < 40)
				{
					// 0 deut
					// 1 norm
					// 2 prot
					position[iteration - 1] = 3;
					pn[0] = 0;  pp[0] = 0;  pd[0] = 1;
					pn[1] = 1;  pp[1] = 0;  pd[1] = 0;
					pn[2] = 0;  pp[2] = 1;  pd[2] = 0;
				}
				else if (rr >= 40 && rr < 50)
				{
					// 0 prot
					// 1 deut
					// 2 norm
					position[iteration - 1] = 4;
					pn[0] = 0;  pp[0] = 1;  pd[0] = 0;
					pn[1] = 0;  pp[1] = 0;  pd[1] = 1;
					pn[2] = 1;  pp[2] = 0;  pd[2] = 0;
				}
				else
				{
					// 0 deut
					// 1 prot
					// 2 norm
					position[iteration - 1] = 5;
					pn[0] = 0;  pp[0] = 0;  pd[0] = 1;
					pn[1] = 0;  pp[1] = 1;  pd[1] = 0;
					pn[2] = 1;  pp[2] = 0;  pd[2] = 0;
				}

//				boolean DeutOnTop = r.nextBoolean();
//				if (DeutOnTop)
//				{
//					p_0_R = p_normal - (p_dev + ((int)(r.nextDouble() * p_jit - (p_jit/2))));
//					p_2_R = p_normal + (p_dev + ((int)(r.nextDouble() * p_jit - (p_jit/2))));
//				}
//				else
//				{
//					p_0_R = p_normal + (p_dev + ((int)(r.nextDouble() * p_jit - (p_jit/2))));
//					p_2_R = p_normal - (p_dev + ((int)(r.nextDouble() * p_jit - (p_jit/2))));
//				}

				p_0_R = pn[0] * (p_normal + (r.nextInt(p_normal_jit * 2) - p_normal_jit)) +
                        pp[0] * (p_normal + (p_dev_p + ((r.nextInt(2 * p_jit) - p_jit)))) +
                        pd[0] * (p_normal - (p_dev_d + ((r.nextInt(2 * p_jit) - p_jit))));
				p_1_R = pn[1] * (p_normal + (r.nextInt(p_normal_jit * 2) - p_normal_jit)) +
                        pp[1] * (p_normal + (p_dev_p + ((r.nextInt(2 * p_jit) - p_jit)))) +
                        pd[1] * (p_normal - (p_dev_d + ((r.nextInt(2 * p_jit) - p_jit))));
				p_2_R = pn[2] * (p_normal + (r.nextInt(p_normal_jit * 2) - p_normal_jit)) +
                        pp[2] * (p_normal + (p_dev_p + ((r.nextInt(2 * p_jit) - p_jit)))) +
                        pd[2] * (p_normal - (p_dev_d + ((r.nextInt(2 * p_jit) - p_jit))));

                int led = l_normal + r.nextInt(l_normal_jit * 2) - l_normal_jit;
				SetLED(0, led);
				SetLED(1, led);
				SetLED(2, led);

				patch_0.setColorFilter(Color.rgb(p_0_R, 255 - p_0_R, 0));
				patch_1.setColorFilter(Color.rgb(p_1_R, 255 - p_1_R, 0));
				patch_2.setColorFilter(Color.rgb(p_2_R, 255 - p_2_R, 0));
				//txt_inst.setText(Integer.toString(iteration));
			}
			else if (iteration >= InitialIterations && !finished)
			{
				double deut_anom = 0;
				double prot_anom = 0;
				double normal = 0;
				//this is the first time we need to do some processing.
				if (iteration == InitialIterations)
				{

					for (int x = 0; x < InitialIterations; x++)
					{
						int[] pn = new int[3]; int[] pp = new int[3]; int[] pd = new int[3];
						if (position[x] == 0)
						{
							// 0 norm
							// 1 prot
							// 2 deut
							pn[0] = 1;  pp[0] = 0;  pd[0] = 0;
							pn[1] = 0;  pp[1] = 1;  pd[1] = 0;
							pn[2] = 0;  pp[2] = 0;  pd[2] = 1;

						}
						else if (position[x] == 1)
						{
							// 0 norm
							// 1 deut
							// 2 prot
							pn[0] = 1;  pp[0] = 0;  pd[0] = 0;
							pn[1] = 0;  pp[1] = 0;  pd[1] = 1;
							pn[2] = 0;  pp[2] = 1;  pd[2] = 0;
						}
						else if (position[x] == 2)
						{
							// 0 prot
							// 1 normal
							// 2 deut
							pn[0] = 0;  pp[0] = 1;  pd[0] = 0;
							pn[1] = 1;  pp[1] = 0;  pd[1] = 0;
							pn[2] = 0;  pp[2] = 0;  pd[2] = 1;
						}
						else if (position[x] == 3)
						{
							// 0 deut
							// 1 norm
							// 2 prot
							pn[0] = 0;  pp[0] = 0;  pd[0] = 1;
							pn[1] = 1;  pp[1] = 0;  pd[1] = 0;
							pn[2] = 0;  pp[2] = 1;  pd[2] = 0;
						}
						else if (position[x] == 4)
						{
							// 0 prot
							// 1 deut
							// 2 norm
							pn[0] = 0;  pp[0] = 1;  pd[0] = 0;
							pn[1] = 0;  pp[1] = 0;  pd[1] = 1;
							pn[2] = 1;  pp[2] = 0;  pd[2] = 0;
						}
						else
						{
							// 0 deut
							// 1 prot
							// 2 norm
							pn[0] = 0;  pp[0] = 0;  pd[0] = 1;
							pn[1] = 0;  pp[1] = 1;  pd[1] = 0;
							pn[2] = 1;  pp[2] = 0;  pd[2] = 0;

						}
						prot_anom += pp[0] * lane00[x] + pp[1] * lane10[x] + pp[2] * lane20[x];
						normal += pn[0] * lane00[x] + pn[1] * lane10[x] + pn[2] * lane20[x];
						deut_anom += pd[0] * lane00[x] + pd[1] * lane10[x] + pd[2] * lane20[x];
					}
					if (deut_anom >= normal)
					{
						Probably_Deuteranomolous = true;
					}
					else if (prot_anom >= normal)
					{
						Probably_Protanomolous = true;
					}
					else
					{
						Probably_Normal = true;
					}
				}
				iteration++;

				//Now we know what pathway

				//first, let's program the normal pathway

				if (Probably_Protanomolous)
				{
					txt_inst.setText("Probably Protanomolous. Prot: " + Double.toString(prot_anom) +  " Deut: " + Double.toString(deut_anom) + " Norm: " + Double.toString(normal));
					startTime2 = System.currentTimeMillis();
					timerHandler2.postDelayed(timerRunnable2, 0);
				}
				else if (Probably_Deuteranomolous)
				{
					txt_inst.setText("Probably Deuteranomolous. Prot: " + Double.toString(prot_anom) +  " Deut: " + Double.toString(deut_anom) + " Norm: " + Double.toString(normal));
					startTime2 = System.currentTimeMillis();
					timerHandler2.postDelayed(timerRunnable2, 0);
				}
				else
				{
					//probably normal pathway
					txt_inst.setText("Probably Normal. Prot: " + Double.toString(prot_anom) +  " Deut: " + Double.toString(deut_anom) + " Norm: " + Double.toString(normal));
					startTime2 = System.currentTimeMillis();
					timerHandler2.postDelayed(timerRunnable2, 0);



					int L_0;
					int L_2;
					Random r = new Random();
					//
					if (r.nextBoolean())
					{
						//red side

						if (iterR == 0)
						{
							Red_Margin[iterR] = p_normal + p_dev_p;
						}
						else
						{
							Red_Margin[iterR] = Red_Margin[iterR - 1] - 5;
						}
						Red[iteration - (InitialIterations + 1)] = true;
						Grn[iteration - (InitialIterations + 1)] = false;

						if (r.nextBoolean())
						{
							// position 0 margin,
							// position 2 protan

							if (r.nextBoolean()) {
								L_2 = l_pr_0;
								p_2_R = p_pr_0;
							}
							else {
								L_2 = l_pr_1;
								p_2_R = p_pr_1;
							}
							L_0 = l_normal;
							p_0_R = Red_Margin[iterR];
						}
						else
						{
							// position 0 protan,
							// position 2 margin
							if (r.nextBoolean()) {
								L_0 = l_pr_0;
								p_0_R = p_pr_0;
							}
							else {
								L_0 = l_pr_1;
								p_0_R = p_pr_1;
							}
							L_2 = l_normal;
							p_2_R = Red_Margin[iterR];
						}
						iterR ++;
					}
					else {
						//green side
						if (iteration == (InitialIterations+1))
						{
							Grn_Margin[iterG] = p_normal - p_dev_d;
						}
						else
						{
							Grn_Margin[iterG] = Grn_Margin[iterG - 1] + 5;
						}
						Red[iteration - (InitialIterations + 1)] = false;
						Grn[iteration - (InitialIterations + 1)] = true;
						if (r.nextBoolean())
						{
							// position 0 margin,
							// position 2 deutan

							if (r.nextBoolean()) {
								L_2 = l_de_0;
								p_2_R = p_de_0;
							}
							else {
								L_2 = l_de_1;
								p_2_R = p_de_1;
							}
							L_0 = l_normal;
							p_0_R = Grn_Margin[iterG];
						}
						else
						{
							// position 0 deutan,
							// position 2 margin
							if (r.nextBoolean()) {
								L_0 = l_de_0;
								p_0_R = p_de_0;
							}
							else {
								L_0 = l_de_1;
								p_0_R = p_de_1;
							}
							L_2 = l_normal;
							p_2_R = Grn_Margin[iterG];
						}
						iterG++;
					}
					SetLED(0, L_0);
					SetLED(1, l_normal);
					SetLED(2, L_2);

					patch_0.setColorFilter(Color.rgb(p_0_R, 255 - p_0_R, 0));
					patch_1.setColorFilter(Color.rgb(p_normal, 255 - p_normal, 0));
					patch_2.setColorFilter(Color.rgb(p_2_R, 255 - p_2_R, 0));

				}
			}

		//}
		//else
		//{
		//	intest = false;
		//	Blank(false);
		//	Reset();
		//	txt_inst.setText("The anomaloscope device is not attached. Cannot proceed.");
		//	startTime2 = System.currentTimeMillis();
		//	timerHandler2.postDelayed(timerRunnable2, 0);
		//}

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