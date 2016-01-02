package net.mabako.steamgifts.adapters.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.MainActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;

/**
 * Created by mabako on 01.01.2016.
 */
public class GiveawayListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final View itemContainer;
    private final TextView giveawayDetails;
    private final TextView giveawayName;
    private final ImageView giveawayImage;

    private final EndlessAdapter adapter;
    private final Activity activity;

    private static int measuredHeight = 0;

    public GiveawayListItemViewHolder(View v, Activity activity, EndlessAdapter adapter) {
        super(v);
        itemContainer = v.findViewById(R.id.list_item);
        giveawayName = (TextView) v.findViewById(R.id.giveaway_name);
        giveawayDetails = (TextView) v.findViewById(R.id.giveaway_details);
        giveawayImage = (ImageView) v.findViewById(R.id.giveaway_image);

        this.activity = activity;
        this.adapter = adapter;

        v.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Giveaway giveaway = (Giveaway) adapter.getItem(getAdapterPosition());

        GiveawayDetailFragment.setParent(activity);
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, giveaway);

        activity.startActivityForResult(intent, MainActivity.REQUEST_LOGIN_PASSIVE);
    }

    public void setFrom(Giveaway giveaway) {

        giveawayName.setText(giveaway.getTitle());

        String str = giveaway.getPoints() + "P | " + giveaway.getEntries() + " entries";
        if (giveaway.getCopies() > 1)
            str = giveaway.getCopies() + " copies | " + str;
        giveawayDetails.setText(str);

        // giveaway_image
        Picasso.with(activity).load("http://cdn.akamai.steamstatic.com/steam/" + giveaway.getType().name().toLowerCase() + "s/" + giveaway.getGameId() + "/capsule_184x69.jpg").into(giveawayImage, new Callback() {
            /**
             * We manually set the height of this image to fit the container.
             */
            @Override
            public void onSuccess() {
                if (measuredHeight <= 0)
                    measuredHeight = itemContainer.getMeasuredHeight();

                ViewGroup.LayoutParams params = giveawayImage.getLayoutParams();
                params.height = measuredHeight;
            }

            @Override
            public void onError() {

            }
        });

        itemContainer.setBackgroundResource(giveaway.isEntered() ? R.color.md_blue_50 : android.R.color.background_light);
    }
}
