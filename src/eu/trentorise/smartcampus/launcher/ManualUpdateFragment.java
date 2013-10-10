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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.MenuItem;

import eu.trentorise.smartcampus.ac.SCAccessProvider;
import eu.trentorise.smartcampus.common.AppInspector;
import eu.trentorise.smartcampus.common.LauncherException;
import eu.trentorise.smartcampus.common.Status;
import eu.trentorise.smartcampus.launcher.AppFragment.AppItem;
import eu.trentorise.smartcampus.launcher.apps.ApkInstaller.ApkDownloaderTask;
import eu.trentorise.smartcampus.launcher.models.SmartApp;
import eu.trentorise.smartcampus.launcher.models.UpdateModel;
import eu.trentorise.smartcampus.launcher.util.ConnectionUtil;
import eu.trentorise.smartcampus.launcher.widget.TileButton;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;


public class ManualUpdateFragment extends SherlockFragment {
	
	
	 // variable used for changing version downloaded
	 
	private static final String KEY_UPDATE_DEV = "update_dev";
	
	
	 // variable used for forcing refresh coming back from setting activity
	 
	private static final String KEY_UPDATE_REFRESH = "refresh";	
	private String UPDATE_ADDRESS = null;
	private String UPDATE_ADDRESS_DEV = null;
	
	public static final String PREFS_NAME = "LauncherPreferences";
	private ArrayAdapter<AppItem> mListAdapter;
	private ArrayList<AppItem> apps = new ArrayList<AppItem>();
	private DialogInterface.OnClickListener updateDialogClickListener;
	private ConnectivityManager mConnectivityManager;
	private ApkDownloaderTask mDownloaderTask;
	private AppTask mAppTask;
	private AppInspector mInspector;
	private ListView mList;
	private TextView mEmpty;
	private static final String UPDATE = "_updateModel";
	private String UPDATE_HOST = null;
	private static final String LAUNCHER = "SmartLAuncher";
	private int[] version;

	private SharedPreferences settings;
	private boolean forced = false;
	private boolean toUpdate = true;


	public ManualUpdateFragment() {
		super();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mInspector = new AppInspector(getActivity());	
		mConnectivityManager = ConnectionUtil.getConnectivityManager(getActivity());

	}

	@Override
	public void onStart() {
		super.onStart();
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

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (getActivity() instanceof SherlockFragmentActivity) {
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			((SherlockFragmentActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.manual_update));
		}
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,	Bundle args) {
		View v = inflater.inflate(R.layout.manualupdatelist, null);
		// Getting UI references
		mList = (ListView) v.findViewById(R.id.applist);
		mEmpty = (TextView) v.findViewById(R.id.labelEmptyList);
		return v;
	}

	private void startNewAppTask(){
		// Stopping task
		stopAnyActiveAppTask();
		// Starting new one
		mAppTask = new AppTask();
		mAppTask.execute();
	}

	private void stopAnyActiveAppTask(){
		if(mAppTask != null && !mAppTask.isCancelled()){
			mAppTask.cancel(true);
		}
	}

	// Task that retrieves the not updated application info
	private class AppTask extends AsyncTask<Void, Void, List<AppItem>>{

		@Override
		protected List<AppItem> doInBackground(Void... params) {
			settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
			SharedPreferences.Editor editor = settings.edit();

			List<AppItem> notUpdatedItems = new ArrayList<AppItem>();
			// Getting applications names, packages, ...
			String[] labels = getResources().getStringArray(R.array.app_labels);
			String[] packages = getResources().getStringArray(R.array.app_packages);
			String[] backgrounds = getResources().getStringArray(R.array.app_backgrounds);
			String url = getResources().getString(R.string.smartcampus_url_apk);
		//	String[] urls = getResources().getStringArray(R.array.app_urls);
			String[] filenames = getResources().getStringArray(R.array.apk_filename);

			int[] versions = getResources().getIntArray(R.array.app_version);
			versions = readUpdateVersions(packages, versions);

			TypedArray icons = getResources().obtainTypedArray(R.array.app_icons);
			TypedArray grayIcons = getResources().obtainTypedArray(R.array.app_gray_icons);
			// They have to be the same length
			assert labels.length == packages.length
					&& labels.length == backgrounds.length
//					&& labels.length == urls.length
					&& labels.length == icons.length()
					&& labels.length == grayIcons.length();
			// Preparing all items
			for(int i=0;i<labels.length;i++){
				AppFragment outer = new AppFragment();
				AppFragment.AppItem  item = outer.new AppItem();
				item.app = new SmartApp();
				item.app.fillApp(labels[i], packages[i], buildUrlDownloadApp(url,packages[i],versions[i],filenames[i]),
						icons.getDrawable(i), grayIcons.getDrawable(i),
						backgrounds[i], versions[i], filenames[i]);
				try {
					mInspector.isAppInstalled(item.app.appPackage);
					item.status = eu.trentorise.smartcampus.common.Status.OK;
					if (!mInspector.isAppUpdated(item.app.appPackage, versions[i]))
						item.status=eu.trentorise.smartcampus.common.Status.NOT_UPDATED;
					else{
						if (!settings.getBoolean(item.app.name+"-update", true))

						{
							// after a new installation, I must remove the application from the list
							editor.remove(item.app.name+"-update");
							editor.remove(item.app.name+"-version");

							editor.commit();
						}
					}

				} catch (LauncherException e) {
					e.printStackTrace();
					// Getting status
					item.status = e.getStatus();
				}
				// Matching just retrieved status
				switch (item.status) {
				case NOT_UPDATED:
					//Installed but updated
					boolean autoupdate = settings.getBoolean(item.app.name+"-update", true);
					if (!autoupdate)
						notUpdatedItems.add(item);
					break;				
				}				
			}

			// Returning result
			return notUpdatedItems;
		}
//		private int[] readUpdateVersions(String[] packageNames, int[] defaultVersions) {
//
//
//			SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
//			int[] res = defaultVersions; 
//
//			for (int i = 0; i < packageNames.length; i++) {
//				res[i]=settings.getInt(packageNames[i]+"-version", 0);
//			}
//
//			return res;
//		}	
		
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
							SCAccessProvider.getInstance(getSherlockActivity()).readToken(getSherlockActivity(), LauncherHelper.getCLIENT_ID(), LauncherHelper.getCLIENT_SECRET()));
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
		
		private String buildUrlDownloadApp(String url, String packages, int versions,
				String filenames) {
			return  new String(url+packages+"/"+versions+"/"+filenames);
		}
		
		@Override
		protected void onPostExecute(List<AppItem> result) {
			super.onPostExecute(result);
			// Clearing items list
			apps.clear();
			// Checking result
			if(result!=null){
				apps.addAll(result);
				//mAppItems.addAll(result);
			}
			if (result.isEmpty())
			{
				mList.setVisibility(View.INVISIBLE);
				mEmpty.setVisibility(View.VISIBLE);
			} else {
				mList.setVisibility(View.VISIBLE);
				mEmpty.setVisibility(View.INVISIBLE);
			}
			// Notifying adapter
			mListAdapter = new UpdateElementAdapter(getActivity(), apps);
			mList.setAdapter(mListAdapter);

			mListAdapter.notifyDataSetChanged();
		}

	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		mList.setOnItemClickListener(listener);
	}

	private class UpdateElementAdapter extends ArrayAdapter<AppItem> {
		private final Context context;
		private  ArrayList<AppItem> values= new ArrayList<AppItem>();

		public UpdateElementAdapter(Context context, ArrayList<AppItem> values) {
			super(context, R.layout.update_adapter, values);
			this.context = context;
			this.values = values;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			convertView = null;
			if(convertView==null){
				// Inflate View for ListItem
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.update_adapter, null);
				TileButton holder = new TileButton(convertView);
				// add Holder to View
				convertView.setTag(holder)	;		
				holder.setText(values.get(position).app.name);
				holder.setImage(values.get(position).app.icon);
				holder.setBackgroundColor(values.get(position).app.background);

				holder.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						//vuoi installare application
						updateDialogClickListener=  new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								switch (which){
								case DialogInterface.BUTTON_POSITIVE:
									//If yes is pressed download the new app

									downloadApplication(values.get(position).app.url, values.get(position).app.name);
									break;

								case DialogInterface.BUTTON_NEGATIVE:
									//If no is pressed add to the manual list applications update
									//Put the application in the blacklist in the SharedPreferences
									SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
									SharedPreferences.Editor editor = settings.edit();
									editor.putBoolean(values.get(position).app.name+"-update", false);
									editor.commit();
									//change the icon like the updated, notifica che e' cambiato
									notifyDataSetChanged();
									break;
								}
							}
						};
						AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
						builder.setMessage(getString(R.string.update_application_question)).setPositiveButton("Yes", updateDialogClickListener)
						.setNegativeButton("No", updateDialogClickListener).show();

					}
				});

			}
			return convertView;
		}
	}

	private void downloadApplication(String url, String name){
		if (ConnectionUtil.isConnected(mConnectivityManager)) {
			// Checking url
			if(!TextUtils.isEmpty(url)){
				if(mDownloaderTask != null && !mDownloaderTask.isCancelled()){
					mDownloaderTask.cancel(true);
				}
				mDownloaderTask = new ApkDownloaderTask(getActivity(), url);
				mDownloaderTask.execute();
			}else{
				Log.d(AppFragment.class.getName(), "Empty url for download: " + name);
				Toast.makeText(getActivity(), R.string.error_occurs,Toast.LENGTH_SHORT).show();
			}	class AppItem {
				SmartApp app;
				eu.trentorise.smartcampus.common.Status status = Status.NOT_FOUND;	
			}
		} else {
			Toast.makeText(getActivity(), R.string.enable_connection,Toast.LENGTH_SHORT).show();
			Intent intent = ConnectionUtil.getWifiSettingsIntent();
			startActivity(intent);
		}
	}







}
