package mavlana.halaleats;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by samir on 20/08/15.
 */
public class ListItem implements Item {
    public final String         str1;
    public final String         str2;
    private boolean clicked;

    public ListItem(String text1, String text2) {
        this.str1 = text1;
        this.str2 = text2;
        this.clicked = false;
    }

    @Override
    public int getViewType() {
        return TwoTextArrayAdapter.RowType.LIST_ITEM.ordinal();
    }

    public boolean isClicked(){
        return this.clicked;
    }

    @Override
    public String getName() {
        return this.str1;
    }

    @Override
    public String getName2() {
        return this.str2;
    }


    public void toggleClicked(){
        if (isClicked()){
            this.clicked = false;
        }
        else{
            this.clicked = true;
        }
    }

    @Override
    public View getView(LayoutInflater inflater, View convertView) {
        View view;
        if (convertView == null) {
            view = (View) inflater.inflate(R.layout.my_list_item, null);
            // Do some initialization
        } else {
            view = convertView;
        }

        TextView text1 = (TextView) view.findViewById(R.id.list_content1);
        //TextView text2 = (TextView) view.findViewById(R.id.list_content2);
        text1.setText(str1);
        //text2.setText(str2);

        return view;
    }

}