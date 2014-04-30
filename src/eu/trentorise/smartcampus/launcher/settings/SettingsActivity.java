package eu.trentorise.smartcampus.launcher.settings;

import it.smartcampuslab.launcher.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;


public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
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
