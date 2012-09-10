package eu.trentorise.smartcampus.launcher;

import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.Toast;
import eu.trentorise.smartcampus.ac.embedded.EmbeddedSCAccessProvider;

public class MainActivity extends FragmentActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		try {
			new EmbeddedSCAccessProvider().getAuthToken(this, null);
		} catch (OperationCanceledException e) {
			Toast.makeText(this, getString(R.string.token_required), Toast.LENGTH_LONG).show();
			finish();
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			String token = data.getExtras().getString(AccountManager.KEY_AUTHTOKEN);
			if (token == null) {
				Toast.makeText(this, getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(this, getString(R.string.token_required), Toast.LENGTH_LONG).show();
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	
}
