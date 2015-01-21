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
package eu.trentorise.smartcampus.launcher.auth;

import it.smartcampuslab.launcher.R;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import eu.trentorise.smartcampus.ac.AuthActivity;

/**
 *  Implementation of the {@link AuthActivity} storing the acquired token
 * in the {@link AccountManager} infrastructure and broadcasting the result event.
 * @author raman
 *
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		showDisclaimer();
	}
	
	private void showDisclaimer() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		WebView wv = new WebView(this);
		wv.loadData(getString(R.string.disclaimer), "text/html; charset=UTF-8", "utf-8");

		builder//.setTitle(android.R.string.dialog_alert_title)
				.setView(wv)
//				.setMessage(R.string.welcome_msg)
				.setOnCancelListener(
						new DialogInterface.OnCancelListener() {

							@Override
							public void onCancel(DialogInterface arg0) {
								 final Intent intent = new Intent();
								 setAccountAuthenticatorResult(intent.getExtras());
								 setResult(RESULT_CANCELED, intent);
								 finish();  	    		  
							}
						})
				.setPositiveButton(getString(R.string.close),
								new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								 final Intent intent = new Intent();
								 setAccountAuthenticatorResult(intent.getExtras());
								 setResult(RESULT_CANCELED, intent);
								 finish();  	    		  
							}
						});
		builder.create().show();
	}

}
