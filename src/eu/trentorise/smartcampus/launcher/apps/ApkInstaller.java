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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

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

}
