package eu.trentorise.smartcampus.launcher.models;

import android.graphics.drawable.Drawable;

/**
 * How a SmartApp has to be represented (extended in future)
 * 
 * @author s.casagranda
 * 
 */
public class SmartApp {

	public String appPackage;
	public String name;
	public String url;
	public Drawable icon;
	public Drawable gray_icon;
	public String background;
	public int version;
	public String filename;

	public void fillApp(String name, String pack, String url, Drawable icon, Drawable gray, String background, int versions, String filename) {
		this.name = name;
		this.appPackage = pack;
		this.url = url;
		this.icon = icon;
		this.gray_icon = gray;
		this.background = background;
		this.version = versions;
		this.filename = filename;
	}

}
