package de.floatec.mensa;

import java.util.ArrayList;

/**
 * klasse fur ein einzelnes menu
 * @author kcirta
 *
 */
public class Menu {
	private String price="";
	private String title="";
	private String text="";
	private ArrayList<MenuPart> mp=new ArrayList<MenuPart>();
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		mp.clear();
		String[]parts=text.split(" ");
		for (int i = 0; i < parts.length; i++) {
		 	MenuPart  part=new MenuPart(parts[i]);
			mp.add(part);
		}
		
		this.text = text;
	}
	public Menu(String title) {
		// TODO Auto-generated constructor stub
		this.title=title;
	}
}
