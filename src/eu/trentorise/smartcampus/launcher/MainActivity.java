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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		showDownloadMarket();

		// Getting saved instance
		if (savedInstanceState == null) {
			// Loading first fragment that works as home for application.
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			Fragment frag = new AppFragment();
			ft.add(R.id.fragment_container, frag).commit();
		}

	}

	private void showDownloadMarket() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.dialog_market_title))
				.setMessage(getString(R.string.dialog_market_info))
				.setNeutralButton(getString(R.string.ok), null);
//		.setPositiveButton(getString(R.string.label_go),
//		new DialogInterface.OnClickListener() {
//
//			@Override
//			public void onClick(DialogInterface dialog,
//					int which) {
////				String package_name = "it.smartcampuslab.launcher";
////				Intent market = new Intent(Intent.ACTION_VIEW,
////						Uri.parse("market://details?id="
////								+ package_name));
////				startActivity(new Intent(MainActivity.this,Wizard.class));
////				finish();
//			}
//		})
		builder.create().show();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		FragmentManager fragmentManager = getSupportFragmentManager();
		try {
			AppFragment appfragment = (AppFragment) fragmentManager
					.findFragmentById(R.id.fragment_container);
			appfragment.flip();
		} catch (ClassCastException e) {

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			String token = data.getExtras().getString(
					AccountManager.KEY_AUTHTOKEN);
			if (token == null) {
				Toast.makeText(this, getString(R.string.auth_failed),
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, getString(R.string.token_required),
					Toast.LENGTH_LONG).show();
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
