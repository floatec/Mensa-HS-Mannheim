package de.floatec.mensa;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.Toast;

public class Preferences extends PreferenceActivity  {

	@Override
	 protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		
		setResult(Activity.RESULT_OK);
		// Get the custom preference

		Preference customPref = (Preference) findPreference("mensa");

		if(-1==Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("mensa", "-1")))
			Toast.makeText(this, "Bitte Wähle eine Mensa aus!", Toast.LENGTH_LONG).show();

	
    
        
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
