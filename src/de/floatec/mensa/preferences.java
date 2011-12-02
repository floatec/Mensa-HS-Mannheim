package de.floatec.mensa;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class preferences extends PreferenceActivity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);


		// Get the custom preference
		
		 Preference customPref = (Preference) findPreference("add");
		 customPref.setOnPreferenceClickListener(new
		 OnPreferenceClickListener() {
		 
		  public boolean onPreferenceClick(Preference preference) {

			  if(preference.getKey()=="add"){
				  showDialog(0);
			  }
			  return true; } });
		 
		
	}
	
	protected Dialog onCreateDialog(int id) {

	    AlertDialog.Builder b = new AlertDialog.Builder(this);
	    b.setTitle("Hinweis")
	     .setMessage("Bevor Sie die Werbung deaktivieren denken Sie daran das SIch diese App durch die Werbung finanziert!")
	     .setPositiveButton("OK", new DialogInterface.OnClickListener() {

	        
	        public void onClick(DialogInterface dialog, int which) {

	            dialog.cancel();

	        }

	     });

	    return b.create();

	}
	
}
