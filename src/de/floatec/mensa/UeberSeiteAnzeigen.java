package de.floatec.mensa;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class UeberSeiteAnzeigen extends Activity{
	/** Called when the activity is first created. */
	
	private TextView tvHP;
    @Override
    public void onCreate(Bundle iCicle) {
        super.onCreate(iCicle);
        setContentView(R.layout.ueber);
        tvHP=(TextView)findViewById(R.id.hp);
       
    }
    public void callHP(View target){
   	 Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.floatec.de"));
   		 startActivity(browser);
    }
    public void callMail(View target){
      	 Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:android@floatec.de?subject=Android app:"+getString(R.string.app_name)+" V."+getString(R.string.app_version)));
      		 startActivity(browser);
       }
}
