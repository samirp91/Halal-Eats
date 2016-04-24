package mavlana.halaleats;

import android.view.LayoutInflater;
import android.view.View;

/**
 * Created by samir on 20/08/15.
 */
public interface Item {
    boolean isClicked();
    String getName();
    String getName2();
    int getViewType();
    View getView(LayoutInflater inflater, View convertView);
}
