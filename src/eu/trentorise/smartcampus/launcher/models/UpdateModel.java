package eu.trentorise.smartcampus.launcher.models;

import java.text.SimpleDateFormat;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

public class UpdateModel {

	private JSONObject object;
	
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy"); 
	
	public UpdateModel() {
		super();
	}
	
	public UpdateModel(String json) throws JSONException {
		object = new JSONObject(json);
	}

	public long getNextUpdate() {
		try {
			String date = object.getString("nextUpdate");
			return sdf.parse(date).getTime();
		} catch (Exception e) {
			return -1;
		}
	}
	@SuppressWarnings("unchecked")
	public Iterator<String> getKeys() {
		try {
			return object.getJSONObject("versions").keys();
		} catch (JSONException e) {
			return null;
		}
	}
	public Integer getVersion(String key) {
		try {
			return object.getJSONObject("versions").getInt(key);
		} catch (JSONException e) {
			return null;
		}
	}
	
}
