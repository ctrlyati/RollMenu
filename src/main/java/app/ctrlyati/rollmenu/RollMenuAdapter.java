package app.ctrlyati.rollmenu;

import android.view.View;
import android.view.ViewGroup;

public interface RollMenuAdapter{
	
	public int getCount();
	public View getView(int position,View view, ViewGroup parent);
	
}
