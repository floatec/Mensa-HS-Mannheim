package de.floatec.mensa;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MensaActivity extends Activity {
	private float downXValue;
	private MensaReader mr;
	private ViewFlipper vf;
	private String weeks[];
	private int activeDay = 0;
	private Calendar calendar = Calendar.getInstance();
	private SharedPreferences prefs;
	private Button buttondi, buttonmo, buttonmi, buttondon, buttonfr;
	private Spinner s;
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat(
			"dd.MM.yyyy");
	private ImageButton oeffnungszeiten;
	private ProgressDialog pd;

	private void flipTo(int day) {
		int way;
		way = activeDay < day ? -1 : 1;
		while (day != activeDay % 5) {
			if (way == 1) {
				activeDay--;

				vf.setAnimation(AnimationUtils.loadAnimation(this,
						android.R.anim.fade_out));
				vf.showNext();
			} else {
				activeDay++;
				vf.setAnimation(AnimationUtils.loadAnimation(this,
						android.R.anim.fade_out));
				vf.showPrevious();
			}

		}
		setButtonColorDefoult();
		setButtonColor(activeDay);
	}

	/**
	 * initialisiert den content bereich beim start
	 */
	private void initFlip() {

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

	private class RefrashDatas extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			if (urls[0].equals("CACHE")&& prefs.getBoolean("cache", true)) {
				mr.refrashlist();
			} else {
				mr.refrashlistWithoutCache();
			}
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			loadUI();
			pd.dismiss();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		setContentView(R.layout.main);
		vf = (ViewFlipper) findViewById(R.id.viewFlipper1);
		LinearLayout layMain = (LinearLayout) findViewById(R.id.mov);
		OnTouchListener ot = new OnTouchListener() {

			public boolean onTouch(View arg0, MotionEvent arg1) {

				// Get the action that was done on this touch event
				switch (arg1.getAction()) {
				case MotionEvent.ACTION_DOWN: {
					// store the X value when the user's finger was pressed down
					downXValue = arg1.getX();
					break;
				}
				case MotionEvent.ACTION_CANCEL:
				case MotionEvent.ACTION_UP: {
					// Get the X value when the user released his/her finger
					float currentX = arg1.getX();

					int oldDay = activeDay;
					// going backwards: pushing stuff to the right
					if (downXValue + 30 < currentX) {
						if (activeDay > 0) {
							setButtonColorDefoult();
							setButtonColor(--activeDay);
							vf.setAnimation(AnimationUtils.loadAnimation(
									getBaseContext(), android.R.anim.fade_out));
							vf.showPrevious();
						} else {
							if (s.getSelectedItemPosition() != 0) {
								flipTo(4);

								s.setSelection(s.getSelectedItemPosition() - 1);

							}
						}
					}

					// going forwards: pushing stuff to the left
					if (downXValue - 30 > currentX) {
						if (activeDay < 4) {
							setButtonColorDefoult();
							setButtonColor(++activeDay);
							vf.setAnimation(AnimationUtils.loadAnimation(
									getBaseContext(), android.R.anim.fade_out));
							vf.showNext();
						} else {
							if (s.getSelectedItemPosition() < 2
									|| (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
											&& calendar
													.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && s
											.getSelectedItemPosition() < 3)) {
								flipTo(0);
								s.setSelection(s.getSelectedItemPosition() + 1);

							}
						}

					}
					break;
				}
				}

				// if you return false, these actions will not be recorded
				return true;
			}
		};
		layMain.setOnTouchListener(ot);

		layMain = (LinearLayout) findViewById(R.id.div);
		layMain.setOnTouchListener(ot);

		layMain = (LinearLayout) findViewById(R.id.miv);
		layMain.setOnTouchListener(ot);

		layMain = (LinearLayout) findViewById(R.id.dov);
		layMain.setOnTouchListener(ot);

		layMain = (LinearLayout) findViewById(R.id.frv);
		layMain.setOnTouchListener(ot);
		mr = new MensaReader();

		// intialize gui objekts

		buttonmo = (Button) findViewById(R.id.mo);
		buttonmo.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				flipTo(MensaReader.DAY_MO);
				setButtonColorDefoult();
				v.setBackgroundColor(Color.rgb(255, 127, 36));
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_selected));
			}
		});
		buttondi = (Button) findViewById(R.id.di);
		buttondi.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				flipTo(MensaReader.DAY_DI);
				setButtonColorDefoult();
				v.setBackgroundColor(Color.rgb(255, 127, 36));
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_selected));
			}
		});
		buttonmi = (Button) findViewById(R.id.mi);
		buttonmi.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				flipTo(MensaReader.DAY_MI);
				setButtonColorDefoult();
				v.setBackgroundColor(Color.rgb(255, 127, 36));
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_selected));
			}
		});
		buttondon = (Button) findViewById(R.id.don);
		buttondon.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				flipTo(MensaReader.DAY_DO);
				setButtonColorDefoult();
				v.setBackgroundColor(Color.rgb(255, 127, 36));
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_selected));
			}
		});
		buttonfr = (Button) findViewById(R.id.fr);
		buttonfr.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {

				flipTo(MensaReader.DAY_FR);
				setButtonColorDefoult();
				v.setBackgroundColor(Color.rgb(255, 127, 36));
				v.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.btn_selected));
			}
		});

		oeffnungszeiten = (ImageButton) findViewById(R.id.oefnungszeiten);

		oeffnungszeiten.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				showOeffnungszeiten();
			}
		});
		// init spinner
		if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY
				|| calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
			weeks = new String[3];
		} else {
			weeks = new String[4];
		}
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
		if (!(calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || calendar
				.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
			myCalMo.add(myCalMo.DAY_OF_MONTH, +7);
			myCalFr.add(myCalFr.DAY_OF_MONTH, +7);
			weeks[3] = FORMAT.format(myCalMo.getTime()) + " - "
					+ FORMAT.format(myCalFr.getTime());
		}
		s = (Spinner) findViewById(R.id.week);
		ArrayAdapter adapter = new ArrayAdapter(this,
				android.R.layout.simple_spinner_item, weeks);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		s.setAdapter(adapter);

		s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				mr.setWeekOffset(pos);
				refrashDatas();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		initFlip();
	}
	void refrashDatas(){
		refrashDatas(true);
	}
	void refrashDatas(boolean useCache) {

		pd = ProgressDialog.show(this, "Laden...",
				"Daten werden Heruntergeladen", true, true);
		RefrashDatas task = new RefrashDatas();
		if(useCache)

			task.execute(new String[] { "CACHE" });
		else
			task.execute(new String[] { "NOCACHE" });
	
	}

	private void loadUI() {
		for (int day = 0; day < 5; day++) {
			boolean refrashButton=false;
			MenuList ml = mr.readDay(day);
			TextView tw = new TextView(this);
			LinearLayout contentLayout = (LinearLayout) ((ScrollView) ((LinearLayout) vf
					.getChildAt(day)).getChildAt(0)).getChildAt(0);
			// leert view
			contentLayout.removeAllViews();
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
					refrashButton=true;
					tw.setTextColor(Color.WHITE);
					tw.setBackgroundColor(Color.RED);
					tw.setPadding(5, 1, 5, 1);
					tw.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.bg_header_error));
				}
				contentLayout.addView(tw);
				tw = new TextView(this);
				tw.setText(ml.getMenu(i).getText() + " "
						+ ml.getMenu(i).getPrice());
				tw.setPadding(5, 1, 5, 1);
				tw.setFocusable(true);
				// registerForContextMenu(tw);
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
			ImageButton ib=new ImageButton(getBaseContext());
			ib.setBackgroundResource(R.drawable.btn_not_selected);
			ib.setImageResource(android.R.drawable.ic_menu_rotate);
			ib.setOnClickListener(new OnClickListener() {
				
				public void onClick(View v) {
					refrashDatas() ;
					
				}
			});
			if(refrashButton){
				contentLayout.addView(ib);
			}
			try{
			AdView adView;
			adView = new AdView(this, AdSize.BANNER,
					"/14148428/ca-pub-3723428902598385/Phone_Android_Mannheim");
			// Lookup your LinearLayout assuming it’s been given
			// the attribute android:id="@+id/mainLayout"

			// Add the adView to it
			if (prefs.getBoolean("add", true)) {
				contentLayout.addView(adView);
			}
			// Initiate a generic request to load it with an ad
			adView.loadAd(new AdRequest());
			}catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	public void showOeffnungszeiten() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Öffnungszeiten");
		builder.setIcon(android.R.drawable.ic_menu_recent_history);
		SpannableString s = new SpannableString(
				"Mensa Hochschule Mannheim:\nGebäude J OG.1\nMontag - Donnerstag\n11:15 - 14:00 Uhr\nFreitag\n11:15 - 13:45 Uhr\n\nCafé Integral:\nGebäude J EG.\nMontag - Donnerstag\n7:45 - 16:00 Uhr\nFreitag\n7:45 - 15:30 Uhr\n\nCafe Sonnendeck:\nGebäude H OG. 7\nMontag - Donnerstag\n7:30 - 15:45 Uhr\nFreitag\n7:30 - 13:45 Uhr\nDer Raum ist zugänglich und die Kaffeemaschine dienstbereit von 7:30 - 18:30 Uhr.");
		s.setSpan(new StyleSpan(Typeface.BOLD), 0, 27, 0);
		s.setSpan(new StyleSpan(Typeface.BOLD), 115 - 8, 129 - 8, 0);
		s.setSpan(new StyleSpan(Typeface.BOLD), 214 - 16, 231 - 16, 0);
		builder.setMessage(s);
		AlertDialog alert = builder.create();
		alert.show();
	}

	public boolean onCreateOptionsMenu(android.view.Menu menu) {

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
			refrashDatas(false);
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

	public void showZusatzstoffe() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Zusatzstoffe");
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setMessage("KENNZEICHNUNGSPFLICHTIGE ZUSATZSTOFFE:	\nS Schweinefleisch	\nVeg Vegetarisch	\n1 mit Farbstoff	\n2 mit Konservierungsstoff	  \n3 mit Antioxidationsmittel	  \n4 mit Geschmacksverstärker	  \n5 geschwefelt\n6 geschwärzt	\n7 gewachst	\n8 mit Phosphat	\n9 mit Säuerungsmittel	\n10 enthält eine Phenylalaninquelle	\n13 enthält Natriumnitrit	\n14 Bio-Kontrollnummer: DE-ÖKO-007")

		;
		AlertDialog alert = builder.create();
		alert.show();
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
		switch (day) {
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

}
