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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.apache.http.util.ByteArrayBuffer;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.text.ClipboardManager;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.webkit.DownloadListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MensaActivity extends Activity implements OnTouchListener {
	private AdView adView;
	private int activDay;
	OnClickListener l;
	ImageButton oeffnungszeiten;
	private String weeks[];
	Calendar calendar = Calendar.getInstance();
	SharedPreferences prefs;
	LinearLayout contentLayout;
	MensaReader mr;
	private Button buttondi, buttonmo, buttonmi, buttondon, buttonfr;
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat("dd.MM.yyyy");
	  private float downXValue;
	

	 public boolean onTouch(View arg0, MotionEvent arg1) {

	        // Get the action that was done on this touch event
	        switch (arg1.getAction())
	        {
	            case MotionEvent.ACTION_DOWN:
	            {
	                // store the X value when the user's finger was pressed down
	                downXValue = arg1.getX();
	                break;
	            }
	            case MotionEvent.ACTION_CANCEL:
	            case MotionEvent.ACTION_UP:
	            {
	                // Get the X value when the user released his/her finger
	                float currentX = arg1.getX();            

	                // going backwards: pushing stuff to the right
	                if (downXValue + 30 < currentX &&activDay>0)
	                {
	                	activDay=(activDay-1);
	                }

	                // going forwards: pushing stuff to the left
	                if (downXValue - 30 > currentX&&activDay<4)
	                {
	                	activDay=(activDay+1);	  
	                	 
	                }
	                reloadUi();
	                break;
	            }
	        }

	        // if you return false, these actions will not be recorded
	        return true;
	    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// user has long pressed your TextView
		menu.add(0, v.getId(), 0, "Kopieren");

		// cast the received View to TextView so that you can get its text
		TextView yourTextView = (TextView) v;

		// place your TextView's text in clipboard
		ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		clipboard.setText(yourTextView.getText());
	}

	/**
	 * set button color to default
	 */
	private void setButtonColorDefoult() {

		buttonmo.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.btn_not_selected));
		buttondi.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.btn_not_selected));
		buttonmi.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.btn_not_selected));
		buttondon.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.btn_not_selected));
		buttonfr.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.btn_not_selected));
	}
	
	/**
	 * set button color to sleected
	 */
	private void setButtonColor(int day) {
		switch (day){
			case 0:
			buttonmo.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.btn_selected));
				break;
			case 1:
					buttondi.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.btn_selected));
					break;
			case 2:
					buttonmi.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.btn_selected));
					break;
			case 3:
					buttondon.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.btn_selected));
					break;
			case 4:
						buttonfr.setBackgroundDrawable(getResources().getDrawable(
				R.drawable.btn_selected));
					break;
				default:
					Context context = getApplicationContext();
					CharSequence text = "swype error";
					int duration = Toast.LENGTH_SHORT;

					Toast toast = Toast.makeText(context, text, duration);
					toast.show();
		
			
		}
	}
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		 // Add these two lines
        LinearLayout layMain = (LinearLayout) findViewById(R.id.linearLayout3);
        layMain.setOnTouchListener((OnTouchListener) this);
		mr = new MensaReader();

		// Create the adView
		adView = new AdView(this, AdSize.BANNER,
				"/14148428/ca-pub-3723428902598385/Phone_Android_Mannheim");

		// Lookup your LinearLayout assuming it’s been given
		// the attribute android:id="@+id/mainLayout"
		LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayout3);
		
		// Add the adView to it
		if (prefs.getBoolean("add", true)) {
		layout.addView(adView);
		}
		// Initiate a generic request to load it with an ad
		adView.loadAd(new AdRequest());
		/* init spinner */
		weeks = new String[3];
		Calendar myCalMo = Calendar.getInstance();
		Calendar myCalFr = Calendar.getInstance();
		myCalMo.add(myCalMo.DAY_OF_MONTH, -myCalMo.get(myCalMo.DAY_OF_WEEK) + 2);
		myCalFr.add(myCalFr.DAY_OF_MONTH, -myCalFr.get(myCalFr.DAY_OF_WEEK) + 6);
		if (calendar.get(Calendar.DAY_OF_WEEK) == 7) {
			myCalMo.add(myCalMo.DAY_OF_MONTH, +7);
			myCalFr.add(myCalFr.DAY_OF_MONTH, +7);
		}
		weeks[0] = FORMAT.format(myCalMo.getTime()) + " - "
				+ FORMAT.format(myCalFr.getTime());
		myCalMo.add(myCalMo.DAY_OF_MONTH, +7);

		myCalFr.add(myCalFr.DAY_OF_MONTH, +7);
		weeks[1] = FORMAT.format(myCalMo.getTime()) + " - "
				+ FORMAT.format(myCalFr.getTime());
		myCalMo.add(myCalMo.DAY_OF_MONTH, +7);
		myCalFr.add(myCalFr.DAY_OF_MONTH, +7);
		weeks[2] = FORMAT.format(myCalMo.getTime()) + " - "
				+ FORMAT.format(myCalFr.getTime());
		Spinner s = (Spinner) findViewById(R.id.week);
		ArrayAdapter adapter = new ArrayAdapter(this,
				android.R.layout.simple_spinner_item, weeks);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);
		
		s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				mr.setWeekOffset(pos);
				reloadUi();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		/* intialize gui objekts */
		contentLayout = (LinearLayout) findViewById(R.id.content);

		buttonmo = (Button) findViewById(R.id.mo);
		buttonmo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reloadUi(0);
				setButtonColorDefoult();
				v.setBackgroundColor(Color.rgb(255, 127, 36));
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_selected));
			}
		});
		buttondi = (Button) findViewById(R.id.di);
		buttondi.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reloadUi(1);
				setButtonColorDefoult();
				v.setBackgroundColor(Color.rgb(255, 127, 36));
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_selected));
			}
		});
		buttonmi = (Button) findViewById(R.id.mi);
		buttonmi.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reloadUi(2);
				setButtonColorDefoult();
				v.setBackgroundColor(Color.rgb(255, 127, 36));
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_selected));
			}
		});
		buttondon = (Button) findViewById(R.id.don);
		buttondon.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reloadUi(3);
				setButtonColorDefoult();
				v.setBackgroundColor(Color.rgb(255, 127, 36));
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_selected));
			}
		});
		buttonfr = (Button) findViewById(R.id.fr);
		buttonfr.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				reloadUi(4);
				setButtonColorDefoult();
				v.setBackgroundColor(Color.rgb(255, 127, 36));
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_selected));
			}
		});
		initContent();

		oeffnungszeiten = (ImageButton) findViewById(R.id.oefnungszeiten);
		l = new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				showOeffnungszeiten();
			}
		};
		oeffnungszeiten.setOnClickListener(l);
	}

	/**
	 * initialisiert den content bereich beim start
	 */
	private void initContent() {

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
	 * reloads the last selected da
	 */
	public void reloadUi() {
		reloadUi(activDay);
	}

	/**
	 * läd den content bereich (neu)
	 * 
	 * @param day
	 *            gewünschter tag(0-4)
	 */
	public void reloadUi(int day) {
		setButtonColorDefoult();
		setButtonColor(day);
		activDay = day;

		// wenn in eistellungen cache aktiv
		if (prefs.getBoolean("cache", true)) {
			mr.refrashlist();
		} else {
			mr.refrashlistWithoutCache();
		}
		// leert view
		contentLayout.removeAllViews();
		TextView twTime = new TextView(this);
		// für Mo-Do
		if (day != MensaReader.DAY_FR) {
			twTime.setText("11:15 - 14:00 Uhr");

			// für Fr
		} else {

			twTime.setText("11:15 - 13:45 Uhr");

		}
		twTime.setPadding(5, 1, 5, 1);
		twTime.setTextSize(12);
		//contentLayout.addView(twTime);
		// tag auslesen
		MenuList ml = mr.readDay(day);
		TextView tw = new TextView(this);
		// gibt alle menüs aus
		for (int i = 0; i < ml.getMenuCount(); i++) {
			tw = new TextView(this);
			tw.setText(ml.getMenu(i).getTitle());
			tw.setTextSize(20);
			tw.setTextColor(Color.BLACK);
			// Fehlerfallüberprüfung
			if (ml.getMenu(i).getTitle().compareTo("ERROR") != 0) {
				tw.setTextColor(Color.BLACK);
				tw.setBackgroundColor(Color.rgb(255, 127, 36));
				tw.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.bg_header));
				tw.setPadding(5, 1, 5, 1);
			} else {
				tw.setTextColor(Color.WHITE);
				tw.setBackgroundColor(Color.RED);
				tw.setPadding(5, 1, 5, 1);
				tw.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.bg_header_error));
			}
			contentLayout.addView(tw);
			tw = new TextView(this);
			tw.setText(ml.getMenu(i).getText() + " " + ml.getMenu(i).getPrice());
			tw.setPadding(5, 1, 5, 1);
			tw.setFocusable(true);
			//registerForContextMenu(tw);
			contentLayout.addView(tw);
		}
		View line = new View(this);
		line.setBackgroundColor(Color.LTGRAY);
		line.setMinimumHeight(5);
		contentLayout.addView(line);
		tw = new TextView(this);
		tw.setText("Änderungen vorbehalten!");
		tw.setPadding(5, 1, 5, 1);
		tw.setFocusable(true);
		contentLayout.addView(tw);

	}

	public void showZusatzstoffe() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Zusatzstoffe");
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setMessage("KENNZEICHNUNGSPFLICHTIGE ZUSATZSTOFFE:	\nS Schweinefleisch	\nVeg Vegetarisch	\n1 mit Farbstoff	\n2 mit Konservierungsstoff	  \n3 mit Antioxidationsmittel	  \n4 mit Geschmacksverstärker	  \n5 geschwefelt\n6 geschwärzt	\n7 gewachst	\n8 mit Phosphat	\n9 mit Säuerungsmittel	\n10 enthält eine Phenylalaninquelle	\n13 enthält Natriumnitrit	\n14 Bio-Kontrollnummer: DE-ÖKO-007")

		;
		AlertDialog alert = builder.create();
		alert.show();
	}

	public void showOeffnungszeiten() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Öffnungszeiten");
		builder.setIcon(android.R.drawable.ic_menu_recent_history);
		SpannableString s= new SpannableString("Mensa Hochschule Mannheim:\nGebäude J OG.1\nMontag - Donnerstag\n11:15 - 14:00 Uhr\nFreitag\n11:15 - 13:45 Uhr\n\nCafé Integral:\nGebäude J EG.\nMontag - Donnerstag\n7:45 - 16:00 Uhr\nFreitag\n7:45 - 15:30 Uhr\n\nCafe Sonnendeck:\nGebäude H OG. 7\nMontag - Donnerstag\n7:30 - 15:45 Uhr\nFreitag\n7:30 - 13:45 Uhr\nDer Raum ist zugänglich und die Kaffeemaschine dienstbereit von 7:30 - 18:30 Uhr.");
		 s.setSpan(new StyleSpan(Typeface.BOLD), 0, 27, 0);
		 s.setSpan(new StyleSpan(Typeface.BOLD), 115-8, 129-8, 0);
		 s.setSpan(new StyleSpan(Typeface.BOLD), 214-16, 231-16, 0);
		builder.setMessage(s);
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	

	public boolean onCreateOptionsMenu(Menu menu) {

		menu.add(0, 3, 0, "Feedback zur App").setIcon(
				android.R.drawable.ic_menu_send);
		menu.add(0, 4, 0, "Feedback an die Mensa").setIcon(
				android.R.drawable.ic_menu_send);
		menu.add(0, 5, 0, "Zusatzstoffe").setIcon(
				android.R.drawable.ic_menu_help);

		menu.add(0, 2, 0, "Aktuallisieren").setIcon(
				android.R.drawable.ic_menu_rotate);
		menu.add(0, 6, 0, "Spenden").setIcon(android.R.drawable.ic_menu_view);
		menu.add(0, 7, 0, "Einstellungen").setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(0, 1, 0, "Über").setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(0, -1, 0, "Exit").setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);

		return true;

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent browser;
		switch (item.getItemId()) {
		case 3:
			// email an entwickler
			browser = new Intent(Intent.ACTION_VIEW,
					Uri.parse("mailto:android@floatec.de?subject=Android app:"
							+ getString(R.string.app_name) + " V."
							+ getString(R.string.app_version) + ""));
			startActivity(browser);
			return true;
		case 4:
			// öffnet ie mensa feedback seite vom studenten werk
			browser = new Intent(
					Intent.ACTION_VIEW,
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
		case 7:
			Intent intent_menu_settings = new Intent(this, preferences.class);
			startActivity(intent_menu_settings);
			return true;
		case 5:
			showZusatzstoffe();
			return true;
		case 6:
			Intent browser2 = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://floatec.de/donate.html"));
			startActivity(browser2);
			return true;
		case -1:
			this.finish();

			return true;

		}

		return false;

	}
}
