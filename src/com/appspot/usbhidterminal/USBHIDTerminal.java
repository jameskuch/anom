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

	//private EditText txtHidInput;
	private TextView txt_inst;
	private TextView txt_inst2;
	private TextView txt_inst3;
	private TextView txt_inst4;
    private TextView txt_inst5;
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
				if (separation_of_normals_PA_and_DA_complete) {
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
		Reset_Experimental_Variables();
		startTime = System.currentTimeMillis();
		timerHandler.postDelayed(timerRunnable, 0);
	}

	private void initUI() {
		setVersionToTitle();

	//	txtHidInput = (EditText) findViewById(R.id.edtxtHidInput);
		txt_inst = (TextView) findViewById(R.id.txt_inst);
        txt_inst5 = (TextView) findViewById(R.id.txt_inst5);
		txt_inst2 = (TextView) findViewById(R.id.txt_inst2);
		txt_inst3 = (TextView) findViewById(R.id.txt_inst3);
		txt_inst4 = (TextView) findViewById(R.id.txt_inst4);
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

	private void Iterate_Direct_and_Process_Experiment_Variables()
	{
		//This function always iterates first. It is called by the buttons that record the response,
		//that means the functions
		it++;


		if (it == Consts.NUMBER_OF_PRE_TRIALS && !separation_of_normals_PA_and_DA_complete)
		{
			Process_Inputs_Separation();
			separation_of_normals_PA_and_DA_complete = true;
			it = 0;
		}
		else if (!finished && separation_of_normals_PA_and_DA_complete)
		{
            //Note, this will actually generate the finish variable. So, this will run one extra time unless you intercept


			Process_Response_To_Find_Boundaries();
		}


	}

	void Process_Response_To_Find_Boundaries() {

		if (which_sc[it - 1] == Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES)
		{
            controls++;
			// this was the training case

			if (stimulus_on_patch0_and_patch1[it-1]) {
				if (lane05[it-1]==1)
				{
					Subject_Being_Trained++;
				}
				else
				{
                    if (lane15[it-1]==1)
                    {
                        Dichromat_Hits++;
                    }
					Subject_Not_Being_Trained++;
				}
			}
			else
			{
				if (lane15[it-1]==1)
				{
					Subject_Being_Trained++;
				}
				else
				{
                    if (lane05[it-1]==1)
                    {
                        Dichromat_Hits++;
                    }
					Subject_Not_Being_Trained++;

				}
			}

//			if ((Subject_Not_Being_Trained + Subject_Being_Trained) >= 10 && Subject_Not_Being_Trained > 0)
//			{
//				if (Subject_Being_Trained / Subject_Not_Being_Trained <= 0.500)
//				{
//					txt_inst.setText("Stopping trial. Please review instructions.");
//					btn_begin.setImageResource(R.drawable.button_begintest);
//					// show the response buttons
//					btn_0.setVisibility(View.INVISIBLE);
//					btn_1.setVisibility(View.INVISIBLE);
//					btn_2.setVisibility(View.INVISIBLE);
//					btn_3.setVisibility(View.INVISIBLE);
//					btn_4.setVisibility(View.INVISIBLE);
//					// set the boolean variable
//
//					Reset_Experimental_Variables();
//					Blank(false);
//
//				}
//			}
		}
		else
		{
            String msg2 = "";
			//increment the specific staircase
			//it_sc[which_sc[it-1]] ++;
            controls=0;

			if (lane05[it-1] == 1 && stimulus_on_patch0_and_patch1[it-1])
			{
				// Inside threshold
				// increase size
				InsideThreshold[which_sc[it-1]] = true;
                Separation[which_sc[it-1]] = Separation[which_sc[it-1]] * 2;

                if (Separation[which_sc[it-1]] > Consts.THRESHOLD_LARGE) {
                    sc_finished[which_sc[it - 1]] = true;
                    finished = true;
                    for (int x = 0; x < Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES; x++) {
                        if (sc_finished[x] == false) {
                            finished = false;
                        }
                    }
                }
			}
			else if (lane15[it-1] == 1 && !stimulus_on_patch0_and_patch1[it-1])
			{
				// Inside threshold
				// increase size
				InsideThreshold[which_sc[it-1]] = true;
                Separation[which_sc[it-1]] = Separation[which_sc[it-1]] * 2;

                if (Separation[which_sc[it-1]] > Consts.THRESHOLD_LARGE) {
                    sc_finished[which_sc[it - 1]] = true;
                    finished = true;
                    for (int x = 0; x < Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES; x++) {
                        if (sc_finished[x] == false) {
                            finished = false;
                        }
                    }
                }
			}
			else if (lane00[it-1] == 1 && !stimulus_on_patch0_and_patch1[it-1])
			{
				Dichromat_Hits++;
			}
			else if (lane20[it-1] == 1 && stimulus_on_patch0_and_patch1[it-1])
			{
				Dichromat_Hits++;
			}
			else if (lane00[it-1] == 1 && stimulus_on_patch0_and_patch1[it-1])
			{

                if (Consts.DEBUG_PROCESS_RESPONSE)
                {
                    msg2 += "Lane 00 and Top";
                }
				if (p_0_R > p_1_R)
				{

					MoveRightInt[which_sc[it-1]]++;
					MoveLeftInt[which_sc[it-1]]--;
					if (MoveLeftInt[which_sc[it-1]] < 0)
					{
						MoveLeftInt[which_sc[it-1]] = 0;
					}
                    if (Consts.DEBUG_PROCESS_RESPONSE)
                    {
                        msg2 += ". p0 more red";
                    }

				}
				// VERIFIED 3:49 pm.  1
				else if (p_0_R < p_1_R) //this means the 0 lane is more green than red
				{
					MoveRightInt[which_sc[it-1]]--;
					MoveLeftInt[which_sc[it-1]]++;
					if (MoveRightInt[which_sc[it-1]] < 0)
					{
						MoveRightInt[which_sc[it-1]] = 0;
					}
                    if (Consts.DEBUG_PROCESS_RESPONSE)
                    {
                        msg2 += ". p1 more red";
                    }
				}

				if (MoveRightInt[which_sc[it-1]]>=Consts.NUMBER_OF_REPEATS_BEFORE_MOVE)
				{

					MoveRight[which_sc[it-1]] = true;
					MoveRightInt[which_sc[it-1]] = 0;
					MoveLeftInt[which_sc[it-1]] = 0;
				}
				else if (MoveLeftInt[which_sc[it-1]]>=Consts.NUMBER_OF_REPEATS_BEFORE_MOVE)
				{
					MoveLeft[which_sc[it-1]] = true;
					MoveRightInt[which_sc[it-1]] = 0;
					MoveLeftInt[which_sc[it-1]] = 0;
				}

			}
			else if (lane10[it-1] == 1 && stimulus_on_patch0_and_patch1[it-1])
			{
                if (Consts.DEBUG_PROCESS_RESPONSE)
                {
                    msg2 += "Lane 10 and Top";
                }
				if (p_0_R < p_1_R)
				{
					MoveRightInt[which_sc[it-1]]++;
					MoveLeftInt[which_sc[it-1]]--;
					if (MoveLeftInt[which_sc[it-1]] < 0)
					{
						MoveLeftInt[which_sc[it-1]] = 0;
					}
                    if (Consts.DEBUG_PROCESS_RESPONSE)
                    {
                        msg2 += ". p1 more red";
                    }

				}
				else if (p_0_R > p_1_R) //this means the 0 lane is more green than red
				{
					MoveRightInt[which_sc[it-1]]--;
					MoveLeftInt[which_sc[it-1]]++;
					if (MoveRightInt[which_sc[it-1]] < 0)
					{
						MoveRightInt[which_sc[it-1]] = 0;
					}
                    if (Consts.DEBUG_PROCESS_RESPONSE)
                    {
                        msg2 += ". p0 more red";
                    }
				}

				if (MoveRightInt[which_sc[it-1]]>=Consts.NUMBER_OF_REPEATS_BEFORE_MOVE)
				{
					MoveRight[which_sc[it-1]] = true;
					MoveRightInt[which_sc[it-1]] = 0;
					MoveLeftInt[which_sc[it-1]] = 0;
				}
				else if (MoveLeftInt[which_sc[it-1]]>=Consts.NUMBER_OF_REPEATS_BEFORE_MOVE)
				{
					MoveLeft[which_sc[it-1]] = true;
					MoveRightInt[which_sc[it-1]] = 0;
					MoveLeftInt[which_sc[it-1]] = 0;
				}
			}
			else if (lane10[it-1] == 1 && !stimulus_on_patch0_and_patch1[it-1])
			{
                if (Consts.DEBUG_PROCESS_RESPONSE)
                {
                    msg2 += "Lane 10 and Bottom";
                }

				if (p_2_R > p_1_R)
				{
					MoveRightInt[which_sc[it-1]]--;
					MoveLeftInt[which_sc[it-1]]++;
					if (MoveRightInt[which_sc[it-1]] < 0)
					{
						MoveRightInt[which_sc[it-1]] = 0;
					}
                    if (Consts.DEBUG_PROCESS_RESPONSE)
                    {
                        msg2 += ". p2 more red";
                    }

				}
				//JUST VERIFIED - 3:46pm - 1
				else if (p_1_R > p_2_R) //this means the 0 lane is more green than red
				{
					MoveRightInt[which_sc[it-1]]++;
					MoveLeftInt[which_sc[it-1]]--;
					if (MoveLeftInt[which_sc[it-1]] < 0)
					{
						MoveLeftInt[which_sc[it-1]] = 0;
					}
                    if (Consts.DEBUG_PROCESS_RESPONSE)
                    {
                        msg2 += ". p1 more red";
                    }
				}

				if (MoveRightInt[which_sc[it-1]]>=Consts.NUMBER_OF_REPEATS_BEFORE_MOVE)
				{
					MoveRight[which_sc[it-1]] = true;
					MoveRightInt[which_sc[it-1]] = 0;
					MoveLeftInt[which_sc[it-1]] = 0;
				}
				else if (MoveLeftInt[which_sc[it-1]]>=Consts.NUMBER_OF_REPEATS_BEFORE_MOVE)
				{
					MoveLeft[which_sc[it-1]] = true;
					MoveRightInt[which_sc[it-1]] = 0;
					MoveLeftInt[which_sc[it-1]] = 0;
				}
			}
			else if (lane20[it-1] == 1 && !stimulus_on_patch0_and_patch1[it-1])
			{
                if (Consts.DEBUG_PROCESS_RESPONSE)
                {
                    msg2 += "Lane 20 and Bottom";
                }

				if (p_2_R < p_1_R)
				{
					MoveRightInt[which_sc[it-1]]--;
					MoveLeftInt[which_sc[it-1]]++;
					if (MoveRightInt[which_sc[it-1]] < 0)
					{
						MoveRightInt[which_sc[it-1]] = 0;
					}
                    if (Consts.DEBUG_PROCESS_RESPONSE)
                    {
                        msg2 += ". p1 more red";
                    }
				}
				//
				else if (p_2_R > p_1_R) //this means the 0 lane is more green than red
				{
					MoveRightInt[which_sc[it-1]]++;
					MoveLeftInt[which_sc[it-1]]--;
					if (MoveLeftInt[which_sc[it-1]] < 0)
					{
						MoveLeftInt[which_sc[it-1]] = 0;
					}
                    if (Consts.DEBUG_PROCESS_RESPONSE)
                    {
                        msg2 += ". p2 more red";
                    }
				}

				if (MoveRightInt[which_sc[it-1]]>=Consts.NUMBER_OF_REPEATS_BEFORE_MOVE)
				{
					MoveRight[which_sc[it-1]] = true;
					MoveRightInt[which_sc[it-1]] = 0;
					MoveLeftInt[which_sc[it-1]] = 0;
				}
				else if (MoveLeftInt[which_sc[it-1]]>=Consts.NUMBER_OF_REPEATS_BEFORE_MOVE)
				{
					MoveLeft[which_sc[it-1]] = true;
					MoveRightInt[which_sc[it-1]] = 0;
					MoveLeftInt[which_sc[it-1]] = 0;
				}
			}

            //if Left and Right boundaries are found for this particular staircase
            if (LeftBoundaryFound[which_sc[it-1]] && RightBoundaryFound[which_sc[it-1]]) {
                Separation[which_sc[it-1]] = Separation[which_sc[it-1]] / 2;
                if (Separation[which_sc[it-1]] <= Consts.FINAL_SEPARATION) {
                    sc_finished[which_sc[it-1]] = true;

                }
            }
            finished = true;
            for (int x = 0; x < Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES; x++) {
                if (!sc_finished[x])
                {
                    finished = false;
                }
            }

            if (finished) {
                int LboundaryAvg = 0;
                int RboundaryAvg = 0;

                for (int x = 0; x < Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES; x++) {
                    LboundaryAvg += LeftBoundary[x];
                    RboundaryAvg += RightBoundary[x];
                }
                LboundaryAvg = LboundaryAvg / Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES;
                RboundaryAvg = RboundaryAvg / Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES;

                txt_inst.setText("Test finished");
                //txt_inst5.setText("Left boundary = " + Integer.toString(LboundaryAvg) + ", Right boundary = " + Integer.toString(RboundaryAvg));
                txt_inst5.setText("L1 = " + Integer.toString(LeftBoundary[0]) + ", L2 = " + Integer.toString(LeftBoundary[1]) + ", R1 = " + Integer.toString(RightBoundary[0]) + ", R2 = " + Integer.toString(RightBoundary[1]));

                intest = false;
            }

			if (Consts.DEBUG_PROCESS_RESPONSE) {
				String msg = "";


				//msg = "Last: MoveRightInt = " + Integer.toString(MoveRightInt[which_sc[it-1]]) + ", MoveLeftInt = " + Integer.toString(MoveLeftInt[which_sc[it-1]]) + ", sc = " + Integer.toString(which_sc[it-1]);
				txt_inst2.setText(msg);
				//msg2 = "Last: MoveRightBoolean = " + Boolean.toString(MoveRight[which_sc[it-1]]) + ", MoveLeftBoolean = " + Boolean.toString(MoveLeft[which_sc[it-1]]);
                txt_inst3.setText(msg2);
                txt_inst.setText("Left and Right Boundary Found = " + Boolean.toString(sc_finished[which_sc[it-1]]) + ", Finished = " + Boolean.toString(finished));
				//txt_inst4.setText("Last:  Dichromat++ = " + Integer.toString(Dichromat_Hits) + " bot if");

			}


		}

	}

	public void onClick(View v) {

		if (v == btn_0) {
			//txtHidInput.setText("btn0");
			//RECORD
			lane00[it] = 1;

			Iterate_Direct_and_Process_Experiment_Variables();
            if (intest) {
                Blank(true);
            } else {
                Blank(false);
                txt_inst.setText("Finished");
            }
		}
		else if (v == btn_1) {
			//txtHidInput.setText("btn1");
			//RECORD
			lane05[it] = 1;

			Iterate_Direct_and_Process_Experiment_Variables();

            if (intest) {
                Blank(true);
            } else {
                Blank(false);
                txt_inst.setText("Finished");
            }
		}
		else if (v == btn_2) {
			//txtHidInput.setText("btn2");
			//RECORD
			lane10[it] = 1;

			Iterate_Direct_and_Process_Experiment_Variables();
            if (intest) {
                Blank(true);
            } else {
                Blank(false);
                txt_inst.setText("Finished");
            }
		}
		else if (v == btn_3) {
			//txtHidInput.setText("btn3");
			//timerHandler3.removeCallbacks(timerRunnable3);
			//RECORD
			lane15[it] = 1;

			Iterate_Direct_and_Process_Experiment_Variables();
            if (intest) {
                Blank(true);
            } else {
                Blank(false);
                txt_inst.setText("Finished");
            }
		}
		else if (v == btn_4) {
			//txtHidInput.setText("btn4");
			//startTime3 = System.currentTimeMillis();
			//timerHandler3.postDelayed(timerRunnable3, 0);
			//RECORD
			lane20[it] = 1;

			Iterate_Direct_and_Process_Experiment_Variables();
            if (intest) {
                Blank(true);
            } else {
                Blank(false);
                txt_inst.setText("Finished");
            }
		}
		else if (v == btn_begin) {
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

				Reset_Experimental_Variables();
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
                            btn_0.setVisibility(View.VISIBLE);

                            btn_2.setVisibility(View.VISIBLE);

                            btn_4.setVisibility(View.VISIBLE);
							Reset_Experimental_Variables();

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
					Reset_Experimental_Variables();
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


	private void Reset_Experimental_Variables()
	{
		for (int x = 0; x < Consts.MAX_TRIALS; x++)
		{
			lane00[x] = 0;
			lane05[x] = 0;
			lane10[x] = 0;
			lane15[x] = 0;
			lane20[x] = 0;

		}

		for (int x = 0; x < Consts.MAX_TRIALS * Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES; x++)
		{

			which_sc[x] = 0;
			sc_led_int[x] = 0;
			sc_pat_valR[x] = 0;
			sc_dir_R_to_G[x] = false;
		}
        for (int x = 0; x < Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES; x++)
        {
            sc_finished[x] = false;
			//it_sc[x] = 0;
			firsttime[x] = true;
			RightBoundaryFound[x] = false;
            RightBoundary[x] = 0;
			InsideThreshold[x] = false;
			LeftBoundaryFound[x] = false;
            LeftBoundary[x] = 0;
			Separation[x] = Consts.INITIAL_SEPARATION;
			MoveLeft[x] = false;
			MoveRight[x] = false;
			MoveLeftInt[x] = 0;
			MoveRightInt[x] = 0;
		}
        sc_finished[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES] = false;
		firsttime[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES] = true;
		Separation[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES] = Consts.INITIAL_SEPARATION;
        finished = false;

		it = 0;
        controls = 0;

		Subject_Not_Being_Trained = 0;
		Subject_Being_Trained = 0;
		Dichromat_Hits = 0;
        for (int x = 0; x < Consts.NUMBER_OF_PRE_TRIALS; x++)
		{
			position[x] = 0;
		}
		intest = false;
        separation_of_normals_PA_and_DA_complete = false;
		Probably_Normal = false;
		Probably_Protanomolous = false;
		Probably_Deuteranomolous = false;
		//Probably_Deuteranope = false;
		//Probably_Protanope = false;
	}


	// p = patch or position
	// 0, 1, 2 positions , 0 = top, 1 = middle, 2 = bottom
	// R = red, G will always be 255-R
	// dev = deviation
	// jit = jitter (variation away from the deviation (e.g. 50 - 5 or 50 + 2)
	private int p_0_R;
	private int p_1_R;
	private int p_2_R;
	private int led_0;
	private int led_1;
	private int led_2;



	private int it = 0; //iterations
	private int[] which_sc = new int[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES * Consts.MAX_TRIALS];

	private boolean[] firsttime = new boolean[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES + 1];
    private boolean[] sc_finished = new boolean[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES + 1];
    private boolean finished = false;
    private boolean intest = false;
	private boolean separation_of_normals_PA_and_DA_complete = false;

	private int[] sc_led_int = new int[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES * Consts.MAX_TRIALS];
	private int[] sc_pat_valR = new int[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES * Consts.MAX_TRIALS]; //sc = staircase. pat = patch, val = value these will be the values
    private boolean[] sc_dir_R_to_G = new boolean[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES * Consts.MAX_TRIALS];
	private boolean Probably_Normal = false;
	private boolean Probably_Protanomolous = false;
	private boolean Probably_Deuteranomolous = false;

	private int controls = 0;

	private boolean[] stimulus_on_patch0_and_patch1 = new boolean[Consts.MAX_TRIALS];
	private int[] position = new int[Consts.NUMBER_OF_PRE_TRIALS];
	private int[] lane00 = new int[Consts.MAX_TRIALS];
	private int[] lane05 = new int[Consts.MAX_TRIALS];
	private int[] lane10 = new int[Consts.MAX_TRIALS];
	private int[] lane15 = new int[Consts.MAX_TRIALS];
	private int[] lane20 = new int[Consts.MAX_TRIALS];


    private boolean[] LeftBoundaryFound = new boolean[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES];
    private int[] LeftBoundary = new int[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES];
    private boolean[] InsideThreshold = new boolean[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES];
    private boolean[] RightBoundaryFound = new boolean[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES];
    private int[] RightBoundary = new int[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES];
    private int[] Separation = new int[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES + 1];
    private boolean[] MoveLeft = new boolean[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES];
    private boolean[] MoveRight = new boolean[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES];
    private int[] MoveLeftInt = new int[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES];
    private int[] MoveRightInt = new int[Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES];

    private int Subject_Not_Being_Trained = 0;
    private int Subject_Being_Trained = 0;
    private int Dichromat_Hits = 0;


	private void Experiment() {
		//conventions 0 - 0.5 TopOfNormal
		//if (!chk_AnomAttached.isChecked()) {

		if (it >= 0 && it < Consts.NUMBER_OF_PRE_TRIALS && !separation_of_normals_PA_and_DA_complete) {
			Separate_Normals_PA_DA();
		} else if (it >= 0 && it < Consts.MAX_TRIALS && !finished) {
			Create_and_Show_Stimuli_to_find_Boundary_Thresholds();
		}

	}

	private void Create_and_Show_Stimuli_to_find_Boundary_Thresholds() {


		// every n trials with a small variation, we set a training set where the two
		// are identical. is this one of those?
		Random q = new Random();
		Random r = new Random();
		int jit = q.nextInt(2 * Consts.NUMBER_OF_TRIALS_BETWEEN_TRAINING_TRIAL_PLUS_MINUS) - Consts.NUMBER_OF_TRIALS_BETWEEN_TRAINING_TRIAL_PLUS_MINUS;
		int m = it % (Consts.NUMBER_OF_TRIALS_BETWEEN_TRAINING_TRIAL + jit);
		//txt_inst.setText(Integer.toString(m));
		if (m == 0)
		{
			//it is a canned trial!
			//txt_inst.setText("training presentation");
            which_sc[it] = Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES; //tells recording program that it's the training paradigm
		}
		else
		{
			// it is a normal trial...which staircase will we present?
			// which staircase will we present?
			//txt_inst.setText("normal presentation");
			//txt_inst.setText(Integer.toString(it));

			int sc = r.nextInt(Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES);
			// The random number generator chose a staircase, has that staircase finished? If yes,
			// then continually choose until
			while (sc_finished[sc]) {
				sc = r.nextInt(Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES);
			}
			//txt_inst.setText(Integer.toString(staircase));
			which_sc[it] = sc;
		}


		int Dichromat_Patch = 0;
		int Dichromat_LED = 0;

		if (r.nextBoolean())
		{
			Dichromat_Patch = Consts.PROTANOPE_PATCH;
			Dichromat_LED = Consts.PROTANOPE_LED;
		}
		else
		{
			Dichromat_Patch = Consts.DEUTERANOPE_PATCH;
			Dichromat_LED = Consts.DEUTERANOPE_LED;
		}




		if (Probably_Protanomolous && firsttime[which_sc[it]]) {
			firsttime[which_sc[it]] = false;
			//match with too much red light compared to normals
			if (which_sc[it] % 2 == 0) {
				sc_led_int[it] = Consts.LED_INT_PA;
				sc_pat_valR[it] = Consts.PA_RED_START_HIGH;
				sc_dir_R_to_G[it] = true;
			}
			else {
				sc_led_int[it] = Consts.LED_INT_PA;
				sc_pat_valR[it] = Consts.PA_RED_START_LOW;
				sc_dir_R_to_G[it] = false;
			}
			Separation[which_sc[it]] = Consts.INITIAL_SEPARATION;

		}
		else if (Probably_Deuteranomolous && firsttime[which_sc[it]]) {
			firsttime[which_sc[it]] = false;
			if (which_sc[it] % 2 == 0) {
				sc_led_int[it] = Consts.LED_INT_DA;
				sc_pat_valR[it] = Consts.DA_RED_START_HIGH;
				sc_dir_R_to_G[it] = true;
			}
			else {
				sc_led_int[it] = Consts.LED_INT_DA;
				sc_pat_valR[it] = Consts.DA_RED_START_LOW;
				sc_dir_R_to_G[it] = false;
			}
			Separation[which_sc[it]] = Consts.INITIAL_SEPARATION;
		}
		else if (Probably_Normal && firsttime[which_sc[it]]) {
			firsttime[which_sc[it]] = false;
			if (which_sc[it] % 2 == 0) {
				sc_led_int[it] = Consts.LED_INT_NORMAL;
				sc_pat_valR[it] = Consts.NO_RED_START_HIGH;
				sc_dir_R_to_G[it] = true;
			}
			else {
				sc_led_int[it] = Consts.LED_INT_NORMAL;
				sc_pat_valR[it] = Consts.NO_RED_START_LOW;
				sc_dir_R_to_G[it] = false;
			}
			Separation[which_sc[it]] = Consts.INITIAL_SEPARATION;

		}
		else {
			if (which_sc[it] % 2 == 0) {
				sc_dir_R_to_G[it] = true;
			}
			else {
				sc_dir_R_to_G[it] = false;
			}
			sc_led_int[it] = sc_led_int[it-1];
            if (which_sc[it]<Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES) {

                    //continue processing
                int valtoassign;
                if (!sc_finished[which_sc[it]]) {
                    if (MoveLeft[which_sc[it - 1 - controls]]) {
                        valtoassign = sc_pat_valR[it - 1 - controls] - Separation[which_sc[it - 1 - controls]];
                        if (valtoassign < 0) {
                            //extreme case, probably a dichromat
                            valtoassign = 0;
                            LeftBoundaryFound[which_sc[it]] = true;
                            LeftBoundary[which_sc[it]] = 0;
                        } else {
                            RightBoundaryFound[which_sc[it]] = true;
                            RightBoundary[which_sc[it]] = sc_pat_valR[it - 1 - controls];
                        }
                        sc_pat_valR[it] = valtoassign;
                        MoveLeft[which_sc[it - 1 - controls]] = false;
                    } else if (MoveRight[which_sc[it - 1 - controls]]) {
                        valtoassign = sc_pat_valR[it - 1 - controls] + Separation[which_sc[it - 1 - controls]];
                        if (valtoassign > 255) {
                            valtoassign = 255;
                            RightBoundaryFound[which_sc[it]] = true;
                            RightBoundary[which_sc[it]] = 255;
                        } else {
                            LeftBoundaryFound[which_sc[it]] = true;
                            LeftBoundary[which_sc[it]] = sc_pat_valR[it - 1 - controls];
                        }
                        sc_pat_valR[it] = valtoassign;
                        MoveRight[which_sc[it - 1 - controls]] = false;
                    } else {
                        sc_pat_valR[it] = sc_pat_valR[it - 1 - controls];
                    }
                }
            }
			else {
				sc_pat_valR[it] = sc_pat_valR[it-1];
			}
		}


        // We now have our initial guesses. Set LEDs and PATCH colors for all three areas
        if (r.nextBoolean()) {
            stimulus_on_patch0_and_patch1[it] = true;
        } else {
            stimulus_on_patch0_and_patch1[it] = false;
        }
        int val = stimulus_on_patch0_and_patch1[it] ? 1 : 0;
        int valnot = stimulus_on_patch0_and_patch1[it] ? 0 : 1;

        if (which_sc[it] == Consts.NUMBER_OF_SIMULTANEOUS_STAIRCASES) {
            p_1_R = sc_pat_valR[it];
        } else {
            if (sc_dir_R_to_G[it]) {
                p_1_R = sc_pat_valR[it] - Separation[which_sc[it]];
            } else {
                p_1_R = sc_pat_valR[it] + Separation[which_sc[it]];
            }
        }
        led_1 = sc_led_int[it];
        p_0_R = valnot * Dichromat_Patch + val * sc_pat_valR[it];
        p_2_R = val * Dichromat_Patch + valnot * sc_pat_valR[it];
        led_0 = valnot * Dichromat_LED + val * sc_led_int[it];
        led_2 = val * Dichromat_LED + valnot * sc_led_int[it];

        //txt_inst.setText(Boolean.toString(Probably_Normal) + " " +  Integer.toString(led_0) + " " + Integer.toString(led_1) + " " + Integer.toString(led_2) + " " + Integer.toString(p_0_R) + " " + Integer.toString(p_1_R) + " " + Integer.toString(p_2_R));
        if (Consts.DEBUG_ALTERNATE_0) {
            String msg = "";
            if (Probably_Protanomolous) {
                msg = "prot, p0 = ";
            } else if (Probably_Deuteranomolous) {
                msg = "deut, p0 = ";
            } else {
                msg = "normal, p0 = ";
            }
            //txt_inst.setText(msg + Integer.toString(p_0_R) + ", p1 = " + Integer.toString(p_1_R) + ", p2 = " + Integer.toString(p_2_R) + ", sc = " + Integer.toString(which_sc[it]) + ", it = " + Integer.toString(it) + ", Separation = " + Integer.toString(Separation[which_sc[it]]));
            txt_inst.setText(msg + Integer.toString(p_0_R) + ", p1 = " + Integer.toString(p_1_R) + ", p2 = " + Integer.toString(p_2_R) + ", sc = " + Integer.toString(which_sc[it]) + " Separation = " + Integer.toString(Separation[which_sc[it]]));
            txt_inst5.setText("Top = " + Boolean.toString(stimulus_on_patch0_and_patch1[it]) + ", sc_patch_ValR = " + Integer.toString(sc_pat_valR[it]));
        }

        if (Consts.DEBUG_ALTERNATE_1)
        {
            String msg = "l_0 = " + Integer.toString(led_0) + ", l_1 = " + Integer.toString(led_1) + ", l_2 = " + Integer.toString(led_2) + ", p_0_R = " + Integer.toString(p_0_R) + ", p_1_R = " + Integer.toString(p_1_R) + ", p_2_R = " + Integer.toString(p_2_R);
            txt_inst5.setText(msg);

        }




        Output_LED_and_PATCH(led_0, led_1, led_2, p_0_R, p_1_R, p_2_R);
	}


//		int L_0;
//		int L_2;
//		Random r = new Random();
//		//
//		if (r.nextBoolean()) {
//			//red side
//
//			if (iterR == 0) {
//				Red_Margin[iterR] = p_normal + p_dev_p;
//			} else {
//				Red_Margin[iterR] = Red_Margin[iterR - 1] - 5;
//			}
//			Red[iteration - (InitialIterations + 1)] = true;
//			Grn[iteration - (InitialIterations + 1)] = false;
//
//			if (r.nextBoolean()) {
//				// position 0 margin,
//				// position 2 protan
//
//				if (r.nextBoolean()) {
//					L_2 = l_pr_0;
//					p_2_R = p_pr_0;
//				} else {
//					L_2 = l_pr_1;
//					p_2_R = p_pr_1;
//				}
//				L_0 = l_normal;
//				p_0_R = Red_Margin[iterR];
//			} else {
//				// position 0 protan,
//				// position 2 margin
//				if (r.nextBoolean()) {
//					L_0 = l_pr_0;
//					p_0_R = p_pr_0;
//				} else {
//					L_0 = l_pr_1;
//					p_0_R = p_pr_1;
//				}
//				L_2 = l_normal;
//				p_2_R = Red_Margin[iterR];
//			}
//			iterR++;
//		} else {
//			//green side
//			if (iteration == (InitialIterations + 1)) {
//				Grn_Margin[iterG] = p_normal - p_dev_d;
//			} else {
//				Grn_Margin[iterG] = Grn_Margin[iterG - 1] + 5;
//			}
//			Red[iteration - (InitialIterations + 1)] = false;
//			Grn[iteration - (InitialIterations + 1)] = true;
//			if (r.nextBoolean()) {
//				// position 0 margin,
//				// position 2 deutan
//
//				if (r.nextBoolean()) {
//					L_2 = l_de_0;
//					p_2_R = p_de_0;
//				} else {
//					L_2 = l_de_1;
//					p_2_R = p_de_1;
//				}
//				L_0 = l_normal;
//				p_0_R = Grn_Margin[iterG];
//			} else {
//				// position 0 deutan,
//				// position 2 margin
//				if (r.nextBoolean()) {
//					L_0 = l_de_0;
//					p_0_R = p_de_0;
//				} else {
//					L_0 = l_de_1;
//					p_0_R = p_de_1;
//				}
//				L_2 = l_normal;
//				p_2_R = Grn_Margin[iterG];
//			}
//			iterG++;
//		}
//		SetLED(0, L_0);
//		SetLED(1, l_normal);
//		SetLED(2, L_2);
//
//		patch_0.setColorFilter(Color.rgb(p_0_R, 255 - p_0_R, 0));
//		patch_1.setColorFilter(Color.rgb(p_normal, 255 - p_normal, 0));
//		patch_2.setColorFilter(Color.rgb(p_2_R, 255 - p_2_R, 0));



	private void Separate_Normals_PA_DA()
	{
//		public static final int PA_RED_START_L = 50;
//		public static final int PA_RED_START_R = 255;
//		public static final int DA_RED_START_L = 50;
//		public static final int DA_RED_START_R = 255;
//
//		public static final int LED_INT_NORMAL = 200;
//		public static final int LED_INT_DA = 200;
//		public static final int LED_INT_PA = 200;
//		public static final int PATCH_RED_NORMAL = 179;
//		public static final int PATCH_RED_DA = 139; //deuteranomolous trichromats are less sensitive to GRN
//		public static final int PATCH_RED_PA = 229; //protanomolous trichromats are less sensitive to RED
//		public static final int NUMBER_OF_PRE_TRIALS = 3;

		Random r = new Random();
		double rr = 59 * r.nextDouble();

		int[] pn = new int[3]; int[] pp = new int[3]; int[] pd = new int[3];
		if (rr >= 0 && rr < 10)
		{
			// 0 norm
			// 1 prot
			// 2 deut
			position[it] = 0;
			pn[0] = 1;  pp[0] = 0;  pd[0] = 0;
			pn[1] = 0;  pp[1] = 1;  pd[1] = 0;
			pn[2] = 0;  pp[2] = 0;  pd[2] = 1;

		}
		else if (rr >= 10 && rr < 20)
		{
			// 0 norm
			// 1 deut
			// 2 prot
			position[it] = 1;
			pn[0] = 1;  pp[0] = 0;  pd[0] = 0;
			pn[1] = 0;  pp[1] = 0;  pd[1] = 1;
			pn[2] = 0;  pp[2] = 1;  pd[2] = 0;
		}
		else if (rr >= 20 && rr < 30)
		{
			// 0 prot
			// 1 normal
			// 2 deut
			position[it] = 2;
			pn[0] = 0;  pp[0] = 1;  pd[0] = 0;
			pn[1] = 1;  pp[1] = 0;  pd[1] = 0;
			pn[2] = 0;  pp[2] = 0;  pd[2] = 1;
		}
		else if (rr >= 30 && rr < 40)
		{
			// 0 deut
			// 1 norm
			// 2 prot
			position[it] = 3;
			pn[0] = 0;  pp[0] = 0;  pd[0] = 1;
			pn[1] = 1;  pp[1] = 0;  pd[1] = 0;
			pn[2] = 0;  pp[2] = 1;  pd[2] = 0;
		}
		else if (rr >= 40 && rr < 50)
		{
			// 0 prot
			// 1 deut
			// 2 norm
			position[it] = 4;
			pn[0] = 0;  pp[0] = 1;  pd[0] = 0;
			pn[1] = 0;  pp[1] = 0;  pd[1] = 1;
			pn[2] = 1;  pp[2] = 0;  pd[2] = 0;
		}
		else
		{
			// 0 deut
			// 1 prot
			// 2 norm
			position[it] = 5;
			pn[0] = 0;  pp[0] = 0;  pd[0] = 1;
			pn[1] = 0;  pp[1] = 1;  pd[1] = 0;
			pn[2] = 1;  pp[2] = 0;  pd[2] = 0;
		}

		p_0_R = pn[0] * Consts.PATCH_RED_NORMAL +
				pp[0] * Consts.PATCH_RED_PA +
				pd[0] * Consts.PATCH_RED_DA;
		p_1_R = pn[1] * Consts.PATCH_RED_NORMAL +
				pp[1] * Consts.PATCH_RED_PA +
				pd[1] * Consts.PATCH_RED_DA;
		p_2_R = pn[2] * Consts.PATCH_RED_NORMAL +
				pp[2] * Consts.PATCH_RED_PA +
				pd[2] * Consts.PATCH_RED_DA;

		led_0 = pn[0] * Consts.LED_INT_NORMAL +
				pp[0] * Consts.LED_INT_PA +
				pd[0] * Consts.LED_INT_DA;
		led_1 = pn[1] * Consts.LED_INT_NORMAL +
				pp[1] * Consts.LED_INT_PA +
				pd[1] * Consts.LED_INT_DA;
		led_2 = pn[2] * Consts.LED_INT_NORMAL +
				pp[2] * Consts.LED_INT_PA +
				pd[2] * Consts.LED_INT_DA;


		Output_LED_and_PATCH(led_0, led_1, led_2, p_0_R, p_1_R, p_2_R);

	}

	private void Process_Inputs_Separation(){
		double deut_anom = 0;
		double prot_anom = 0;
		double normal = 0;
		for (int x = 0; x < Consts.NUMBER_OF_PRE_TRIALS; x++)
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
        String msg = "";
		if (deut_anom > normal)
		{
			Probably_Deuteranomolous = true;
            msg = "Deuteranomolous, Prot = ";

		}
		else if (prot_anom > normal)
		{
			Probably_Protanomolous = true;
            msg = "Protanomolous, Prot = ";

		}
		else
		{
			Probably_Normal = true;
            msg = "Normal, Prot = ";

		}
        msg += Double.toString(prot_anom) + ", Deut = " + Double.toString(deut_anom) + ", Norm = " + Double.toString(normal);
        txt_inst.setText(msg);
        startTime2 = System.currentTimeMillis();
        timerHandler2.postDelayed(timerRunnable2, 0);


	}


private int temp = 0;
	private void Output_LED_and_PATCH(int l0, int l1, int l2, int p0, int p1, int p2){

        txt_inst4.setText(Integer.toString(temp));
        temp++;
		SetLED(0, l0);
		SetLED(1, l1);
		SetLED(2, l2);
        patch_1.setColorFilter(Color.rgb(p1, 255 - p1, 0));

        if (Consts.DEBUG_NO_DICHROMATIC_PATCH) {

			patch_0.setColorFilter(Color.rgb(p0, p0, 0));
		}
		else {
			patch_0.setColorFilter(Color.rgb(p0, 255 - p0, 0));
		}

        if (Consts.DEBUG_NO_DICHROMATIC_PATCH) {
			patch_2.setColorFilter(Color.rgb(p2, p2, 0));
		}
		else {
			patch_2.setColorFilter(Color.rgb(p2, 255 - p2, 0));
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