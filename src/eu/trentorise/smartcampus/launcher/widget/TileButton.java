package eu.trentorise.smartcampus.launcher.widget;

import eu.trentorise.smartcampus.common.Status;
import eu.trentorise.smartcampus.launcher.R;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.opengl.Visibility;
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
	private ImageView mUpdate;





	public TileButton(View v) {
		mParentView = v;
		mImageView = (ImageView) v.findViewById(R.id.image);
		mTextView = (TextView) v.findViewById(R.id.text);
		mUpdate = (ImageView) v.findViewById(R.id.image_update);
	}
	

	public View getmParentView() {
		return mParentView;
	}

	public void setmParentView(View mParentView) {
		this.mParentView = mParentView;
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

	public void setmUpdate(ImageView mUpdate) {
		this.mUpdate = mUpdate;
	}
	
	public void setmUpdate(Drawable mUpdate) {
		this.mUpdate.setImageDrawable(mUpdate) ;
	}
	
	public void mUpdateVisible(boolean visible){
		if (visible)
			mUpdate.setVisibility(View.VISIBLE);
		else mUpdate.setVisibility(View.INVISIBLE);
	}

}
