package app.ctrlyati.rollmenu;

import android.content.Context;
import android.view.View;

public class RollItem {

	private int mImageResourceId;
	private String mName;
	private Context mContext;
	
	public RollItem() {
		super();
	}

	public RollItem(int mImageResourceId, String mName, Context mContext) {
		super();
		this.mImageResourceId = mImageResourceId;
		this.mName = mName;
		this.mContext = mContext;
	}
	
}
