package app.ctrlyati.rollmenu;

public interface RollMenuListener {

	public void onChanging(int pos);
	public void onSelected(int pos);
	public void onBegin(int pos);
	public void onUpdate(int pos, int dt);
	
}
