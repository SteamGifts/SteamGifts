package net.mabako.steamgifts.adapters;

import java.util.List;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Giveaway;

public class GiveawayAdapter extends ArrayAdapter<Giveaway> {
    private Activity activity;

    public GiveawayAdapter(Activity activity, int resource, int textViewResourceId,
                          List<Giveaway> countries) {
        super(activity, resource, textViewResourceId, countries);
        this.activity = activity;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder = null;
        LayoutInflater inflater =
                (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        // If holder not exist then locate all view from UI file.
        if (convertView == null) {
            // inflate UI from XML file
            convertView = inflater.inflate(R.layout.item_listview, parent, false);
            // get all UI view
            holder = new ViewHolder(convertView);
            // set tag for holder
            convertView.setTag(holder);
        }  else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        Giveaway giveaway = getItem(position);


        String title = giveaway.getTitle();
        if(giveaway.getCopies() > 1)
            title = giveaway.getCopies() + "x " + title;
        holder.giveawayName.setText(title);

        holder.giveawayDetails.setText(giveaway.getPoints() + "P | " + giveaway.getEntries() + " entries");
        Picasso.with(getContext()).load("http://cdn.akamai.steamstatic.com/steam/apps/" + giveaway.getGameId() + "/capsule_184x69.jpg").into(holder.giveawayImage);

        return convertView;
    }

    private static class ViewHolder {
        private TextView giveawayName;
        private final TextView giveawayDetails;
        private ImageView giveawayImage;

        public ViewHolder(View v) {
            giveawayName = (TextView) v.findViewById(R.id.giveaway_name);
            giveawayDetails = (TextView) v.findViewById(R.id.giveaway_details);
            giveawayImage = (ImageView) v.findViewById(R.id.giveaway_image);
        }
    }
}
