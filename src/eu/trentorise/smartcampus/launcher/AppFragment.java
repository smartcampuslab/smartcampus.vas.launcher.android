package eu.trentorise.smartcampus.launcher;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import eu.trentorise.smartcampus.common.AppInspector;
import eu.trentorise.smartcampus.common.LauncherException;
import eu.trentorise.smartcampus.common.Status;
import eu.trentorise.smartcampus.launcher.apps.ApkInstaller.ApkDownloaderTask;
import eu.trentorise.smartcampus.launcher.models.SmartApp;
import eu.trentorise.smartcampus.launcher.util.ConnectionUtil;
import eu.trentorise.smartcampus.launcher.widget.TileButton;

/**
 * 
 * Fragment that allows to page apps icon and manage user interactions.
 * 
 * @author Simone Casagranda
 * 
 */
public class AppFragment extends Fragment {
	
	private ConnectivityManager mConnectivityManager;
	private AppInspector mInspector;

	private GridView mGridView;	
	private AppAdapter mAdapter;
	private List<AppItem> mAppItems = new ArrayList<AppItem>();;
	
	private AppTask mAppTask;
	private ApkDownloaderTask mDownloaderTask;
	
	@Override
	public void onCreate(Bundle args) {
		super.onCreate(args);
		// Getting connectivity manager
		mConnectivityManager = ConnectionUtil.getConnectivityManager(getActivity());
		// Getting inspector
		mInspector = new AppInspector(getActivity());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,	Bundle args) {
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
	
	@Override
	public void onStart() {
		super.onStart();
		// Starting new task
		startNewAppTask();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		// Stopping any active task
		stopAnyActiveAppTask();
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
	
	// Task that retrieves applications info
	private class AppTask extends AsyncTask<Void, Void, List<AppItem>>{

		@Override
		protected List<AppItem> doInBackground(Void... params) {
			List<AppItem> items = new ArrayList<AppItem>();
			List<AppItem> notInstalledItems = new ArrayList<AppItem>();
			// Getting applications names, packages, ...
			String[] labels = getResources().getStringArray(R.array.app_labels);
			String[] packages = getResources().getStringArray(R.array.app_packages);
			String[] backgrounds = getResources().getStringArray(R.array.app_backgrounds);
			String[] urls = getResources().getStringArray(R.array.app_urls);
			TypedArray icons = getResources().obtainTypedArray(R.array.app_icons);
			TypedArray grayIcons = getResources().obtainTypedArray(R.array.app_gray_icons);
			// They have to be the same length
			assert labels.length == packages.length
					&& labels.length == backgrounds.length
					&& labels.length == urls.length
					&& labels.length == icons.length()
					&& labels.length == grayIcons.length();
			// Preparing all items
			for(int i=0;i<labels.length;i++){
				AppItem item = new AppItem();
				item.app = new SmartApp();
				item.app.fillApp(labels[i], packages[i], urls[i],
						icons.getDrawable(i), grayIcons.getDrawable(i),
						backgrounds[i]);
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
		
		@Override
		protected void onPostExecute(List<AppItem> result) {
			super.onPostExecute(result);
			// Clearing items list
			mAppItems.clear();
			// Checking result
			if(result!=null){
				mAppItems.addAll(result);
			}
			// Notifying adapter
			mAdapter.notifyDataSetChanged();
		}
		
	}

	// Array adapter for GridView
	private class AppAdapter extends ArrayAdapter<AppItem> {
		
		AppInspector mAppInspector = new AppInspector(getActivity());
		int mWidth, mHeight;

		public AppAdapter(List<AppItem> items) {
			super(getActivity(), R.layout.item_app_tile, items);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TileButton holder = null;
			if(convertView==null){
				// Inflate View for ListItem
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_app_tile, null);
				// Create Holder
				holder = new TileButton(convertView);
				// add Holder to View
				convertView.setTag(holder);
				// Calculating sizes
				if(mWidth<1||mHeight<1){
					Rect rectgle= new Rect();
					Window window= getActivity().getWindow();
					window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
					int statusBarHeight= rectgle.top;
					int contentViewTop= window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
					// Dimension
					Display display = getActivity().getWindowManager().getDefaultDisplay();
					// We are using android v8
					mWidth = Math.round(display.getWidth()/2f);
					mHeight = Math.round((display.getHeight()-statusBarHeight)/3f);					
				}
				// Setting sizes
				convertView.setMinimumWidth(mWidth);
				convertView.setMinimumHeight(mHeight);
			}else{
				holder = (TileButton)convertView.getTag();
			}
			// Getting item
			final AppItem item = getItem(position);
			holder.setText(item.app.name);
			// Checking status for colors			
			if(item.status == eu.trentorise.smartcampus.common.Status.OK){
				holder.setImage(item.app.icon);
				holder.setBackgroundColor(item.app.background);
				holder.setTextColor(Color.WHITE);				
			}else{
				holder.setImage(item.app.gray_icon);
				holder.setBackgroundColor(getResources().getColor(R.color.tile_background_unsel));
				holder.setTextColor(getResources().getColor(R.color.tile_text_unsel));				
			}
			// Setting application info name
			switch (item.status) {
			case OK:
				holder.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						try {
							mAppInspector.launchApp(item.app.appPackage, getString(R.string.smartcampus_action_start), null, null);
						} catch (LauncherException e) {
							e.printStackTrace();
						}
					}
				});
				break;
			case NOT_FOUND:
				holder.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						if (ConnectionUtil.isConnected(mConnectivityManager)) {
							// Checking url
							if(!TextUtils.isEmpty(item.app.url)){
								if(mDownloaderTask != null && !mDownloaderTask.isCancelled()){
									mDownloaderTask.cancel(true);
								}
								mDownloaderTask = new ApkDownloaderTask(getActivity(), item.app.url);
								mDownloaderTask.execute();
							}else{
								Log.d(AppFragment.class.getName(), "Empty url for download: " + item.app.name);
								Toast.makeText(getActivity(), R.string.error_occurs,Toast.LENGTH_SHORT).show();
							}
						} else {
							Toast.makeText(getActivity(), R.string.enable_connection,Toast.LENGTH_SHORT).show();
							Intent intent = ConnectionUtil.getWifiSettingsIntent();
							startActivity(intent);
						}
					}
				});
				break;
			case NOT_VALID_UID:
				holder.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						// Asking user to remove application
						Toast.makeText(getActivity(), R.string.not_secure_app,Toast.LENGTH_SHORT).show();
					}
				});
				break;
			default:
				// Others haven't any importance
				holder.setOnClickListener(null);
				break;
			}
			// Returning just filled view
			return convertView;
		}

	}

	// Item wrapper of a smartApp
	class AppItem {
		SmartApp app;
		eu.trentorise.smartcampus.common.Status status = Status.NOT_FOUND;	
	}
}
