package eu.trentorise.smartcampus.launcher;

import it.smartcampuslab.launcher.R;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.actionbarsherlock.app.SherlockActivity;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import eu.trentorise.smartcampus.common.AppInspector;
import eu.trentorise.smartcampus.common.LauncherException;

public class WizardActivity extends SherlockActivity {

	private static int REQUEST_CODE_UNINSTALL_APP = 131;
	private static ArrayList<String> mAppsPackageNames;
	private static int mIndex = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wizard);
		if (savedInstanceState == null
				|| !savedInstanceState.containsKey("installed")) {
			Button btn = (Button) findViewById(R.id.startButton);
			btn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(final View v) {
					AnimatorSet as = new AnimatorSet();
					as.setDuration(500);
					as.playTogether(

					ObjectAnimator.ofObject(v, "backgroundColor",
							new ArgbEvaluator(), Color.rgb(0x33, 0xB5, 0xE5),
							Color.LTGRAY),

					ObjectAnimator.ofFloat(findViewById(R.id.progressBar1),
							"alpha", 0, 1.0f), ObjectAnimator.ofFloat(
							findViewById(R.id.wiz_msg), "alpha", 0, 1.0f));

					as.start();
					buildUninstallList();
					v.setEnabled(false);
				}
			});
		} else {
			mAppsPackageNames = savedInstanceState
					.getStringArrayList("installed");
			mIndex = savedInstanceState.getInt("index");

		}

	}

	private void process() {
		if (!mAppsPackageNames.isEmpty() && mIndex < mAppsPackageNames.size())
			unistall(mAppsPackageNames.get(mIndex));
		else
			this.finish();
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
								"Vuoi uscire dal wizard?\nSe non lo completi, non potrai utilizzare le nostre app.")
						.setNeutralButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
								process();
							}
						})
						.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
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
