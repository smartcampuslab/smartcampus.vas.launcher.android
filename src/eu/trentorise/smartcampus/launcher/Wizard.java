package eu.trentorise.smartcampus.launcher;

import java.util.Iterator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;

import eu.trentorise.smartcampus.launcher.AppFragment.AppItem;

public class Wizard extends SherlockActivity {

	private static int REQUEST_CODE_UNINSTALL_APP = 131;
	private static Iterator<AppItem> mAppsItems;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if(mAppsItems==null)
			mAppsItems= LauncherHelper.items.iterator();
		uninstaller();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode== REQUEST_CODE_UNINSTALL_APP){
			
		}
		else
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void uninstaller(){
		if(mAppsItems.hasNext())
			unistall(mAppsItems.next());
	}
	
	private void unistall(AppItem item) {
		Uri uri = Uri.parse("package:" + item.app.appPackage);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		startActivityForResult(intent, REQUEST_CODE_UNINSTALL_APP);
	}
}
