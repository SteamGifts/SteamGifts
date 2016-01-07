package net.mabako.steamgifts.adapters.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.MainActivity;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.IHasEnterableGiveaways;
import net.mabako.steamgifts.web.WebUserData;

public class GiveawayListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
    private static final String TAG = GiveawayListItemViewHolder.class.getSimpleName();

    private final View itemContainer;
    private final TextView giveawayDetails;
    private final TextView giveawayName;
    private final TextView giveawayTime;
    private final ImageView giveawayImage;

    private final GiveawayAdapter adapter;
    private final Activity activity;
    private final IHasEnterableGiveaways fragment;

    private final View indicatorWhitelist, indicatorGroup, indicatorLevelPositive, indicatorLevelNegative;

    private static int measuredHeight = 0;

    public GiveawayListItemViewHolder(View v, Activity activity, GiveawayAdapter adapter, IHasEnterableGiveaways fragment) {
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
        this.fragment = fragment;
        this.adapter = adapter;

        v.setOnClickListener(this);
        v.setOnCreateContextMenuListener(this);
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

        Utils.setBackgroundDrawable(activity, itemContainer, giveaway.isEntered());

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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // Are we logged in & do we have a token to submit with our "form"?
        if (WebUserData.getCurrent().isLoggedIn() && adapter.getXsrfToken() != null) {
            // Which giveaway is this even for?
            final Giveaway giveaway = (Giveaway) adapter.getItem(getAdapterPosition());

            // Header
            menu.setHeaderTitle(giveaway.getTitle());

            if (giveaway.isEntered()) {
                menu.add(0, 1, 0, String.format(activity.getString(R.string.leave_giveaway_with_points), giveaway.getPoints())).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // We're already in the giveaway, so leave it.
                        fragment.requestEnterLeave(giveaway.getGiveawayId(), GiveawayDetailFragment.ENTRY_DELETE, adapter.getXsrfToken());
                        return true;
                    }
                });
            } else {
                menu.add(0, 2, 0, String.format(activity.getString(R.string.enter_giveaway_with_points), giveaway.getPoints())).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        // We're not in the giveaway, enter it.
                        fragment.requestEnterLeave(giveaway.getGiveawayId(), GiveawayDetailFragment.ENTRY_INSERT, adapter.getXsrfToken());
                        return true;
                    }
                    // - Should have enough points
                    // - should be a high enough level (We don't generally see group/whitelist giveaways on the list of all giveaways, where this is displayed)
                    // - should not be created by the current user
                }).setEnabled(giveaway.getPoints() <= WebUserData.getCurrent().getPoints() && giveaway.getLevel() <= WebUserData.getCurrent().getLevel() && !WebUserData.getCurrent().getName().equals(giveaway.getCreator()));
            }
        } else {
            Log.d(TAG, "Not showing context menu for giveaway. (xsrf-token: " + adapter.getXsrfToken() + ")");
        }
    }
}
