package eu.trentorise.smartcampus.launcher.widget;

import eu.trentorise.smartcampus.launcher.R;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Class that works as a button with a top Drawable but allows to have a better
 * pagination of views.
 * 
 * @author Simone Casagranda
 * 
 */
public class TileButton {
	
	private View mParentView;
	private ImageView mImageView;
	private TextView mTextView;

	public TileButton(View v) {
		mParentView = v;
		mImageView = (ImageView) v.findViewById(R.id.image);
		mTextView = (TextView) v.findViewById(R.id.text);
	}
	
	public void setOnClickListener(OnClickListener listener){
		mParentView.setOnClickListener(listener);
	}
	
	public void setImage(int imageRes){
		mImageView.setImageResource(imageRes);
	}
	
	public void setImage(Drawable drawable){
		mImageView.setImageDrawable(drawable);
	}
	
	public void setText(String text){
		mTextView.setText(text);
	}
	
	public void setText(int text){
		mTextView.setText(text);
	}
	
	public CharSequence getText(){
		return mTextView.getText();
	}
	
	public void setBackgroundColor(String color){
		mParentView.setBackgroundColor(Color.parseColor(color));
	}

	public void setBackgroundColor(int color){
		mParentView.setBackgroundColor(color);
	}
	
	public void setTextColor(int color){
		mTextView.setTextColor(color);
	}
}
