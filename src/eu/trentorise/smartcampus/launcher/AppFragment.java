package eu.trentorise.smartcampus.launcher;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.R.drawable;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
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
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

import eu.trentorise.smartcampus.R;
import eu.trentorise.smartcampus.ac.embedded.EmbeddedSCAccessProvider;
import eu.trentorise.smartcampus.common.AppInspector;
import eu.trentorise.smartcampus.common.LauncherException;
import eu.trentorise.smartcampus.common.Status;
import eu.trentorise.smartcampus.launcher.apps.ApkInstaller.ApkDownloaderTask;
import eu.trentorise.smartcampus.launcher.models.SmartApp;
import eu.trentorise.smartcampus.launcher.models.UpdateModel;
import eu.trentorise.smartcampus.launcher.util.ConnectionUtil;
import eu.trentorise.smartcampus.launcher.widget.TileButton;
import eu.trentorise.smartcampus.protocolcarrier.ProtocolCarrier;
import eu.trentorise.smartcampus.protocolcarrier.common.Constants.Method;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageRequest;
import eu.trentorise.smartcampus.protocolcarrier.custom.MessageResponse;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ConnectionException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.ProtocolException;
import eu.trentorise.smartcampus.protocolcarrier.exceptions.SecurityException;

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

	private GridView mGridView;	
	private AppAdapter mAdapter;
	private List<AppItem> mAppItems = new ArrayList<AppItem>();;
	private int heightActionBar =0;
	private AppTask mAppTask;
	private ApkDownloaderTask mDownloaderTask;
	public static final String PREFS_NAME = "LauncherPreferences";
	private static final String UPDATE = "_updateModel";
	private static final String UPDATE_ADDRESS = "/download/VAS/update.conf";
	private static final String UPDATE_HOST = "smartcampus.trentorise.eu";
	private static final String LAUNCHER = "SmartLAuncher";
	private Drawable ic_update;

	
	@Override
	public void onCreate(Bundle args) {
		super.onCreate(args);
		// Getting connectivity manager
		mConnectivityManager = ConnectionUtil.getConnectivityManager(getActivity());
		// Getting inspector
		mInspector = new AppInspector(getActivity());	
		// Asking for an option menu
		setHasOptionsMenu(true);
		
		//if you have some proitem.app.nameblem with the stored data,  uncomment these lines and the data are erased
/*		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
    	SharedPreferences.Editor editor = settings.edit();
		editor.clear();
        editor.commit();	*/
        
        
        
        ic_update =getResources().getDrawable(R.drawable.ic_app_update);

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

	private int[] readUpdateVersions(String[] packageNames, int[] defaultVersions) {
		SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
		int[] res = defaultVersions; 
		UpdateModel update = null;
		long nextUpdate = -1;
		if (settings != null && settings.contains(UPDATE)) {
			String json = settings.getString(UPDATE, null);
			if (json != null) {
				try {
					update = new UpdateModel(json);
					nextUpdate = update.getNextUpdate();
				} catch (JSONException e) {
					Log.e(AppFragment.class.getName(), "Failed to parse update model: "+e.getMessage());
				}
			}
		}
		if (update == null || nextUpdate < System.currentTimeMillis()) {
			MessageRequest req = new MessageRequest(UPDATE_HOST, UPDATE_ADDRESS);
			req.setMethod(Method.GET);
			ProtocolCarrier pc = new ProtocolCarrier(getActivity(), LAUNCHER);
			try {
				MessageResponse mres = pc.invokeSync(req, LAUNCHER, new EmbeddedSCAccessProvider().readToken(getActivity(), null));
				if (mres != null && mres.getBody() != null) {
					update = new UpdateModel(mres.getBody());
					settings.edit().putString(UPDATE, mres.getBody()).commit();
					for (int i = 0; i < packageNames.length; i++) {
						Integer version = update.getVersion(packageNames[i]);
						res[i] = version == null ? 0 : version;
					}
				}
			} catch (Exception e) {
				Log.e(AppFragment.class.getName(),"Error reading update config: "+e.getMessage());
			}
		}
		
		return res;
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
			int[] versions = getResources().getIntArray(R.array.app_version);

//			versions = readUpdateVersions(packages, versions);

			Drawable ic_update =getResources().getDrawable(R.drawable.ic_app_update);

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
					if (!mInspector.isAppUpdated(item.app.appPackage, versions[i]))
						item.status=eu.trentorise.smartcampus.common.Status.NOT_UPDATED;
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
					//Installed but updated
					items.add(item);
					//actually is the same of OK
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
		DialogInterface.OnClickListener updateDialogClickListener;

		public AppAdapter(List<AppItem> items) {
			super(getActivity(), R.layout.item_app_tile, items);
			
			//declare the listener for the update dialogBox

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
				if (heightActionBar<1)
					heightActionBar=((SherlockFragmentActivity) getActivity()).getSupportActionBar().getHeight();
				// Calculating sizes
				
				//Sometimes it's called and it's bigger than the screen (maybe without the action bar??).
				//So actually it's called every time it's reloaded
				
				//if(mWidth<1||mHeight<1){
					Rect rectgle= new Rect();
					Window window= getActivity().getWindow();
					window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
					int statusBarHeight= rectgle.top;
					int contentViewTop= window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
					// Dimension
					Display display = getActivity().getWindowManager().getDefaultDisplay();
					// We are using android v8
					mWidth = Math.round(display.getWidth()/2f);
					mHeight = Math.round(((display.getHeight()-heightActionBar)-statusBarHeight)/3f);					
				//}
					
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
				holder.mUpdateVisible(false);

			}
			else{
				holder.setImage(item.app.gray_icon);
				holder.setBackgroundColor(getResources().getColor(R.color.tile_background_unsel));
				holder.setTextColor(getResources().getColor(R.color.tile_text_unsel));				
			}
			
			if(item.status == eu.trentorise.smartcampus.common.Status.NOT_UPDATED)// e non e' nella blacklist;l
				{
				 SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
				 settings.toString();
			     boolean autoupdate = settings.getBoolean(item.app.name+"-update", true);
				if (autoupdate){
				//if update is not set (true) and is not updated
				holder.setImage(item.app.icon);
				holder.setBackgroundColor(item.app.background);
				holder.setTextColor(Color.WHITE);
				holder.setmUpdate(ic_update);
				holder.mUpdateVisible(true);
				} 
				else{
					//if it is set to false is manual 
					holder.setImage(item.app.icon);
					holder.setBackgroundColor(item.app.background);
					holder.setTextColor(Color.WHITE);
					holder.mUpdateVisible(false);
					item.status=Status.OK;
				}
				

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
				
			case NOT_UPDATED:
				holder.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						//dialog box per fare update
						updateDialogClickListener=  new DialogInterface.OnClickListener() {
						    @Override
						    public void onClick(DialogInterface dialog, int which) {
						        switch (which){
						        case DialogInterface.BUTTON_POSITIVE:
						            //If yes is pressed download the new app

									downloadApplication(item.app.url, item.app.name);
						            break;

						        case DialogInterface.BUTTON_NEGATIVE:
						        	//If no is pressed add to the manual list applications update
						        	//Put the application in the blacklist in the SharedPreferences
						        	SharedPreferences settings = getActivity().getSharedPreferences(PREFS_NAME, 0);
						        	SharedPreferences.Editor editor = settings.edit();
						        	if (settings.getBoolean(item.app.name+"-update", true))
						            {
						        		editor.putBoolean(item.app.name+"-update", false);
							            editor.commit();

						            }
						        	Toast.makeText(getContext(), getString(R.string.update_application_manual_list), Toast.LENGTH_SHORT).show();
						        	//change the icon like the updated, notifica che e' cambiato
						        	mAdapter.notifyDataSetChanged();
						            break;
						        }
						    }
						};
						AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
						builder.setMessage(getString(R.string.update_application_question)).setPositiveButton("Yes", updateDialogClickListener)
						    .setNegativeButton("No", updateDialogClickListener).show();

					}
				});
				break;
				
			case NOT_FOUND:
				holder.setOnClickListener(new OnClickListener() {					
					@Override
					public void onClick(View v) {
						downloadApplication(item.app.url, item.app.name);
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
				}
			} else {
				Toast.makeText(getActivity(), R.string.enable_connection,Toast.LENGTH_SHORT).show();
				Intent intent = ConnectionUtil.getWifiSettingsIntent();
				startActivity(intent);
			}
		}

	}
	
/*	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// TODO Auto-generated method stub
		super.onCreateOptionsMenu(menu, inflater);
	     inflater.inflate(R.menu.update_menu, menu);

	}*/
	
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		getSherlockActivity().getSupportMenuInflater().inflate(R.menu.gripmenu, menu);
		SubMenu submenu = menu.getItem(0).getSubMenu();
		submenu.clear();
		submenu.setIcon(R.drawable.ic_action_overflow);
		submenu.add(Menu.CATEGORY_SYSTEM, R.id.update_option_list, Menu.NONE,
				R.string.update_option_list).setEnabled(availableUpdate()); //enable or disable the option menu
		
		
	}

	/*check if there are update or nor*/
	
	private boolean availableUpdate() {
		//se ci sono available update return true
		//altrimenti false
		return true;
}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	        case R.id.update_option_list:

      	
	            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
	            Fragment newFragment = new ManualUpdateFragment();
	            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
	            transaction.replace(R.id.fragment_container, newFragment);
	            transaction.addToBackStack(null);
	            transaction.commit();
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
