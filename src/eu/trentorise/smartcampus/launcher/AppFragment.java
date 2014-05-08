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

import it.smartcampuslab.launcher.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.android.common.LauncherHelper;
import eu.trentorise.smartcampus.common.AppInspector;
import eu.trentorise.smartcampus.common.LauncherException;
import eu.trentorise.smartcampus.common.Status;
import eu.trentorise.smartcampus.launcher.models.SmartApp;
import eu.trentorise.smartcampus.launcher.settings.SettingsActivity;
import eu.trentorise.smartcampus.launcher.util.ConnectionUtil;
import eu.trentorise.smartcampus.launcher.widget.TileButton;

/**
 * 
 * Fragment that allows to page apps icon and manage user interactions.
 * 
 * @author Simone Casagranda
 * 
 */
public class AppFragment extends SherlockFragment {

	private ConnectivityManager mConnectivityManager;
	private AppInspector mInspector;

	// variable used for forcing refresh coming back from setting activity

	private static final String KEY_UPDATE_REFRESH = "refresh";
	private static final int NUMBER_OF_ROWS = 4;
	private static final int NUMBER_OF_COLUMNS = 2;

	private GridView mGridView;
	private AppAdapter mAdapter;
	private List<AppItem> mAppItems = new ArrayList<AppItem>();;
	private int heightActionBar = 0;
	private AppTask mAppTask;
	public static final String PREFS_NAME = "LauncherPreferences";

	// private ProgressDialog progress = null;

	// force the update pressing the menu button

	private boolean forced = false;
	private SharedPreferences settings = null;

	@Override
	public void onCreate(Bundle args) {
		super.onCreate(args);
		// Getting connectivity manager
		mConnectivityManager = ConnectionUtil
				.getConnectivityManager(getSherlockActivity());
		// Getting inspector
		mInspector = new AppInspector(getSherlockActivity());
		// Asking for an option menu
		setHasOptionsMenu(true);

		settings = getSherlockActivity().getSharedPreferences(PREFS_NAME, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle args) {
		View v = inflater.inflate(R.layout.frag_apps, null);
		// Getting UI references
		mGridView = (GridView) v.findViewById(R.id.gridview);
		// mGridView.setEnabled(false); // disable scrolling
		// mGridView.setVerticalScrollBarEnabled(false);
		return v;
	}

	@Override
	public void onViewCreated(View v, Bundle args) {
		super.onViewCreated(v, args);
		mAdapter = new AppAdapter(mAppItems);
		mGridView.setAdapter(mAdapter);
	}

	public void flip() {
		mAdapter = new AppAdapter(mAppItems);
		mGridView.setAdapter(mAdapter);

	}

	@Override
	public void onStart() {
		super.onStart();
		getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(false);
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(
				false);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(
				true);
		getSherlockActivity().getSupportActionBar().setTitle(
				getString(R.string.app_name));

		if (getSherlockActivity().getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSherlockActivity().getSupportActionBar().setNavigationMode(
					ActionBar.NAVIGATION_MODE_STANDARD);
		}
		// Starting new task
		startNewAppTask();

	}

	@Override
	public void onStop() {
		super.onStop();
		// Stopping any active task
		stopAnyActiveAppTask();
	}

	private void startNewAppTask() {
		// Stopping task
		stopAnyActiveAppTask();
		// Starting new one
		mAppTask = new AppTask();
		mAppTask.execute();
	}

	private void stopAnyActiveAppTask() {
		if (mAppTask != null && !mAppTask.isCancelled()) {
			mAppTask.cancel(true);
		}
	}

	private void downloadApplication(String url, String name) {
		if (ConnectionUtil.isConnected(mConnectivityManager)) {
			// Checking url
			if (!TextUtils.isEmpty(url)) {
				startPlayStore(url);
			} else {
				Log.d(AppFragment.class.getName(), "Empty url for download: "
						+ name);
				Toast.makeText(getSherlockActivity(), R.string.error_occurs,
						Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getSherlockActivity(), R.string.enable_connection,
					Toast.LENGTH_SHORT).show();
			Intent intent = ConnectionUtil.getWifiSettingsIntent();
			startActivity(intent);
		}
	}

	private void startPlayStore(String url) {
		Intent openPlayStore = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		Log.i(AppFragment.class.getName(), "open market for: " + url);
		startActivity(openPlayStore);
	}

	// Task that retrieves applications info
	private class AppTask extends AsyncTask<Void, Void, List<AppItem>> {

		@Override
		protected void onPreExecute() {
			if (settings.getBoolean(KEY_UPDATE_REFRESH, false)) {
				forced = true;
				SharedPreferences.Editor editor = settings.edit();
				editor.remove(KEY_UPDATE_REFRESH).commit();
			}
			// if (((toUpdate) && (progress == null)) || forced)
			// progress = ProgressDialog.show(getSherlockActivity(), "",
			// "Checking applications version", true);

		};

		@Override
		protected List<AppItem> doInBackground(Void... params) {

			List<AppItem> items = new ArrayList<AppItem>();
			List<AppItem> notInstalledItems = new ArrayList<AppItem>();
			// Getting applications names, packages, ...
			String[] labels = getResources().getStringArray(R.array.app_labels);
			String[] packages = getResources().getStringArray(
					R.array.app_packages);
			String[] backgrounds = getResources().getStringArray(
					R.array.app_backgrounds);

			String url = getResources().getString(R.string.open_playstore_url);

			int[] versions = getResources().getIntArray(R.array.app_version);
			String[] filenames = getResources().getStringArray(
					R.array.apk_filename);

			TypedArray icons = getResources().obtainTypedArray(
					R.array.app_icons);
			TypedArray grayIcons = getResources().obtainTypedArray(
					R.array.app_gray_icons);
			// They have to be the same length
			assert labels.length == packages.length
					&& labels.length == backgrounds.length
					&& labels.length == icons.length()
					&& labels.length == grayIcons.length();
			// Preparing all items
			for (int i = 0; i < labels.length; i++) {
				AppItem item = new AppItem();
				item.app = new SmartApp();

				item.app.fillApp(labels[i], packages[i],
						buildUrlDownloadApp(url, packages[i]),
						icons.getDrawable(i), grayIcons.getDrawable(i),
						backgrounds[i], versions[i], filenames[i]);
				try {
					mInspector.isAppInstalled(item.app.appPackage);
					item.status = eu.trentorise.smartcampus.common.Status.OK;

				} catch (LauncherException e) {
					e.printStackTrace();
					// Getting status
					item.status = e.getStatus();
				}
				// Matching just retrieved status
				switch (item.status) {
				case OK:
					items.add(item);
					break;
				default:
					// Not installed list
					notInstalledItems.add(item);
					break;
				}
			}
			// Concatenation of not installed ones
			items.addAll(notInstalledItems);
			// Returning result
			return items;
		}

		private String buildUrlDownloadApp(String url, String packageID) {
			return String.format(url, packageID);
		}

		@Override
		protected void onPostExecute(List<AppItem> result) {
			super.onPostExecute(result);
			// se anche il launcher
			// if (progress != null) {
			// try {
			// progress.cancel();
			// progress = null;
			// } catch (Exception e) {
			// Log.w(getClass().getName(), "Problem closing progress dialog: " +
			// e.getMessage());
			// }
			// }
			int i = 0;
			for (AppItem app : result) {
				if (app.app.name.compareTo("Launcher") == 0)
					break;
				i++;
			}

			// tolgo dalle app normali il launcher
			result.remove(i);

			// Clearing items list
			mAppItems.clear();
			// Checking result
			if (result != null) {
				mAppItems.addAll(result);

			}
			AppFragment.this.getSherlockActivity()
					.supportInvalidateOptionsMenu();
			// Notifying adapter
			mAdapter.notifyDataSetChanged();
			if (forced)
				forced = false;
		}

	}

	static class ViewHolder {
		TileButton button;
	}

	// Array adapter for GridView
	private class AppAdapter extends ArrayAdapter<AppItem> {

		AppInspector mAppInspector = new AppInspector(getSherlockActivity());
		int mWidth, mHeight;

		public AppAdapter(List<AppItem> items) {
			super(getSherlockActivity(), R.layout.item_app_tile, items);

			// declare the listener for the update dialogBox

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = new ViewHolder();

			convertView = LayoutInflater.from(getSherlockActivity()).inflate(
					R.layout.item_app_tile, null);
			// Create Holder
			holder.button = new TileButton(convertView);
			// add Holder to View
			convertView.setTag(holder.button);
			// Calculating sizes

			// Sometimes it's called and it's bigger than the screen (maybe
			// without the action bar??).
			// So actually it's called every time it's reloaded

			// if(mWidth<1||mHeight<1){
			Rect rectgle = new Rect();
			Window window = getSherlockActivity().getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
			int statusBarHeight = rectgle.top;
			int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT)
					.getTop();
			// Dimension
			Display display = getSherlockActivity().getWindowManager()
					.getDefaultDisplay();
			// We are using android v8
			heightActionBar = getSherlockActivity().getSupportActionBar()
					.getHeight();
			mWidth = Math.round(display.getWidth() / NUMBER_OF_COLUMNS);
			mHeight = Math
					.round(((display.getHeight() - (heightActionBar)) - statusBarHeight)
							/ NUMBER_OF_ROWS); // problem
												// if
												// the
												// sum
												// of
												// the
												// tails
												// is
												// less
												// or
												// great
												// than
												// the
												// number
			// if the row is the last add the difference (positive or negative)
			if (position >= (NUMBER_OF_COLUMNS * (NUMBER_OF_ROWS - 1))) {
				mHeight = mHeight
						+ (display.getHeight() - (mHeight * NUMBER_OF_ROWS
								+ heightActionBar + statusBarHeight));
			}
			// Setting sizes
			convertView.setMinimumWidth(mWidth);
			convertView.setMinimumHeight(mHeight);

			// Getting item
			final AppItem item = getItem(position);
			holder.button.setText(item.app.name);
			// Checking status for colors
			if (item.status == eu.trentorise.smartcampus.common.Status.OK) {
				holder.button.setImage(item.app.icon);
				holder.button.setBackgroundColor(item.app.background);
				holder.button.setTextColor(Color.WHITE);
				holder.button.mUpdateVisible(false);

			} else {
				holder.button.setImage(item.app.gray_icon);
				holder.button.setBackgroundColor(getResources().getColor(
						R.color.tile_background_unsel));
				holder.button.setTextColor(getResources().getColor(
						R.color.tile_text_unsel));
			}

			// //temporary Coming Soon
			// if
			// ("eu.trentorise.smartcampus.studymate".equals(item.app.appPackage))
			// {
			// holder.button.setOnClickListener(new OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// // Asking user to remove application
			// Toast.makeText(getActivity(), R.string.label_coming_soon,
			// Toast.LENGTH_SHORT).show();
			// }
			// });
			// return convertView;
			// }

			// Setting application info name
			switch (item.status) {
			case OK:
				holder.button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							mAppInspector
									.launchApp(
											item.app.appPackage,
											getString(R.string.smartcampus_action_start),
											null, null);
						} catch (LauncherException e) {
							e.printStackTrace();
						}
					}
				});
				break;
			case NOT_FOUND:
				holder.button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						downloadApplication(item.app.url, item.app.name);
					}
				});
				break;
			case NOT_VALID_UID:
				holder.button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						// Asking user to remove application
						Toast.makeText(getSherlockActivity(),
								R.string.not_secure_app, Toast.LENGTH_SHORT)
								.show();
					}
				});
				break;
			default:
				// Others haven't any importance
				holder.button.setOnClickListener(null);
				break;
			}
			// Returning just filled view
			return convertView;
		}

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu,
				menu);
		SubMenu submenu = menu.getItem(1).getSubMenu();
		submenu.clear();
		submenu.setIcon(R.drawable.ic_action_overflow);
		// submenu.add(Menu.CATEGORY_SYSTEM, R.id.settings, Menu.NONE,
		// R.string.settings);// settings
		// page
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.about, Menu.NONE, R.string.about);// about
																					// page

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		AboutFragment newFragment;
		Bundle args;
		FragmentTransaction transaction;
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(getSherlockActivity(),
					SettingsActivity.class));
			return true;
		case R.id.about:
			// Intent browserIntent = new Intent(Intent.ACTION_VIEW,
			// Uri.parse(getString(R.string.smartcampus_url_credits)));
			// startActivity(browserIntent);
			newFragment = new AboutFragment();
			args = new Bundle();
			newFragment.setArguments(args);
			transaction = getSherlockActivity().getSupportFragmentManager()
					.beginTransaction();
			transaction.replace(R.id.fragment_container, newFragment);
			transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
			transaction.addToBackStack(null);
			transaction.commit();
			return true;
		case R.id.help:
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//			TextView tv = new TextView(getActivity());
//			tv.setPadding(20, 20, 20, 20);
//			tv.setTextSize(18f);
//			tv.setText(Html.fromHtml(getString(R.string.about_2)));
			WebView wv = new WebView(getActivity());
			wv.loadData(getString(R.string.about_2), "text/html", "utf-8");
			//tv.setMovementMethod(LinkMovementMethod.getInstance());
			builder.setTitle("").setView(wv).create().show();
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}

	}

	// Item wrapper of a smartApp
	class AppItem {
		SmartApp app;
		eu.trentorise.smartcampus.common.Status status = Status.NOT_FOUND;
	}

}
