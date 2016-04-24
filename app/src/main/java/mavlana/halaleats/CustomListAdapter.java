package mavlana.halaleats;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by samir on 19/08/15.
 */
public class CustomListAdapter extends ArrayAdapter {

    private Context mContext;
    private int id;
    private List <RestaurantInfo>items ;
    private Typeface tf;

    public CustomListAdapter(Context context, int textViewResourceId , List<RestaurantInfo> list, String FONT )
    {
        super(context, textViewResourceId, list);
        mContext = context;
        id = textViewResourceId;
        items = list ;
        tf = Typeface.createFromAsset(context.getAssets(), FONT);
    }

    @Override
    public View getView(int position, View v, ViewGroup parent)
    {
        View mView = v ;
        if(mView == null){
            LayoutInflater vi = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mView = vi.inflate(id, null);
        }

        TextView text = (TextView) mView.findViewById(R.id.textView);

        if(items.get(position) != null )
        {
            RestaurantInfo r = (RestaurantInfo) items.get(position);
            final SpannableStringBuilder sb = new SpannableStringBuilder(r.toString());
            final ForegroundColorSpan fcs = new ForegroundColorSpan(Color.CYAN);
            final ForegroundColorSpan gcs = new ForegroundColorSpan(Color.GREEN);
            final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
            final StyleSpan iss = new StyleSpan(Typeface.ITALIC);
            sb.setSpan(fcs, 0, r.toString().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(bss, 0, r.getName().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            sb.setSpan(iss, r.getName().length() + r.priceTitle().length() + 3, r.getName().length() + r.priceTitle().length() + 5 + r.getCuisine().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
            sb.setSpan(new AbsoluteSizeSpan(33), r.getName().length() + r.priceTitle().length() + 3, r.getName().length() + r.priceTitle().length() + 5 + r.getCuisine().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE );
            sb.setSpan(gcs, r.toString().length()-r.timeToString().length(),r.toString().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setText(sb);
            text.setBackgroundColor(Color.DKGRAY);
            //int color = Color.argb(200, 255, 64, 64);
            //text.setBackgroundColor( color );

        }

        return mView;
    }
}
