package de.floatec.mensa;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.util.ByteArrayBuffer;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.DownloadListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MensaActivity extends Activity {

	Calendar calendar = Calendar.getInstance();
	SharedPreferences prefs;
	LinearLayout ll;
	MensaReader mr;
	private Button buttondi, buttonmo, buttonmi, buttondon, buttonfr;

	private void setColorDefoult() {
		buttonmo.setBackgroundColor(Color.GRAY);
		buttondi.setBackgroundColor(Color.GRAY);
		buttonmi.setBackgroundColor(Color.GRAY);
		buttondon.setBackgroundColor(Color.GRAY);
		buttonfr.setBackgroundColor(Color.GRAY);
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		 prefs = PreferenceManager
				.getDefaultSharedPreferences(getBaseContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		TextView tvtest = (TextView) findViewById(R.id.test);
		tvtest.setText("b\n\n\nblubb");
		ll = (LinearLayout) findViewById(R.id.content);
		mr = new MensaReader();
		buttonmo = (Button) findViewById(R.id.mo);
		buttonmo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reloadUi(0);
				v.setBackgroundColor(Color.rgb(255, 127, 36));
			}
		});
		buttondi = (Button) findViewById(R.id.di);
		buttondi.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reloadUi(1);
				v.setBackgroundColor(Color.rgb(255, 127, 36));
			}
		});
		buttonmi = (Button) findViewById(R.id.mi);
		buttonmi.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reloadUi(2);
				v.setBackgroundColor(Color.rgb(255, 127, 36));
			}
		});
		buttondon = (Button) findViewById(R.id.don);
		buttondon.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reloadUi(3);
				v.setBackgroundColor(Color.rgb(255, 127, 36));
			}
		});
		buttonfr = (Button) findViewById(R.id.fr);
		buttonfr.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reloadUi(4);
				v.setBackgroundColor(Color.rgb(255, 127, 36));
			}
		});
		start();
		// reloadUi(weekday);//erst nach button intialisierung

	}
	
	
	private void start() {
		
		int weekday = calendar.get(Calendar.DAY_OF_WEEK);
		if (weekday == 1 || weekday == 7) {
			weekday = 0;
		} else {
			weekday = weekday - 2;
		}
		switch (weekday) {
		case 0:
			buttonmo.performClick();
			break;
		case 1:
			buttondi.performClick();
			break;
		case 2:
			buttonmi.performClick();
			break;
		case 3:
			buttondon.performClick();
			break;
		case 4:
			buttonfr.performClick();
			break;

		default:
			break;
		}
	}
/**
 * läd den content bereich
 * @param day gewünschter tag(0-4)
 */
	public void reloadUi(int day) {
		setColorDefoult();
		//wenn ih eistellungen cache aktiv
		if( prefs.getBoolean("cache", true)){
		mr.refrashlist();
		}else{
			mr.refrashlistWithoutCache();
		}
		//leert view
		ll.removeAllViews();
		//für Mo-Do
		if (day != 4) {
			TextView tw = new TextView(this);
			tw.setText("11:15 - 14:00 Uhr");
			tw.setTextSize(12);
			ll.addView(tw);
			//für Fr
		} else {
			TextView tw = new TextView(this);
			tw.setText("11:15 - 13:45 Uhr");
			tw.setTextSize(12);
			ll.addView(tw);
		}
		//tag auslesen
		MenuList ml = mr.readDay(day);
		TextView tw = new TextView(this);
		//gibt alle menüs aus
		for (int i = 0; i < ml.getMenuCount(); i++) {
			tw = new TextView(this);
			tw.setText(ml.getMenu(i).getTitle());
			tw.setTextSize(20);
			tw.setTextColor(Color.BLACK);
			//Fehlerfallüberprüfung
			if(ml.getMenu(i).getTitle().compareTo("ERROR")!=0){
					tw.setTextColor(Color.BLACK);
					tw.setBackgroundColor(Color.rgb(255, 127, 36));
			}else{
				tw.setTextColor(Color.WHITE);
				tw.setBackgroundColor(Color.RED);
			}
			ll.addView(tw);
			tw = new TextView(this);
			tw.setText(ml.getMenu(i).getText() + " " + ml.getMenu(i).getPrice());
			ll.addView(tw);
		}
		
	
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		
		menu.add(0, 1, 0, "Über");
		menu.add(0, 5, 0, "Einstellungen");
		menu.add(0, 3, 0, "Feedback zur App");
		menu.add(0, 4, 0, "Feedback an die Mensa");
		menu.add(0, 2, 0, "Aktuallisieren");
	
		menu.add(0, -1, 0, "Exit");

		return true;

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent browser;
		switch (item.getItemId()) {
		case 3:
			//email an entwickler
			 browser = new Intent(Intent.ACTION_VIEW,
					Uri.parse("mailto:android@floatec.de?subject=Android app:"
							+ getString(R.string.app_name) + " V."
							+ getString(R.string.app_version) + ""));
			startActivity(browser);
			return true;
		case 4:
			//öffnet ie mensa feedback seite vom studenten werk
			 browser = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://www.studentenwerk-mannheim.de/egotec/Essen+_+Trinken/Ihr+Feedback-p-32.html"));
			startActivity(browser);
			return true;
		case 2:
			mr.refrashlistWithoutCache();
			return true;
		case 1:
			Intent intent_menu_ueber = new Intent(this,
					UeberSeiteAnzeigen.class);
			startActivity(intent_menu_ueber);
			return true;
		case 5:
			Intent settingsActivity = new Intent(getBaseContext(),
					preferences.class);
			startActivity(settingsActivity);
			return true;

		case -1:
			this.finish();

			return true;

		}

		return false;

	}
}
