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
		first.setGravity(Gravity.CENTER_HORIZONTAL);
		
		TextView second = (TextView) v.findViewById(R.id.textView2);
		second.setText(Html.fromHtml(getString(R.string.about_1)));
		second.setMovementMethod(LinkMovementMethod.getInstance());
		second.setGravity(Gravity.CENTER_HORIZONTAL);

		TextView third = (TextView) v.findViewById(R.id.textView3);
		third.setText(Html.fromHtml(getString(R.string.about_2)));
		third.setMovementMethod(LinkMovementMethod.getInstance());
		third.setGravity(Gravity.CENTER_HORIZONTAL);
		return v;
	}


}
