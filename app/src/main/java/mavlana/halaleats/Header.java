package mavlana.halaleats;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by samir on 20/08/15.
 */
public class Header implements Item {
    private final String         name;

    public Header(String name) {
        this.name = name;
    }

    public boolean isClicked(){
        return false;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getName2() {
        return "null";
    }

    @Override
    public int getViewType() {
        return TwoTextArrayAdapter.RowType.HEADER_ITEM.ordinal();
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View view;
        if (convertView == null) {
            view = (View) inflater.inflate(R.layout.header, null);
            // Do some initialization
        } else {
            view = convertView;
        }

        TextView text = (TextView) view.findViewById(R.id.separator);
        text.setText(this.name);

        return view;
    }

}