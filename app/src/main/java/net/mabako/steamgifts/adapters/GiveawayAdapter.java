package net.mabako.steamgifts.adapters;

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

import java.util.List;

public class GiveawayAdapter extends ArrayAdapter<Giveaway> {
    private Activity activity;

    public GiveawayAdapter(Activity activity, int resource, int textViewResourceId,
                           List<Giveaway> giveaways) {
        super(activity, resource, textViewResourceId, giveaways);
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
        } else {
            // if holder created, get tag from view
            holder = (ViewHolder) convertView.getTag();
        }

        Giveaway giveaway = getItem(position);


        holder.giveawayName.setText(giveaway.getTitle());

        String str = giveaway.getPoints() + "P | " + giveaway.getEntries() + " entries";
        if (giveaway.getCopies() > 1)
            str = giveaway.getCopies() + " copies | " + str;
        holder.giveawayDetails.setText(str);

        Picasso.with(getContext()).load("http://cdn.akamai.steamstatic.com/steam/" + giveaway.getType().name().toLowerCase() + "s/" + giveaway.getGameId() + "/capsule_184x69.jpg").into(holder.giveawayImage);

        return convertView;
    }

    private static class ViewHolder {
        private final TextView giveawayDetails;
        private TextView giveawayName;
        private ImageView giveawayImage;

        public ViewHolder(View v) {
            giveawayName = (TextView) v.findViewById(R.id.giveaway_name);
            giveawayDetails = (TextView) v.findViewById(R.id.giveaway_details);
            giveawayImage = (ImageView) v.findViewById(R.id.giveaway_image);
        }
    }
}
