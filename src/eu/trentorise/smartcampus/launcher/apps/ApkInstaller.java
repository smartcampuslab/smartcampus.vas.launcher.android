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
package eu.trentorise.smartcampus.launcher.apps;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import eu.trentorise.smartcampus.launcher.R;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.Toast;

/**
 * 
 * Utility that allows to build an intent that prompt to user permissions panel.
 * 
 * @author Simone Casagranda
 * 
 */
public class ApkInstaller {

	private static final String DATA_TYPE = "application/vnd.android.package-archive";
	private static final String FOLDER = Environment
			.getExternalStorageDirectory() + "/download/";
	private static final String FILE_EXT = ".apk";

	/**
	 * We have to ask to system certificate signed application for installation.
	 * PackageManager doesn't allow to call installPackage(...) because checks
	 * app UserID and certificate.
	 * 
	 * @param context
	 * @param file
	 */
	public static void promptInstall(Context context, Uri uri) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(uri, DATA_TYPE);
		context.startActivity(intent);
	}
	
	/**
	 * We ask to system to prompt user an un-installation form for that package
	 * @param context
	 * @param appPackage
	 */
	public static void promptUnInstall(Context context, String appPackage){
		Uri packageUri = Uri.parse(appPackage);
        Intent intent = new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri);
        context.startActivity(intent);

	}

	/**
	 * Retrieves an APK from a passed URL and try to store in the preferred
	 * folder.
	 * 
	 * @param urlLocation
	 * @param appLabel
	 * @return
	 */
	public static Uri retrieveApk(String urlLocation, String appLabel) {
		try {
			// Preparing folder and file
			File folder = new File(FOLDER);
			folder.mkdirs();
			File apkFile = new File(folder, appLabel + FILE_EXT);
			FileOutputStream fos = new FileOutputStream(apkFile);
			// Opening connection
			URL url = new URL(urlLocation);
			HttpURLConnection c = (HttpURLConnection) url.openConnection();
			c.setRequestMethod("GET");
			c.setDoOutput(true);
			c.connect();
			// Getting InputStream
			InputStream is = c.getInputStream();
			byte[] buffer = new byte[1024];
			int len1 = 0;
			while ((len1 = is.read(buffer)) != -1) {
				fos.write(buffer, 0, len1);
			}
			fos.close();
			is.close();
			// Return uri
			return Uri.fromFile(apkFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Very simple AsyncTask that in background downloads an APK and then on the
	 * UI thread launch an intent to prompt installation to user.
	 */
	public static class ApkDownloaderTask extends AsyncTask<Void, Void, Uri> {

		private String mAppUrl;
		private Context mContext;
		private ProgressDialog mProgressDialog;

		public ApkDownloaderTask(Context context, String appUrl) {
			mContext = context;
			mAppUrl = appUrl;
		}

		@Override
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(mContext, "",
					mContext.getString(R.string.getting_apk));
		}

		@Override
		protected Uri doInBackground(Void... params) {
			Uri uri = null;
			uri = retrieveApk(mAppUrl,
					mContext.getString(R.string.downloaded_app));
			return uri;
		}

		@Override
		protected void onPostExecute(Uri result) {
			mProgressDialog.dismiss();
			// Checking result
			if (result != null) {
				promptInstall(mContext, result);
			} else {
				Toast.makeText(mContext, "Error!", Toast.LENGTH_SHORT).show();
			}
		}
	}
}
