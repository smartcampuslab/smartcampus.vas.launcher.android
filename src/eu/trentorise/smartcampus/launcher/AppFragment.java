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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.common.AppInspector;
import eu.trentorise.smartcampus.common.LauncherException;
import eu.trentorise.smartcampus.common.Status;
import eu.trentorise.smartcampus.launcher.apps.ApkInstaller.ApkDownloaderTask;
import eu.trentorise.smartcampus.launcher.models.SmartApp;
import eu.trentorise.smartcampus.launcher.models.UpdateModel;
import eu.trentorise.smartcampus.launcher.settings.SettingsActivity;
import eu.trentorise.smartcampus.launcher.util.ConnectionUtil;
import eu.trentorise.smartcampus.launcher.widget.TileButton;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;

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

	// variable used for changing version downloaded

	private static final String KEY_UPDATE_DEV = "update_dev";

	// variable used for forcing refresh coming back from setting activity

	private static final String KEY_UPDATE_REFRESH = "refresh";

	private GridView mGridView;
	private AppAdapter mAdapter;
	private List<AppItem> mAppItems = new ArrayList<AppItem>();;
	private int heightActionBar = 0;
	private AppTask mAppTask;
	private ApkDownloaderTask mDownloaderTask;
	public static final String PREFS_NAME = "LauncherPreferences";
	private static final String UPDATE = "_updateModel";
	private String UPDATE_ADDRESS = null;
	private String UPDATE_ADDRESS_DEV = null;
	private String UPDATE_HOST = null;
	private static final String LAUNCHER = "SmartLAuncher";
	private Drawable ic_update;
	private boolean availableUpdate = false;
	private int[] version;
	private boolean toUpdate = true;
	private boolean isDialogOpen = false;
	private ProgressDialog progress = null;

	// force the update pressing the menu button

	private boolean forced = false;
	private SharedPreferences settings = null;

	@Override
	public void onCreate(Bundle args) {
		super.onCreate(args);
		// Getting connectivity manager
		mConnectivityManager = ConnectionUtil.getConnectivityManager(getActivity());
		// Getting inspector
		mInspector = new AppInspector(getActivity());
		// Asking for an option menu
		setHasOptionsMenu(true);
		UPDATE_ADDRESS = getResources().getString(R.string.update_address);
		UPDATE_ADDRESS_DEV = getResources().getString(R.string.update_address_develop);

		UPDATE_HOST = getResources().getString(R.string.update_host);

		// if you have some problem with the stored data, uncomment these lines
		// and the data are erased
		/*
		 * SharedPreferences settings =
		 * getActivity().getSharedPreferences(PREFS_NAME, 0);
		 * SharedPreferences.Editor editor = settings.edit(); editor.clear();
		 * editor.commit();
		 */

		ic_update = getResources().getDrawable(R.drawable.ic_app_update);
		settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle args) {
		View v = inflater.inflate(R.layout.frag_apps, null);
		// Getting UI references
		mGridView = (GridView) v.findViewById(R.id.gridview);
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
		getSherlockActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getSherlockActivity().getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSherlockActivity().getSupportActionBar().setTitle(getString(R.string.app_name));

		if (getSherlockActivity().getSupportActionBar().getNavigationMode() != ActionBar.NAVIGATION_MODE_STANDARD) {
			getSherlockActivity().getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
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

	private int[] readUpdateVersions(String[] packageNames,
			int[] defaultVersions) {


		int[] res = defaultVersions;
		UpdateModel update = null;
		long nextUpdate = -1;
		if (settings != null && settings.contains(UPDATE)) {
			nextUpdate = settings.getLong(UPDATE, -1);
		}
		

		if (nextUpdate < System.currentTimeMillis() || forced) {
			//if press the button check now and don't the next time
			if (!forced)
				toUpdate = true;

			// try to update
			//MessageRequest req = new MessageRequest(UPDATE_HOST, UPDATE_ADDRESS);
			
			String destination = new String(UPDATE_ADDRESS);
			if (settings.getBoolean(KEY_UPDATE_DEV,	false))
			{
				destination=UPDATE_ADDRESS_DEV;
			}
			MessageRequest req = new MessageRequest(UPDATE_HOST, destination);
			
			
			
			req.setMethod(Method.GET);
			ProtocolCarrier pc = new ProtocolCarrier(getActivity(), LAUNCHER);
			try {
				MessageResponse mres = pc.invokeSync(req, LAUNCHER,
						SCAccessProvider.getInstance(getSherlockActivity()).readToken(getActivity(), LauncherHelper.getCLIENT_ID(), LauncherHelper.getCLIENT_SECRET()));
				if (mres != null && mres.getBody() != null) {
					// Update from variable sec
					Calendar dateCal = Calendar.getInstance();
					dateCal.setTime(new Date());
					dateCal.add(Calendar.SECOND,
							getResources().getInteger(R.integer.check_interval));
					nextUpdate = dateCal.getTime().getTime();
					update = new UpdateModel(mres.getBody());
					settings.edit().putLong(UPDATE, nextUpdate).commit();
					for (int i = 0; i < packageNames.length; i++) {
						Integer version = update.getVersion(packageNames[i]);
						res[i] = version == null ? 0 : version;
						settings.edit()
								.putInt(packageNames[i] + "-version", version)
								.commit();
					}
					version = res;
				}
			} catch (Exception e) {
				Log.e(AppFragment.class.getName(),
						"Error reading update config: " + e.getMessage());
			}
		} else {
			toUpdate = false;
			for (int i = 0; i < packageNames.length; i++) {
				res[i] = settings.getInt(packageNames[i] + "-version", 0);
			}
			version = res;
		}

		return res;
	}

	// Task that retrieves applications info
	private class AppTask extends AsyncTask<Void, Void, List<AppItem>> {
		private DialogInterface.OnClickListener updateDialogClickListener;
		private AppItem launcher;

		@Override
		protected void onPreExecute() {
			if (settings.getBoolean(KEY_UPDATE_REFRESH, false)) {
				forced = true;
				SharedPreferences.Editor editor = settings.edit();
				editor.remove(KEY_UPDATE_REFRESH).commit();
			}
			if (((toUpdate) && (progress == null)) || forced)
				progress = ProgressDialog.show(getSherlockActivity(), "", "Checking applications version", true);

		};

		@Override
		protected List<AppItem> doInBackground(Void... params) {

			List<AppItem> items = new ArrayList<AppItem>();
			List<AppItem> notInstalledItems = new ArrayList<AppItem>();
			// Getting applications names, packages, ...
			String[] labels = getResources().getStringArray(R.array.app_labels);
			String[] packages = getResources().getStringArray(R.array.app_packages);
			String[] backgrounds = getResources().getStringArray(R.array.app_backgrounds);
			String url = getResources().getString(R.string.smartcampus_url_apk);
			int[] versions = getResources().getIntArray(R.array.app_version);
			String[] filenames = getResources().getStringArray(R.array.apk_filename);

			versions = readUpdateVersions(packages, versions);

			Drawable ic_update = getResources().getDrawable(R.drawable.ic_app_update);

			TypedArray icons = getResources().obtainTypedArray(R.array.app_icons);
			TypedArray grayIcons = getResources().obtainTypedArray(R.array.app_gray_icons);
			// They have to be the same length
			assert labels.length == packages.length && labels.length == backgrounds.length
					&& labels.length == icons.length() && labels.length == grayIcons.length();
			// Preparing all items
			for (int i = 0; i < labels.length; i++) {
				AppItem item = new AppItem();
				item.app = new SmartApp();

				item.app.fillApp(labels[i], packages[i],
						buildUrlDownloadApp(url, packages[i], versions[i], filenames[i]), icons.getDrawable(i),
						grayIcons.getDrawable(i), backgrounds[i], versions[i], filenames[i]);
				try {
					mInspector.isAppInstalled(item.app.appPackage);
					item.status = eu.trentorise.smartcampus.common.Status.OK;
					if (!mInspector.isAppUpdated(item.app.appPackage, versions[i]))
						item.status = eu.trentorise.smartcampus.common.Status.NOT_UPDATED;
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
				case NOT_UPDATED:
					// Installed but not updated
					items.add(item);
					// actually is the same of OK
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

		private String buildUrlDownloadApp(String url, String packages, int versions, String filenames) {
			return new String(url + packages + "/" + versions + "/" + filenames);
		}

		@Override
		protected void onPostExecute(List<AppItem> result) {
			super.onPostExecute(result);
			// se anche il launcher
			if (progress != null) {
				try {
					progress.cancel();
					progress = null;
				} catch (Exception e) {
					Log.w(getClass().getName(), "Problem closing progress dialog: " + e.getMessage());
				}
			}
			int i = 0;
			for (AppItem app : result) {
				if (app.app.name.compareTo("Launcher") == 0)
					break;
				i++;
			}
			launcher = result.get(i);
			updateDialogClickListener = new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					isDialogOpen = false;
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						// If yes is pressed download the new app

						downloadApplication(launcher.app.url, launcher.app.name);

						break;

					case DialogInterface.BUTTON_NEGATIVE:
						// If no is pressed add to the manual list applications
						// update
						// Put the application in the blacklist in the
						// SharedPreferences
						SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
						SharedPreferences.Editor editor = settings.edit();
						if (settings.getBoolean(launcher.app.name + "-update", true)) {
							editor.putBoolean(launcher.app.name + "-update", false);
							editor.commit();

						}
						Toast.makeText(getSherlockActivity(), getString(R.string.update_application_manual_list),
								Toast.LENGTH_SHORT).show();

						availableUpdate = true;

						AppFragment.this.getSherlockActivity().invalidateOptionsMenu();
						// change the icon like the updated, notifica che e'
						// cambiato
						mAdapter.notifyDataSetChanged();
						break;
					}
				}
			};
			if (launcher.status == eu.trentorise.smartcampus.common.Status.NOT_UPDATED)// e
																						// non
																						// e'
																						// nella
																						// blacklist;l
			{
				SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
				settings.toString();
				boolean autoupdate = settings.getBoolean(launcher.app.name + "-update", true);
				if ((autoupdate) && (!isDialogOpen)) {
					// update
					AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
					builder.setMessage(getString(R.string.update_application_question))
							.setPositiveButton("Yes", updateDialogClickListener)
							.setNegativeButton("No", updateDialogClickListener).show();
					isDialogOpen = true;

				} else {
					// not update
					availableUpdate = true;

				}
			}

			// tolgo dalle app normali il launcher
			result.remove(i);

			// Clearing items list
			mAppItems.clear();
			// Checking result
			if (result != null) {
				mAppItems.addAll(result);

			}
			AppFragment.this.getSherlockActivity().invalidateOptionsMenu();
			// Notifying adapter
			mAdapter.notifyDataSetChanged();
			if (forced)
				forced = false;
		}

		private void downloadApplication(String url, String name) {
			if (ConnectionUtil.isConnected(mConnectivityManager)) {
				// Checking url
				if (!TextUtils.isEmpty(url)) {
					if (mDownloaderTask != null && !mDownloaderTask.isCancelled()) {
						mDownloaderTask.cancel(true);
					}
					mDownloaderTask = new ApkDownloaderTask(getActivity(), url);
					mDownloaderTask.execute();
				} else {
					Log.d(AppFragment.class.getName(), "Empty url for download: " + name);
					Toast.makeText(getActivity(), R.string.error_occurs, Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getActivity(), R.string.enable_connection, Toast.LENGTH_SHORT).show();
				Intent intent = ConnectionUtil.getWifiSettingsIntent();
				startActivity(intent);
			}
		}

	}

	static class ViewHolder {
		TileButton button;
	}

	// Array adapter for GridView
	private class AppAdapter extends ArrayAdapter<AppItem> {

		AppInspector mAppInspector = new AppInspector(getActivity());
		int mWidth, mHeight;
		DialogInterface.OnClickListener updateDialogClickListener;

		public AppAdapter(List<AppItem> items) {
			super(getActivity(), R.layout.item_app_tile, items);

			// declare the listener for the update dialogBox

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = new ViewHolder();

			// Inflate View for ListItem
			convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_app_tile, null);
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
			Window window = getActivity().getWindow();
			window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
			int statusBarHeight = rectgle.top;
			int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
			// Dimension
			Display display = getActivity().getWindowManager().getDefaultDisplay();
			// We are using android v8
			heightActionBar = getSherlockActivity().getSupportActionBar().getHeight();
			mWidth = Math.round(display.getWidth() / 2f);
			mHeight = Math.round(((display.getHeight() - heightActionBar) - statusBarHeight) / 3f);
			// }

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
				holder.button.setBackgroundColor(getResources().getColor(R.color.tile_background_unsel));
				holder.button.setTextColor(getResources().getColor(R.color.tile_text_unsel));
			}

			if (item.status == eu.trentorise.smartcampus.common.Status.NOT_UPDATED)// e
																					// non
																					// e'
																					// nella
																					// blacklist;l
			{
				SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
				settings.toString();
				boolean autoupdate = settings.getBoolean(item.app.name + "-update", true);
				if (autoupdate) {
					// if update is not set (true) and is not updated
					holder.button.setImage(item.app.icon);
					holder.button.setBackgroundColor(item.app.background);
					holder.button.setTextColor(Color.WHITE);
					holder.button.setmUpdate(ic_update);
					holder.button.mUpdateVisible(true);
				} else {
					// if it is set to false is manual
					holder.button.setImage(item.app.icon);
					holder.button.setBackgroundColor(item.app.background);
					holder.button.setTextColor(Color.WHITE);
					holder.button.mUpdateVisible(false);
					item.status = Status.OK;
					// menu available update presente
					// se ce n'e' uno NOT UPDATED => true
					availableUpdate = true;
					AppFragment.this.getSherlockActivity().invalidateOptionsMenu();
				}

			}
			// Setting application info name
			switch (item.status) {
			case OK:
				holder.button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							mAppInspector.launchApp(item.app.appPackage, getString(R.string.smartcampus_action_start),
									null, null);
						} catch (LauncherException e) {
							e.printStackTrace();
						}
					}
				});
				break;

			case NOT_UPDATED:
				holder.button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						// dialog box per fare update
						updateDialogClickListener = new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which) {
								case DialogInterface.BUTTON_POSITIVE:
									// If yes is pressed download the new app

									downloadApplication(item.app.url, item.app.name);
									break;

								case DialogInterface.BUTTON_NEGATIVE:
									// If no is pressed add to the manual list
									// applications update
									// Put the application in the blacklist in
									// the SharedPreferences
									SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
									SharedPreferences.Editor editor = settings.edit();
									if (settings.getBoolean(item.app.name + "-update", true)) {
										editor.putBoolean(item.app.name + "-update", false);
										editor.commit();

									}
									Toast.makeText(getContext(), getString(R.string.update_application_manual_list),
											Toast.LENGTH_SHORT).show();

									availableUpdate = true;

									AppFragment.this.getSherlockActivity().invalidateOptionsMenu();
									// change the icon like the updated,
									// notifica che e' cambiato
									mAdapter.notifyDataSetChanged();
									break;
								}
							}
						};
						AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
						builder.setMessage(getString(R.string.update_application_question))
								.setPositiveButton("Yes", updateDialogClickListener)
								.setNegativeButton("No", updateDialogClickListener).show();

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
						Toast.makeText(getActivity(), R.string.not_secure_app, Toast.LENGTH_SHORT).show();
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

		private void downloadApplication(String url, String name) {
			if (ConnectionUtil.isConnected(mConnectivityManager)) {
				// Checking url
				if (!TextUtils.isEmpty(url)) {
					if (mDownloaderTask != null && !mDownloaderTask.isCancelled()) {
						mDownloaderTask.cancel(true);
					}
					mDownloaderTask = new ApkDownloaderTask(getActivity(), url);
					mDownloaderTask.execute();
				} else {
					Log.d(AppFragment.class.getName(), "Empty url for download: " + name);
					Toast.makeText(getActivity(), R.string.error_occurs, Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getActivity(), R.string.enable_connection, Toast.LENGTH_SHORT).show();
				Intent intent = ConnectionUtil.getWifiSettingsIntent();
				startActivity(intent);
			}
		}

	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);
		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();
		submenu.setIcon(R.drawable.ic_action_overflow);
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.update_option_list, Menu.NONE, R.string.update_option_list).setEnabled(
				availableUpdate()); // enable
									// or
									// disable
									// the
									// option
									// menu
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.check_updates, Menu.NONE, R.string.check_updates);// force
																									// the
																									// update
																									// check
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.settings, Menu.NONE, R.string.settings);// settings
																						// page
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.about, Menu.NONE, R.string.about);// about
																					// page

	}

	/* check if there are update or nor */

	private boolean availableUpdate() {
		return availableUpdate;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.update_option_list:
			FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
			Fragment newFragment = new ManualUpdateFragment();
			Bundle args = new Bundle();
			args.putIntArray("versions", version);
			// Put any other arguments
			newFragment.setArguments(args);
			FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
			transaction.replace(R.id.fragment_container, newFragment);
			transaction.addToBackStack(null);
			transaction.commit();
			return true;
		case R.id.check_updates:
			// force the updates
			forced = true;
			startNewAppTask();
			return true;
		case R.id.settings:
			startActivity(new Intent(getActivity(), SettingsActivity.class));
			return true;
		case R.id.about:
			Intent browserIntent = new Intent(Intent.ACTION_VIEW,
					Uri.parse(getString(R.string.smartcampus_url_credits)));
			startActivity(browserIntent);
			// fragmentTransaction = getActivity().getSupportFragmentManager()
			// .beginTransaction();
			// newFragment = new AboutFragment();
			// args = new Bundle();
			// newFragment.setArguments(args);
			// transaction = getActivity().getSupportFragmentManager()
			// .beginTransaction();
			// transaction.replace(R.id.fragment_container, newFragment);
			// transaction.addToBackStack(null);
			// transaction.commit();
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
