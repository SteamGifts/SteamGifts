package net.mabako.steamgifts.adapters.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
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

public class GiveawayListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final View itemContainer;
    private final TextView giveawayDetails;
    private final TextView giveawayName;
    private final TextView giveawayTime;
    private final ImageView giveawayImage;

    private final EndlessAdapter adapter;
    private final Activity activity;

    private final View indicatorWhitelist, indicatorGroup, indicatorLevelPositive, indicatorLevelNegative;

    private static int measuredHeight = 0;

    public GiveawayListItemViewHolder(View v, Activity activity, EndlessAdapter adapter) {
        super(v);
        itemContainer = v.findViewById(R.id.list_item);
        giveawayName = (TextView) v.findViewById(R.id.giveaway_name);
        giveawayDetails = (TextView) v.findViewById(R.id.giveaway_details);
        giveawayTime = (TextView) v.findViewById(R.id.time);
        giveawayImage = (ImageView) v.findViewById(R.id.giveaway_image);

        indicatorWhitelist = v.findViewById(R.id.giveaway_list_indicator_whitelist);
        indicatorGroup = v.findViewById(R.id.giveaway_list_indicator_group);
        indicatorLevelPositive = v.findViewById(R.id.giveaway_list_indicator_level_positive);
        indicatorLevelNegative = v.findViewById(R.id.giveaway_list_indicator_level_negative);

        this.activity = activity;
        this.adapter = adapter;

        v.setOnClickListener(this);
    }

    public void setFrom(Giveaway giveaway) {
        giveawayName.setText(giveaway.getTitle());
        giveawayTime.setText(giveaway.getTimeRemaining());

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

        if (giveaway.isEntered()) {
            int attrs[] = new int[]{R.attr.colorHighlightBackground};
            TypedArray ta = activity.getTheme().obtainStyledAttributes(attrs);

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                itemContainer.setBackgroundDrawable(ta.getDrawable(0));
            } else {
                itemContainer.setBackground(ta.getDrawable(0));
            }
        } else {
            itemContainer.setBackgroundResource(R.color.colorTransparent);
        }

        // Check all the indicators
        indicatorWhitelist.setVisibility(giveaway.isWhitelist() ? View.VISIBLE : View.GONE);
        indicatorGroup.setVisibility(giveaway.isGroup() ? View.VISIBLE : View.GONE);
        indicatorLevelPositive.setVisibility(giveaway.isLevelPositive() ? View.VISIBLE : View.GONE);
        indicatorLevelNegative.setVisibility(giveaway.isLevelNegative() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        Giveaway giveaway = (Giveaway) adapter.getItem(getAdapterPosition());

        GiveawayDetailFragment.setParent(activity);
        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, giveaway);

        activity.startActivityForResult(intent, MainActivity.REQUEST_LOGIN_PASSIVE);
    }
}
