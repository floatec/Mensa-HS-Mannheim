package de.floatec.mensa;

import java.util.ArrayList;
/**
 *  listen element ffr menus
 * @author kcirta
 *
 */
public class MenuList {
	private ArrayList<Menu> menus =new ArrayList<Menu>();
	
	public void addmenu(String title,String text,String price) {
		Menu tempMenu =new Menu(title);
		tempMenu.setText(text);
		tempMenu.setPrice(price);
		menus.add(tempMenu);
	}
	
	public void clear() {
		menus.clear();
	}
	public Menu getMenu(int index) {
		return menus.get(index);
	}
	public int getMenuCount(){
		return menus.size();
	}
}
