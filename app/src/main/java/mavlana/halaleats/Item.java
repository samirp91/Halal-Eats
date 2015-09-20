package mavlana.halaleats;

import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by samir on 20/08/15.
 */
public interface Item {
    public boolean isClicked();
    public String getName();
    public String getName2();
    public int getViewType();
    public View getView(LayoutInflater inflater, View convertView);
}
