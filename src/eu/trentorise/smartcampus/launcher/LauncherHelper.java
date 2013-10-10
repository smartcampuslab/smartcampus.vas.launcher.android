package eu.trentorise.smartcampus.launcher;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;

public class LauncherHelper {
	private static String CLIENT_ID = null;
	private static String CLIENT_SECRET = null;
	
	public static void init(Context ctx){
	ApplicationInfo ai = null;
	try {
		ai = ctx.getPackageManager().getApplicationInfo(ctx.getPackageName(),
				PackageManager.GET_META_DATA);
	} catch (NameNotFoundException e) {

	}
	Bundle metaData = ai.metaData;
	if (metaData != null) {

		CLIENT_ID = (String) metaData.get("CLIENT_ID");
		CLIENT_SECRET = (String) metaData.get("CLIENT_SECRET");
	}
	}

	public static String getCLIENT_ID() {
		return CLIENT_ID;
	}

	public static void setCLIENT_ID(String cLIENT_ID) {
		CLIENT_ID = cLIENT_ID;
	}

	public static String getCLIENT_SECRET() {
		return CLIENT_SECRET;
	}

	public static void setCLIENT_SECRET(String cLIENT_SECRET) {
		CLIENT_SECRET = cLIENT_SECRET;
	}
}
