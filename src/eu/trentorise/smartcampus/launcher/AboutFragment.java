package eu.trentorise.smartcampus.launcher;

import it.smartcampuslab.launcher.R;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class AboutFragment extends SherlockFragment {

	@Override
	public void onCreate(Bundle arg0) {
		super.onCreate(arg0);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle args) {
		View v = inflater.inflate(R.layout.about, null, false);
		TextView first = (TextView) v.findViewById(R.id.textView1);
		first.setText(Html.fromHtml(getString(R.string.about_0)));
		
		TextView second = (TextView) v.findViewById(R.id.textView2);
		second.setText(Html.fromHtml(getString(R.string.about_1)));
		second.setMovementMethod(LinkMovementMethod.getInstance());

		return v;
	}


}
