/*******************************************************************************
 * Copyright 2012-2013 Trento RISE
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either   express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
