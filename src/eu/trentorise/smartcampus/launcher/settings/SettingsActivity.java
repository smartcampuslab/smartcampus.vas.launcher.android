package eu.trentorise.smartcampus.launcher.settings;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import eu.trentorise.smartcampus.launcher.R;


public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	private static final String KEY_UPDATE_DEV = "update_dev";
	private static final String KEY_UPDATE_REFRESH = "refresh";	
	public static final String PREFS_NAME = "LauncherPreferences";
   // private ListPreference mListPreference;

	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      //  mListPreference = (ListPreference) getPreferenceScreen().findPreference(KEY_UPDATE_DEV);

		if (prefs == null) return;

    }

	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs == null) return;
		if (KEY_UPDATE_DEV.equals(key)) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean(KEY_UPDATE_DEV, sharedPreferences.getBoolean(KEY_UPDATE_DEV, false)).commit();
/*			if (mListPreference.getValue().compareTo("1")==0)
				editor.putBoolean(KEY_UPDATE_DEV,false).commit();
			if (mListPreference.getValue().compareTo("2")==0)
				editor.putBoolean(KEY_UPDATE_DEV,true).commit();*/
			editor.putBoolean(KEY_UPDATE_REFRESH,true).commit();

		}

		

	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}
	
	 @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
}
