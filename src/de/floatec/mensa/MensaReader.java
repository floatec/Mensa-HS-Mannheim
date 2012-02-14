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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

/**
 * klasse um mensaplan auszulesen
 * 
 * @author kcirta
 * 
 */
public class MensaReader {
	public static final int MENSA_HS=0,MENSA_DHMM=2,MENSA_DHKT=3,MENSA_MHS=4,MENSA_UNI=1;
	public static final String[] MENSAS={"Hochschule Mannheim","Mensa am Schloss","DHBW Mensaria Metropol","DHBW Käfertaler Straße","Musikhochschule"};
	public static final int DAY_MO = 0;
	public static final int DAY_DI = 1;
	public static final int DAY_MI = 2;
	public static final int DAY_DO = 3;
	public static final int DAY_FR = 4;
	Calendar calendar = Calendar.getInstance();
	public MenuList ml;
	private int mensa=0;
	private int week;
	public boolean cache;
	private String buffer = "";
	private final static String[] URL= { "http://www.studentenwerk-mannheim.de/mensa/wo_hs.normal.php?+kw=", "http://www.studentenwerk-mannheim.de/mensa/wo_mas.normal.php?+kw=","http://www.studentenwerk-mannheim.de/mensa/wo_dh_mm.normal.php?+kw=","http://www.studentenwerk-mannheim.de/mensa/wo_mas.dhk.php?+kw=","http://www.studentenwerk-mannheim.de/mensa/wo_mas.mhs.php?+kw="};
	// String
	// UNI_URL="http://www.studentenwerk-mannheim.de/mensa/wo_mas.normal.php";

	public void setMensa(int mensa) {
		this.mensa = mensa;
		this.buffer="";//clears buffer
	}
	public String getMensaName() {
		return MENSAS[mensa];
	}
	public int getMensa() {
		return mensa;
	}
	
	public String getDate() {
		try {
			return buffer.substring(buffer.indexOf("<div class='WoDate_hs'>")
					+ "<div class='WoDate_hs'>".length(),
					buffer.indexOf("<div class='WoDate_hs'>")
							+ "<div class='WoDate_hs'>".length() + 19);
		} catch (Exception e) {
			return "Datum nicht verfügbar";
		}
	}

	/**
	 * setzt denn offset der auszulesende woche
	 */
	public void setWeek(int weekOffset) {

		week = weekOffset;//old
		calendar = Calendar.getInstance();
		calendar.add(Calendar.WEEK_OF_YEAR, weekOffset);
		if (calendar.get(Calendar.DAY_OF_WEEK) == 7
				|| calendar.get(Calendar.DAY_OF_WEEK) == 1) {
			calendar.add(calendar.WEEK_OF_YEAR, 1) ;
		} 	
		buffer = "";// to reset the buffer
	}

	/**
	 * 
	 * @return aktuelle kalender woche startend bei Mo und nicht wie in den USA
	 *         bei So!!!
	 */
	public int getKW() {

		
		return calendar.get(Calendar.WEEK_OF_YEAR) ;
	}

	/**
	 * nur implementiert wenn mehree wochen oder andere schulen hinzukommen
	 * 
	 * @return gibt die zu parsende url zurück
	 */
	private String urlBuilder() {
		return URL[mensa]+getKW();
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
			buffer = loadDatas(true);
	}

	/**
	 * erneutert den Buffer aber verzichtet auf den cached datas
	 */
	public void refrashlistWithoutCache() {
		buffer = loadDatas( false);
	}

	/**
	 * list tag aus
	 * 
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
		case DAY_MO:
			searchDay("Montag");
			break;
		case DAY_DI:
			searchDay("Dienstag");
			break;
		case DAY_MI:
			searchDay("Mittwoch");
			break;
		case DAY_DO:
			searchDay("Donnerstag<");
			break;
		case DAY_FR:
			searchDay("Freitag");
			break;
		default:
			ml.addmenu("ERROR", "Ungültiger Tag", "");
			break;
		}
		return ml;
	}

	
	/**
	 * sicht tag heraus und parsed ihn
	 * 
	 * @param day
	 *            zu parsendes element f.e. <h4>Montag</h4>
	 */
	private void searchDay(String day) {
		String dayBuffer;
		buffer.indexOf(day);

		// errro wenn leerer buffer 
		if (buffer.length() == 0) {
			ml.addmenu("ERROR", "Daten konnten nicht heruntergeladen werden.", "");
			return;
		}
		if(buffer.indexOf(day)==-1){
			ml.addmenu("ERROR", "Daten Nicht verfügbar", "");
			return;
		}
		dayBuffer = buffer.substring(buffer.indexOf(day));
		if(dayBuffer.indexOf("</tr>")==-1){
			ml.addmenu("ERROR", "Daten Nicht verfügbar", "");
			return;
		}
		dayBuffer = dayBuffer.substring(0, dayBuffer.indexOf("</tr>"));
		// splittet in daten teile
		String menus[] = dayBuffer
				.split("<td class='inh_1a oben rechts dickl'>|<td class='inh_1a oben rechts' nowrap='nowrap' width='50'>|</td><td class='inh_1a oben' width='125'>|<td class='inh_1a oben'>|<td width='' class='inh_1a oben rechts'>|<td class='inh_1a oben dickl' width='90'>|<td class='inh_1a oben links'>|<td class='inh_1a oben bo_re' nowrap='nowrap' width='33'>|<td class='inh_1a oben rechts' nowrap='nowrap' width='33'>|<td class='inh_1a oben' nowrap='nowrap' width='33'>|<td class='inh_1a oben rechts' nowrap='nowrap'>|<td width='' class='inh_1a oben rechts dickl'>|</h5>");
		// Log.i("Mensa", menus.length+dayBuffer);
		if (menus.length<5 ) {
			ml.addmenu("ERROR", "Daten Nicht verfügbar", "");
			return;
		}
		for (int i = 0; i < menus.length; i++) {
			Log.i("Mensa", html2plain(menus[i]));
			Log.i("Mensa", menus[i]);
		}
		for (int i = 1; i < menus.length-1; i=i+2) {
			
			Matcher matcher = Pattern.compile("[0-9]+,[0-9]{2} €").matcher(menus[i]+menus[i+1]);
			
			String price="";
			if(matcher.find())
			price=(menus[i]+menus[i+1]).substring(matcher.start(),matcher.end());
	        menus[i] = (menus[i]+menus[i+1]).replace(price, "");
	        menus[i+1] = price;
	        
		}
		// fügt menüs der liste hinzu
		if(mensa==MENSA_HS){
			ml.addmenu("Vegetarisch", html2plain(menus[1]), html2plain(menus[2]));
			ml.addmenu("Menü 1", html2plain(menus[3]), html2plain(menus[4]));
			ml.addmenu("Menü 2", html2plain(menus[5]), html2plain(menus[6]));
			ml.addmenu("Aktion", html2plain(menus[9]), html2plain(menus[10]));
			ml.addmenu("Wok", html2plain(menus[11]), html2plain(menus[12]));
			ml.addmenu("Dessert", html2plain(menus[7]), html2plain(menus[8]));
		}
if(mensa==MENSA_UNI){
			ml.addmenu("Vegetarisch", html2plain(menus[1]), html2plain(menus[2]));
			ml.addmenu("Menü 1", html2plain(menus[3]), html2plain(menus[4]));
			ml.addmenu("Menü 2", html2plain(menus[5]), html2plain(menus[6]));
			ml.addmenu("Grill", html2plain(menus[7]), html2plain(menus[8]));
			ml.addmenu("Pasta", html2plain(menus[9]), html2plain(menus[10]));
			ml.addmenu("Aktion", html2plain(menus[11]), html2plain(menus[12]));
			
		}
if(mensa==MENSA_DHMM){
	ml.addmenu("Vegetarisch", html2plain(menus[1]), html2plain(menus[2]));
	ml.addmenu("Menü 1", html2plain(menus[3]), html2plain(menus[4]));
	ml.addmenu("Menü 2", html2plain(menus[5]), html2plain(menus[6]));
}
if(mensa==MENSA_DHKT||mensa==MENSA_MHS){
	ml.addmenu("Vegetarisch", html2plain(menus[1]), html2plain(menus[2]));
	ml.addmenu("Menü 1", html2plain(menus[3]), html2plain(menus[4]));
	
}
	}
	
	public int getWeek() {
		return week;
	}

	/**
	 * entfernt html tags
	 * 
	 * @param html
	 *            code
	 * @return plain text
	 */
	private String html2plain(String html) {
		// return html;
		String s = "";
		html = html.replace("</h4>", "\n");
		html = html.replace("&nbsp;", "");
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
	 * läd daten in buffer
	 * 
	 * @param html
	 * @param useCache
	 * @return
	 */
	private String loadDatas( boolean useCache) {
		String html=urlBuilder();
		StringBuilder filebufer = new StringBuilder();
		// cache
		File file = new File("/data/data/de.floatec.mensa/"+mensa+"KW" + getKW());
		/* if cache is avalible and it should be used */
		if (file.exists() && useCache) {
			try {
				/*read cache*/
				BufferedReader br = new BufferedReader(new FileReader(
						"/data/data/de.floatec.mensa/"+mensa+"KW" + getKW()));
				String line = br.readLine();
				while (line != null) {
					filebufer.append(line);
					line = br.readLine();
				}

				br.close();
				cache = true;
				return filebufer.toString();//return cache

			} catch (Exception e) {
			}
		}
		String s = "";
		try {
			/*load datas*/
			cache = false;
			URL url = new URL(html);
			URLConnection ucon = url.openConnection();
			InputStream is = ucon.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String current = "";
			while ((current = br.readLine()) != null) {

				s = s + current;
			}

			try {
				/* safe to cache */

				FileWriter testwriter = new FileWriter(
						"/data/data/de.floatec.mensa/"+mensa+"KW" + getKW(), false);
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
		}catch (Exception e) {
			// TODO: handle exception
		}
		return s;//return downloaded datas

	}

}
