package de.floatec.mensa;

import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import android.util.Log;
/**
 * klasse um mensaplan auszulesen
 * @author kcirta
 *
 */
public class MensaReader {

	Calendar calendar = Calendar.getInstance();
	public MenuList ml;
	private int week;
	public boolean cache;
	private String buffer = "";
	private final static String HS_URL = "http://www.studentenwerk-mannheim.de/mensa/wo_hs.normal.php";// "http://www.studentenwerk-mannheim.de/mensa/wo_mas.normal.php?+kw=43";
	// String
	// UNI_URL="http://www.studentenwerk-mannheim.de/mensa/wo_mas.normal.php";
	private final static int MENU_COUNT = 6;

	
	/**
	 * @deprecated wird noch nicht verwendet
	 */
	public void switchWeek(int weekOffset) {
		int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
		if (weekOffset < 0) {
			weekOffset = 0;
		}
		if (weekOffset > 3) {
			weekOffset = 3;
		}
		week = weekOfYear + weekOffset;
	}
	
	/**
	 * 
	 * @return aktuelle kalender woche startend bei Mo und nicht wie in den USA bei So!!!
	 */
	public int KW(){
	
		if( calendar.get(Calendar.DAY_OF_WEEK)==1){
			return  calendar.get(Calendar.WEEK_OF_YEAR-1);
		}else{
			return  calendar.get(Calendar.WEEK_OF_YEAR);
		}
	}

	/**
	 * nur implementiert wenn mehree wochen oder andere schulen hinzukommen
	 * @return gibt die zu parsende url zurück
	 */
	private String urlBuilder() {
		return HS_URL;// +week;
	}
/**
 * inti
 */
	public MensaReader() {
		// refrashlist();
		ml = new MenuList();
	}
	/**
	 * erneuert denn buffer
	 */
	public void refrashlist() {
		if (buffer.length() == 0)
			buffer = DownloadFromUrl(urlBuilder(),true);
	}
	/**
	 * erneutert den Buffer aber verzichtet auf den cached datas
	 */
	public void refrashlistWithoutCache() {
			buffer = DownloadFromUrl(urlBuilder(),false);
	}

	/**
	 * list tag aus
	 * @param day
	 *            MO=0 Di=1 Mi=2 Do=3 Fr=4
	 */
	public MenuList readDay(int day) {
		ml.clear();
		if (buffer.length() == 0) {
			ml.addmenu("ERROR", "Daten Nicht verfügbar", "");
			return ml;
		}

		switch (day) {
		case 0:
			searchDay("<h1>Montag</h1>");
			break;
		case 1:
			searchDay("<h1>Dienstag</h1>");
			break;
		case 2:
			searchDay("<h1>Mittwoch</h1>");
			break;
		case 3:
			searchDay("<h1>Donnerstag</h1>");
			break;
		case 4:
			searchDay("<h1>Freitag</h1>");
			break;
		default:
			ml.addmenu("ERROR", "Ungültiger Tag", "");
			break;
		}
		return ml;
	}

	/*
	 * <h1>Montag</h1></td><td class='inh_1a oben' width='125'><h4>Erbsensuppe
	 * mit Genüsewürfeln</h4>3 Käseknödel auf Blattspinat mit gem. Salat</td><td
	 * class='inh_1a oben rechts' nowrap='nowrap' width='33'>2,50 €</td><td
	 * class='inh_1a oben' width='125'><h4>Erbsensuppe mit
	 * Gemüsewürfeln</h4>Schwäb. Maultaschen (S) mit Zwiebelsauce,
	 * Kartoffelsalat mit Petersilie und Blattsalat</td><td class='inh_1a oben
	 * rechts' nowrap='nowrap' width='33'>2,50 €</td><td class='inh_1a oben'
	 * width='125'><h4>Erbsensuppe mit Gemüsewürfeln</h4>4 Chicken Wings in
	 * feuriger Chilisauce mit Crazy frites und Krautsalat</td><td class='inh_1a
	 * oben rechts' nowrap='nowrap' width='33'>3,10 €</td><td class='inh_1a oben
	 * dickl' width='90'>Aprikosencreme</td><td class='inh_1a oben'
	 * nowrap='nowrap' width='33'>Portion<br/>0,50 €</td><td class='inh_1a oben
	 * links'>Pan. Schweineschnitzel mit Pommes frites</td><td class='inh_1a
	 * oben' nowrap='nowrap' width='33'>3,10 €</td><td class='inh_1a oben
	 * links'>Bayr. Krustenbraten (S) in Weißbiersauce mit Kartoffelknödel und
	 * Krautsalat</td><td class='inh_1a oben bo_re' nowrap='nowrap'
	 * width='33'>3,10 €'>
	 */
	/**
	 * sicht tag heraus und parsed ihn
	 * @param day zu parsendes element f.e. <h4>Montag</h4>
	 */
	private void searchDay(String day) {
		String dayBuffer;
		buffer.indexOf(day);
		
		//errro wenn leerer buffer
		if (buffer.indexOf(day) == -1) {
			ml.addmenu("ERROR", "Daten Nicht verfügbar", "");
			return;
		}
		dayBuffer = buffer.substring(buffer.indexOf(day));

		dayBuffer = dayBuffer.substring(0, dayBuffer.indexOf("</tr>"));
		//splittet in daten teile
		String menus[] = dayBuffer
				.split("</td><td class='inh_1a oben' width='125'>|<td class='inh_1a oben dickl' width='90'>|<td class='inh_1a oben links'>|<td class='inh_1a oben bo_re' nowrap='nowrap' width='33'>|<td class='inh_1a oben rechts' nowrap='nowrap' width='33'>|<td class='inh_1a oben' nowrap='nowrap' width='33'>");
		// Log.i("Mensa", menus.length+dayBuffer);
		if (menus.length != 13) {
			ml.addmenu("ERROR", "Daten Nicht verfügbar", "");
			return;
		}
		for (int i = 0; i < menus.length; i++) {
			Log.i("Mensa", html2plain(menus[i]));
			Log.i("Mensa", menus[i]);
		}
		//fügt menüs der lsite hinzu
		ml.addmenu("Vegetarisch",html2plain(menus[1]), html2plain(menus[2]));
		ml.addmenu("Menü 1",html2plain(menus[3]), html2plain(menus[4]));
		ml.addmenu("Menü 2",html2plain(menus[5]), html2plain(menus[6]));
		ml.addmenu("Dessert",html2plain(menus[7]), html2plain(menus[8]));
		ml.addmenu("Aktion",html2plain(menus[9]), html2plain(menus[10]));
		ml.addmenu("Wok",html2plain(menus[11]), html2plain(menus[12]));
		

	}
	/**
	 * netfernt html tags
	 * @param html code
	 * @return plain text
	 */
	private String html2plain(String html) {
		// return html;
		String s = "";
		html = html.replace("</h4>", "\n");
		html = html.replace("<br/>", "\n");
		boolean inTag = false;
		for (int i = 0; i < html.length(); i++) {
			if (html.substring(i, i + 1).compareTo("<") == 0 || inTag) {
				inTag = true;
			} else {
				s = s + html.substring(i, i + 1);
			}
			if (html.substring(i, i + 1).compareTo(">") == 0) {
				inTag = false;
			}
		}
		return s;
	}
/**
 *  läd daten herunter
 * @param html
 * @param useCache 
 * @return
 */
	private String DownloadFromUrl(String html, boolean useCache) { 
		StringBuilder filebufer=new StringBuilder();		
		// cache
		File file = new File("/data/data/de.floatec.mensa/KW"
				+KW());
		/*if ceche is avalible and it should be used*/
        if(file.exists()&&useCache) {
			try {

				BufferedReader br = new BufferedReader(new FileReader(
						"/data/data/de.floatec.mensa/KW"
								+ KW()));
				
				String line = br.readLine();
				double avg = 0.0;
				int num = 0, count = 0;
				
				while (line != null) {
					filebufer.append(line);
					line = br.readLine();
				}

				br.close();
				cache=true;
				return filebufer.toString();

			} catch (Exception e) {
			}
		}
		String s = "";
		try {
			cache=false;
			URL url = new URL(html); // you can write here any link

			long startTime = System.currentTimeMillis();

			/* Open a connection to that URL. */
			URLConnection ucon = url.openConnection();

			InputStream is = ucon.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			String current = "";
			while ((current = br.readLine()) != null) {
				
				s = s + current;
			}

			try {
				/* safe to ceche*/
				
				FileWriter testwriter = new FileWriter(
						"/data/data/de.floatec.mensa/KW"
								+KW(), false);
				BufferedWriter out = new BufferedWriter(testwriter);
				out.write(s);
				out.flush();
				out.close();
				testwriter.close();

			} catch (IOException e) {
				Log.d("Mensa", "Cashe Error: " + e);
			}

		} catch (IOException e) {
			Log.d("Mensa", "Error: " + e);
		}
		return s;

	}

}
