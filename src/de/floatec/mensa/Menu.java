package de.floatec.mensa;
/**
 * klasse für ein einzelnes menü
 * @author kcirta
 *
 */
public class Menu {
	private String price="";
	private String title="";
	private String text="";
	
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
		this.text = text;
	}
	public Menu(String title) {
		// TODO Auto-generated constructor stub
		this.title=title;
	}
}
