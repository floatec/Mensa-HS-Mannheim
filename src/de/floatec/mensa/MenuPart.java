package de.floatec.mensa;

import java.util.ArrayList;

public class MenuPart {
	String name;
	ArrayList<MenuRating> ratings;
	public MenuPart(String name) {
		setName(name);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void addRating(MenuRating mr) {
		ratings.add(mr);
	}
	public int getRatingCount() {
		return ratings.size();
	}
	public MenuRating getRating(int i) {
		return ratings.get(i);
	}
}
