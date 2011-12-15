package de.floatec.mensa;
import com.google.ads.m;

import android.R.anim;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;

public class RatingOverviewActivity extends ListActivity{
	private Menu menu;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		 super.onCreate(savedInstanceState);
	   Intent intent = getIntent();
	   menu=(Menu)intent.getExtras().get( "menu" );
		 setContentView(android.R.layout.simple_expandable_list_item_2);
	     // der List-Adapter
	   
	}

}
