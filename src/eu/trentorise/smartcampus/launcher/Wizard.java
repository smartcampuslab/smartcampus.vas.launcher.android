package eu.trentorise.smartcampus.launcher;

import java.util.ArrayList;
import java.util.Iterator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockActivity;

import eu.trentorise.smartcampus.launcher.AppFragment.AppItem;

public class Wizard extends SherlockActivity {

	private static int REQUEST_CODE_UNINSTALL_APP = 131;
	private static ArrayList<String> mAppsPackageNames;
	private static int mIndex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard);
		if (savedInstanceState == null
				|| !savedInstanceState.containsKey("installed")) {
			buildUninstallList();
			Button btn = (Button) findViewById(R.id.startButton);
			btn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					process();
				}
			});
		} else {
			mAppsPackageNames = savedInstanceState
					.getStringArrayList("installed");
			mIndex = savedInstanceState.getInt("index");

			process();

		}

	}

	private void process() {
		if (mIndex < mAppsPackageNames.size())
			unistall(mAppsPackageNames.get(mIndex));
		else {
			String package_name = "it.smartcampuslab.launcher";
			Intent market = new Intent(Intent.ACTION_VIEW,
					Uri.parse("market://details?id=" + package_name));

			market.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getBaseContext().startActivity(market);
			Handler selfUninstallHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					unistall("eu.trentorise.smartcampus.launcher");
				}
			};
			selfUninstallHandler.sendMessageDelayed(new Message(), 500);
		}
	}

	private void buildUninstallList() {

		if (LauncherHelper.items != null) {
			mAppsPackageNames = new ArrayList<String>(
					LauncherHelper.items.size());
			for (AppItem i : LauncherHelper.items)
				mAppsPackageNames.add(i.app.appPackage);
			mAppsPackageNames.remove(mAppsPackageNames.size() - 1);
		} else
			this.finish();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("index", ++mIndex);
		outState.putStringArrayList("installed", mAppsPackageNames);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_UNINSTALL_APP) {
			// uninstaller();
		} else
			super.onActivityResult(requestCode, resultCode, data);
	}

	private void unistall(String packageName) {
		Uri uri = Uri.parse("package:" + packageName);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		startActivityForResult(intent, REQUEST_CODE_UNINSTALL_APP);
	}
}
