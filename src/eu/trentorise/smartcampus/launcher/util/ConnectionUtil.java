package eu.trentorise.smartcampus.launcher.util;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;

/**
 * Utility class that allows you to receive about phone connection
 * 
 * @author Simone Casagranda
 *
 */
public class ConnectionUtil {

	private ConnectionUtil(){
		throw new AssertionError("You must use static methods!");
	}
	
	public static Intent getWifiSettingsIntent(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setAction(Settings.ACTION_WIFI_SETTINGS);
		return intent;
	}
	
	/**
	 * Retrieves the connectivity manager
	 * @param context
	 * @return
	 */
	public static ConnectivityManager getConnectivityManager(Context context){
		return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	
	/**
	 * Check if we are connected to a WiFi network
	 * @param cm
	 * @return
	 */
	public static boolean isConnectedToWiFi(ConnectivityManager cm) {
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.getType() == ConnectivityManager.TYPE_WIFI ;
	}
	
	/**
	 * Check if we are connected to a 3G network
	 * @param cm
	 * @return
	 */
	public static boolean isConnectedTo3G(ConnectivityManager cm) {
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.getType() == ConnectivityManager.TYPE_MOBILE ;
	}
	
	/**
	 * Check if we are connected to a network
	 * @param cm
	 * @return
	 */
	public static boolean isConnected(ConnectivityManager cm){
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isConnected();
	}
	
	/**
	 * Check if we are connecting or connected to a WiFi network
	 * @param cm
	 * @return
	 */
	public static boolean isConnectingOrConnectedToWiFi(ConnectivityManager cm){
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.getType() == ConnectivityManager.TYPE_WIFI && info.isConnectedOrConnecting();
	}
	
	/**
	 * Check if we are connecting or connected to a 3G network
	 * @param cm
	 * @return
	 */
	public static boolean isConnectingOrConnectedTo3G(ConnectivityManager cm){
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.getType() == ConnectivityManager.TYPE_MOBILE && info.isConnectedOrConnecting();
	}

	/**
	 * Check if we are connecting or connected to a network
	 * @param cm
	 * @return
	 */
	public static boolean isConnectingOrConnected(ConnectivityManager cm){
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isConnectedOrConnecting();
	}
	
	/**
	 * Check if we are connected in roaming
	 * @param cm
	 * @return
	 */
	public static boolean isRoaming(ConnectivityManager cm) {
		NetworkInfo info = cm.getActiveNetworkInfo();
		return info != null && info.isRoaming();
	}
}
