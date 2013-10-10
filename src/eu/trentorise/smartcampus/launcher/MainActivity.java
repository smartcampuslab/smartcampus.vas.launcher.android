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
package eu.trentorise.smartcampus.launcher;

import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.ac.Constants;
import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.android.common.GlobalConfig;



public class MainActivity extends SherlockFragmentActivity {
	

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		LauncherHelper.init(this);
		
		try {
			initGlobalConstants();
			SCAccessProvider.getInstance(this).login(this, LauncherHelper.getCLIENT_ID(), LauncherHelper.getCLIENT_SECRET(), null);
		
		} catch (Exception e) {
			Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
			finish();
		}

    	// Getting saved instance
		if (savedInstanceState == null) {
			// Loading first fragment that works as home for application.
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment frag = new AppFragment();
			ft.add(R.id.fragment_container, frag).commit();
		}
	}

	private void initGlobalConstants() throws NameNotFoundException, NotFoundException {
		GlobalConfig.setAppUrl(this, getResources().getString(R.string.smartcampus_app_url));
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		  FragmentManager fragmentManager = getSupportFragmentManager();
		  try{
		  AppFragment appfragment =  (AppFragment) fragmentManager.findFragmentById(R.id.fragment_container);	
		  appfragment.flip();
		  } catch (ClassCastException e){
			  
		  }
		  }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			String token = data.getExtras().getString(AccountManager.KEY_AUTHTOKEN);
			if (token == null) {
				Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, getString(R.string.token_required), Toast.LENGTH_LONG).show();
			finish();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.emptymenu, menu);
		return true;
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case android.R.id.home:
	        	onBackPressed();
	    }
		return super.onOptionsItemSelected(item);
	}

	
}
