package de.floatec.mensa;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.BufferType;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class MensaActivity extends Activity {
	private static final int MENU_EXIT = 0, MENU_ZUSATZSTOFFE = 1,
			MENU_EINSTELLUNGEN = 2, MENU_FEEDBACK_MENSA = 3,
			MENU_FEEDBACK_APP = 4, MENU_REFRASH = 5, MENU_DONATE = 6,
			MENU_UEBER = 7, MENU_OEFFNUNGSZEITEN = 8, MENU_WHAT = 9,MENU_LAGEPLAN=10;
	Calendar Mo = Calendar.getInstance();
	private float downXValue;
	private MensaReader mr;
	private ViewFlipper vf;

	private int activeDay = 0, week = 0;
	private Calendar calendar = Calendar.getInstance();
	private SharedPreferences prefs;

	private static final SimpleDateFormat FORMAT = new SimpleDateFormat(
			"dd.MMMM.yyyy");

	private OnTouchListener ot = new OnTouchListener() {

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
					if (vf.getDisplayedChild() > 0) {
						--activeDay;
						vf.setAnimation(AnimationUtils.loadAnimation(
								getBaseContext(), android.R.anim.fade_out));
						vf.showPrevious();
					} else {
						if (mr.getWeek() > 0) {
							flipTo(4);

							setWeek(mr.getWeek() - 1);

						} else {
							Toast.makeText(
									getBaseContext(),
									"Daten aus der Vergangenheit können nicht Abgerufen Werden.",
									Toast.LENGTH_SHORT).show();
						}
					}
				}

				// going forwards: pushing stuff to the left
				if (downXValue - 30 > currentX) {
					if (vf.getDisplayedChild() < 4) {
						++activeDay;
						vf.setAnimation(AnimationUtils.loadAnimation(
								getBaseContext(), android.R.anim.fade_out));
						vf.showNext();
					} else {
						if (mr.getWeek() < 2
								|| (calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
										&& calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY && mr
										.getWeek() < 3)) {

							setWeek(mr.getWeek() + 1);
							flipTo(0);
						} else {
							Toast.makeText(getBaseContext(),
									"Daten sind noch nicht verfügbar.",
									Toast.LENGTH_SHORT).show();

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

	private ProgressDialog pd;

	private void flipTo(int day) {
		int way;
		way = vf.getDisplayedChild() < day ? -1 : 1;
		while (day != vf.getDisplayedChild()) {
			if (way == 1) {

				vf.setAnimation(AnimationUtils.loadAnimation(this,
						android.R.anim.fade_out));
				vf.showNext();
			} else {
				vf.setAnimation(AnimationUtils.loadAnimation(this,
						android.R.anim.fade_out));
				vf.showPrevious();
			}
		}
	}

	/**
	 * ändert die woche
	 * 
	 * @param week
	 */
	public void setWeek(int week) {
		this.week = week;
		mr.setWeek(week);
		refrashDatas();
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
		flipTo(weekday);
	}

	private void showProgressDIalog() {
		pd.show();
	}

	private class RefrashDatas extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... urls) {
			// showProgressDIalog();
			if (urls[0].equals("CACHE") && prefs.getBoolean("cache", true)) {
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

		@Override
		protected void onCancelled() {
			super.onCancelled();
			pd.dismiss();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		showProgressDIalog();
		refrashDatas();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// init Weekday
		if (Mo.get(Mo.DAY_OF_WEEK) != Calendar.SUNDAY
				&& Mo.get(Mo.DAY_OF_WEEK) != Calendar.SATURDAY)
			Mo.add(Mo.DAY_OF_MONTH, -Mo.get(Mo.DAY_OF_WEEK) + 2);
		else
			Mo.add(Mo.DAY_OF_MONTH, -Mo.get(Mo.DAY_OF_WEEK) + 2 + 7);
		// activity init
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		// init progrss dialog
		pd = ProgressDialog.show(this, "Laden...",
				"Daten werden Heruntergeladen", true, true);
		// init admob
		AdView adView;
		adView = new AdView(this, AdSize.BANNER,
				"/14148428/ca-pub-3723428902598385/Phone_Android_Mannheim");
		((LinearLayout) findViewById(R.id.ad)).addView(adView);
		adView.loadAd(new AdRequest());

		// init view
		vf = (ViewFlipper) findViewById(R.id.viewFlipper1);
		LinearLayout layMain = (LinearLayout) findViewById(R.id.mov);
		layMain.setOnTouchListener(ot);

		layMain = (LinearLayout) findViewById(R.id.div);
		layMain.setOnTouchListener(ot);

		layMain = (LinearLayout) findViewById(R.id.miv);
		layMain.setOnTouchListener(ot);

		layMain = (LinearLayout) findViewById(R.id.dov);
		layMain.setOnTouchListener(ot);

		layMain = (LinearLayout) findViewById(R.id.frv);
		layMain.setOnTouchListener(ot);

		// init mensareader
		mr = new MensaReader();
		// wenn keine Mensa Eingestellt ist
		if (-1 == Integer.parseInt(prefs.getString("mensa", "-1"))) {
			Intent intent_menu_settings = new Intent(this, Preferences.class);
			startActivityForResult(intent_menu_settings, 1);
		}
		setMensa(Integer.parseInt(prefs.getString("mensa", "0")));
		// Setze auf Aktuellen Tag
		initFlip();
	}

	void refrashDatas() {
		refrashDatas(true);
	}

	void refrashDatas(boolean useCache) {
		setMensa(Integer.parseInt(prefs.getString("mensa", "0")));
		showProgressDIalog();
		RefrashDatas task = new RefrashDatas();
		if (useCache)

			task.execute(new String[] { "CACHE" });
		else
			task.execute(new String[] { "NOCACHE" });

	}

	private void loadUI() {
		Calendar calDay = Calendar.getInstance();

		for (int day = 0; day < 5; day++) {
			boolean refrashButton = false;
			MenuList ml = mr.readDay(day);
			TextView tw = new TextView(this);
			LinearLayout contentLayout = (LinearLayout) ((ScrollView) ((LinearLayout) vf
					.getChildAt(day)).getChildAt(1)).getChildAt(0);
			// Setzt datum
			calDay = (Calendar) Mo.clone();
			calDay.add(Mo.DAY_OF_MONTH, day + 7 * mr.getWeek());
			((TextView) ((LinearLayout) ((LinearLayout) vf.getChildAt(day))
					.getChildAt(0)).getChildAt(1)).setText(FORMAT.format(calDay
					.getTime()));

			// leert view
			contentLayout.removeAllViews();
			// gibt alle menüs aus
			for (int i = 0; i < ml.getMenuCount(); i++) {
				LinearLayout ll = new LinearLayout(getApplicationContext());
				ll.setOrientation(LinearLayout.HORIZONTAL);
				ll.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.bg_header));
				tw = new TextView(this);
				tw.setText(ml.getMenu(i).getTitle());
				tw.setTextSize(14);
				tw.setTypeface(Typeface.DEFAULT_BOLD);
				tw.setTextColor(Color.WHITE);
				// Fehlerfallüberprüfung
				if (ml.getMenu(i).getTitle().compareTo("ERROR") != 0) {
					tw.setTextColor(Color.WHITE);
					// tw.setBackgroundColor(Color.rgb(255, 127, 36));

					tw.setPadding(5, 1, 5, 1);
					ll.addView(tw);
					tw = new TextView(this);
					tw.setLayoutParams(new LayoutParams(
							LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
					tw.setText(ml.getMenu(i).getPrice());
					tw.setTextSize(14);
					tw.setTypeface(Typeface.DEFAULT_BOLD);
					tw.setTextColor(Color.WHITE);
					tw.setGravity(Gravity.RIGHT);
					tw.setPadding(5, 1, 5, 1);
					ll.addView(tw);
				} else {
					refrashButton = true;
					tw.setTextColor(Color.WHITE);
					tw.setBackgroundColor(Color.RED);
					tw.setPadding(5, 1, 5, 1);
					tw.setBackgroundDrawable(getResources().getDrawable(
							R.drawable.bg_header_error));
					ll.addView(tw);
				}

				contentLayout.addView(ll);
				tw = new TextView(this);
				tw.setBackgroundColor(Color.LTGRAY);

				tw.setTextSize(16);
				tw.setText(ml.getMenu(i).getText());
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
			ImageButton ib = new ImageButton(getBaseContext());
			ib.setBackgroundResource(R.drawable.btn_not_selected);
			ib.setImageResource(android.R.drawable.ic_menu_rotate);
			ib.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					refrashDatas();

				}
			});
			if (refrashButton) {
				contentLayout.addView(ib);
			}
			try {
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
				// adView.loadAd(new AdRequest());
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}

	public void showOeffnungszeiten() {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Öffnungszeiten");
		builder.setIcon(android.R.drawable.ic_menu_recent_history);
		SpannableString s = new SpannableString("Nicht verfügbar");

		switch (mr.getMensa()) {
		case MensaReader.MENSA_HS:
			s = new SpannableString(
					"Mensa Hochschule Mannheim:\nGebäude J OG.1\nMontag - Donnerstag\n11:15 - 14:00 Uhr\nFreitag\n11:15 - 13:45 Uhr\n\nCafé Integral:\nGebäude J EG.\nMontag - Donnerstag\n7:45 - 16:00 Uhr\nFreitag\n7:45 - 15:30 Uhr\n\nCafe Sonnendeck:\nGebäude H OG. 7\nMontag - Donnerstag\n7:30 - 15:45 Uhr\nFreitag\n7:30 - 13:45 Uhr\nDer Raum ist zugänglich und die Kaffeemaschine dienstbereit von 7:30 - 18:30 Uhr.");
			s.setSpan(new StyleSpan(Typeface.BOLD), 0, 27, 0);
			s.setSpan(new StyleSpan(Typeface.BOLD), 115 - 8, 129 - 8, 0);
			s.setSpan(new StyleSpan(Typeface.BOLD), 214 - 16, 231 - 16, 0);
			break;
		case MensaReader.MENSA_UNI:
			s = new SpannableString(
					"Mensa am Schloss\n\nÖffnungszeiten:\nMontag - Donnerstag\n10:00 - 16:00 Uhr\nFreitag\n10:00 - 15:00 Uhr\n\nMittagstisch:\nMontag - Donnerstag\n11:30 - 14:15 Uhr\nFreitag\n11:30 - 14:00 Uhr");
			s.setSpan(new StyleSpan(Typeface.BOLD), 0, 16, 0);
			break;
		case MensaReader.MENSA_DHKT:
			s = new SpannableString(
					"Mensaria Wohlgelegen Duale Hochschule Käfertaler Straße\n\nÖffnungszeiten:\nMontag - Donnerstag\n8:30 - 15:30 Uhr\nFreitag\n8:30 - 15:15 Uhr\n\nMittagsmenüs\nMontag -  Freitag\n12:00 - 13:30 Uhr");
			s.setSpan(new StyleSpan(Typeface.BOLD), 0, 55, 0);
			break;
		case MensaReader.MENSA_DHMM:
			s = new SpannableString(
					"Mensaria DH Hans-Thoma-Straße\n\nÖffnungszeiten:\nMontag - Donnerstag\n8:00 - 16:00 Uhr\nFreitag\n8:00 - 15:00 Uhr");
			s.setSpan(new StyleSpan(Typeface.BOLD), 0, 30, 0);
			break;
		case MensaReader.MENSA_MHS:
			s = new SpannableString(
					"Cafeteria Musikhochschule\n\nÖffnungszeiten:\nMontag - Freitag\n9:00 - 15:30 Uhr\n\nMittagstisch:\nMontag - Freitag\n11:30 - 13:30 Uhr\n\nDie benachbarte Automatenstation steht täglich von 7:00 bis 22:00 Uhr zur Verfügung.");
			s.setSpan(new StyleSpan(Typeface.BOLD), 0, 25, 0);
			break;
		default:
			break;
		}

		builder.setMessage(s);
		AlertDialog alert = builder.create();
		alert.show();
	}

	public boolean onCreateOptionsMenu(android.view.Menu menu) {

		menu.add(0, MENU_ZUSATZSTOFFE, 0, "Zusatzstoffe").setIcon(
				android.R.drawable.ic_menu_help);

		menu.add(0, MENU_REFRASH, 0, "Aktuallisieren").setIcon(
				android.R.drawable.ic_menu_rotate);

		menu.add(0, MENU_EINSTELLUNGEN, 0, "Einstellungen").setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_OEFFNUNGSZEITEN, 0, "Öffnungszeiten").setIcon(
				android.R.drawable.ic_menu_recent_history);
		if (prefs.getBoolean("whatshouldieat", false))
			menu.add(0, MENU_WHAT, 0, "What should I eat?").setIcon(
					android.R.drawable.ic_menu_help);
		menu.add(0, MENU_UEBER, 0, "Über").setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(0, MENU_FEEDBACK_APP, 0, "Feedback zur App").setIcon(
				android.R.drawable.ic_menu_send);
		menu.add(0, MENU_FEEDBACK_MENSA, 0, "Feedback an die Mensa").setIcon(
				android.R.drawable.ic_menu_send);
		menu.add(0, MENU_DONATE, 0, "Spenden").setIcon(
				android.R.drawable.ic_menu_view);
		//menu.add(0, MENU_LAGEPLAN, 0, "Lageplan").setIcon(		android.R.drawable.ic_menu_directions);
		menu.add(0, MENU_EXIT, 0, "Exit").setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);

		return true;

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent browser;
		switch (item.getItemId()) {
		case MENU_FEEDBACK_APP:
			// email an entwickler
			browser = new Intent(Intent.ACTION_VIEW,
					Uri.parse("mailto:android@floatec.de?subject=Android app:"
							+ getString(R.string.app_name) + " V."
							+ getString(R.string.app_version) + ""));
			startActivity(browser);
			return true;
		case MENU_FEEDBACK_MENSA:
			// öffnet ie mensa feedback seite vom studenten werk
			browser = new Intent(
					Intent.ACTION_VIEW,
					Uri.parse("http://www.studentenwerk-mannheim.de/egotec/Essen+_+Trinken/Ihr+Feedback-p-32.html"));
			startActivity(browser);
			return true;
		case MENU_REFRASH:
			refrashDatas(false);
			return true;
		case MENU_OEFFNUNGSZEITEN:
			showOeffnungszeiten();
			return true;
		case MENU_LAGEPLAN:
			showLageplan();
			return true;
		case MENU_WHAT:
			showWhatShouldIEat();
			return true;
		case MENU_UEBER:
			Intent intent_menu_ueber = new Intent(this,
					UeberSeiteAnzeigen.class);
			startActivity(intent_menu_ueber);
			return true;
		case MENU_EINSTELLUNGEN:
			Intent intent_menu_settings = new Intent(this, Preferences.class);
			startActivityForResult(intent_menu_settings, 1);
			return true;
		case MENU_ZUSATZSTOFFE:
			showZusatzstoffe();
			return true;
		case MENU_DONATE:
			Intent browser2 = new Intent(Intent.ACTION_VIEW,
					Uri.parse("http://floatec.de/donate.html"));
			startActivity(browser2);
			return true;
		case MENU_EXIT:
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

	public void showLageplan() {
		// http://maps.google.com/maps/api/staticmap?center=Mannheim%20luisenpark&zoom=13&size=512x512&markers=color:blue%7Clabel:A%7CHochschule%20Mannheim&markers=color:green%7Clabel:B%7CMannheim%20Schloss&markers=color:red%7Ccolor:red%7Clabel:C%7CHans-Thoma-Str.%2040%2068163%20Mannheim&markers=color:red%7Ccolor:red%7Clabel:D%7CK%C3%A4fertaler%20Stra%C3%9Fe%20258%20Mannheim&markers=color:red%7Ccolor:yellow%7Clabel:E%7CN7%20Mannheim&sensor=false
		String s = "";
		for (int i = 0; i < MensaReader.MENSAS.length; i++) {
			s += ((char) (i + 'A')) + " " + MensaReader.MENSAS[i] + "\n";
		}
	
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);

		View layout = inflater.inflate(R.layout.lageplan,
				(ViewGroup) findViewById(R.id.root));
		ImageView   image=(ImageView)findViewById(R.id.logo);
		Drawable drawable = LoadImageFromWebOperations("http://maps.google.com/maps/api/staticmap?center=Mannheim%20luisenpark&zoom=13&size=512x512&markers=color:blue%7Clabel:A%7CHochschule%20Mannheim&markers=color:green%7Clabel:B%7CMannheim%20Schloss&markers=color:red%7Ccolor:red%7Clabel:C%7CHans-Thoma-Str.%2040%2068163%20Mannheim&markers=color:red%7Ccolor:red%7Clabel:D%7CK%C3%A4fertaler%20Stra%C3%9Fe%20258%20Mannheim&markers=color:red%7Ccolor:yellow%7Clabel:E%7CN7%20Mannheim&sensor=false");
		image.setImageDrawable(drawable);
		//TextView   tv=(TextView)findViewById(R.id.text);
		//tv.setText(s);
		AlertDialog.Builder adb = new AlertDialog.Builder(this);

		adb.setView(layout);

		adb.show();

	}
	private Drawable LoadImageFromWebOperations(String url)
	{
	try
	{
	InputStream is = (InputStream) new URL(url).getContent();
	Drawable d = Drawable.createFromStream(is, "src name");
	return d;
	}catch (Exception e) {
	System.out.println("Exc="+e);
	return null;
	}
	 }
	public void showWhatShouldIEat() {
		MenuList list = mr.readDay(vf.getDisplayedChild());
		int menu = new Random().nextInt(list.getMenuCount());
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("What should I eat?");
		builder.setIcon(android.R.drawable.ic_menu_info_details);
		builder.setMessage(list.getMenu(menu).getText() + "\n"
				+ list.getMenu(menu).getPrice());
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void setMensa(int Mensa) {
		mr.setMensa(Mensa);
		((TextView) findViewById(R.id.mensa))
				.setText(MensaReader.MENSAS[Mensa]);
	}

}
