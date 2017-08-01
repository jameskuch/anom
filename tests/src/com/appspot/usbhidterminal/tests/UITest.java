package com.appspot.usbhidterminal.tests;

import android.test.ActivityInstrumentationTestCase2;

import com.appspot.usbhidterminal.AutoAnom;

public class UITest extends ActivityInstrumentationTestCase2<AutoAnom> {

	public UITest(){
		super(AutoAnom.class);
	}

	//@Test
	public void test() {
		//fail("Not yet implemented");
		assertNotNull("ReceiverActivity is null", null);
	}

}
