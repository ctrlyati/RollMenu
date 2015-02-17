package app.ctrlyati.rollmenu;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ArrayRollMenuAdapter implements RollMenuAdapter{

	private List<RollItem> mItems;
	private Context mContext;
	private int mLayoutId;
	
	public ArrayRollMenuAdapter(Context context, List<RollItem> items, int layout_id){
		this.mItems = items;
		this.mContext = context;
		this.mLayoutId = layout_id;
	}
	
	public int getCount(){
		return mItems.size();
	}

	@Override
	public View getView(int position,View view, ViewGroup parent) {
		
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(mLayoutId , parent, false);
		}
		
		return view;
	}
}
