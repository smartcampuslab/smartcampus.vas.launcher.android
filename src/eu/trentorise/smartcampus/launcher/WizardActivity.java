package eu.trentorise.smartcampus.launcher;

import it.smartcampuslab.launcher.R;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;

import eu.trentorise.smartcampus.common.AppInspector;
import eu.trentorise.smartcampus.common.LauncherException;

public class WizardActivity extends SherlockActivity {

	private static int REQUEST_CODE_UNINSTALL_APP = 131;
	private static ArrayList<String> mAppsPackageNames;
	private static int mIndex = 0;

	private TextView mTextView;
	private Button mStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard);

		mTextView = (TextView) findViewById(R.id.wiz_msg);
		
		
		mStart = (Button) findViewById(R.id.startButton);
		mStart.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(final View v) {
				buildUninstallList();
				v.setEnabled(false);
			}
		});

		if (savedInstanceState != null
				&& savedInstanceState.containsKey("installed")) {

			mAppsPackageNames = savedInstanceState
					.getStringArrayList("installed");
			mIndex = savedInstanceState.getInt("index");

		}

	}

	private void process() {
		if (!mAppsPackageNames.isEmpty() && mIndex < mAppsPackageNames.size())
			unistall(mAppsPackageNames.get(mIndex));
		else {
			startActivity(new Intent(this, MainActivity.class));
			this.finish();
		}
	}

	private void buildUninstallList() {

		AsyncTask<String, Void, Void> a = new AsyncTask<String, Void, Void>() {

			@Override
			protected Void doInBackground(String... params) {
				AppInspector ai = new AppInspector(WizardActivity.this);
				mAppsPackageNames = new ArrayList<String>(params.length);
				for (String packageName : params) {
					try {
						ai.isPackageInstalled(packageName);
						mAppsPackageNames.add(packageName);
					} catch (LauncherException le) {
					}
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				super.onPostExecute(result);
				process();
			}
		};
		a.execute(getResources().getStringArray(R.array.old_app_packages));
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("index", mIndex);
		outState.putStringArrayList("installed", mAppsPackageNames);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_UNINSTALL_APP) {
			AppInspector ai = new AppInspector(WizardActivity.this);
			try {
				ai.isPackageInstalled(mAppsPackageNames.get(mIndex));
				AlertDialog.Builder builder = new AlertDialog.Builder(
						WizardActivity.this);
				builder.setTitle(android.R.string.dialog_alert_title)
						.setMessage(
								R.string.wiz_dialog)
						.setOnCancelListener(
								new DialogInterface.OnCancelListener() {

									@Override
									public void onCancel(DialogInterface dialog) {
										process();
									}
								})
						.setNeutralButton(android.R.string.cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										process();
									}
								})
						.setPositiveButton(android.R.string.ok,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										System.exit(0);
									}
								});
				builder.create().show();
			} catch (LauncherException le) {
				mIndex++;
				process();
			}

		} else
			super.onActivityResult(requestCode, resultCode, data);
	}

	private void unistall(String packageName) {
		Uri uri = Uri.parse("package:" + packageName);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		startActivityForResult(intent, REQUEST_CODE_UNINSTALL_APP);
	}
}
