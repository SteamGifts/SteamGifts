package net.mabako.steamgifts.adapters.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.MainActivity;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.web.SteamGiftsUserData;

public class GiveawayListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
    private static final String TAG = GiveawayListItemViewHolder.class.getSimpleName();

    private final View itemContainer;
    private final TextView giveawayDetails;
    private final TextView giveawayName;
    private final TextView giveawayTime;
    private final ImageView giveawayImage;

    private final GiveawayAdapter adapter;
    private final Activity activity;
    private final IHasEnterableGiveaways fragment;

    private final View indicatorWhitelist, indicatorGroup, indicatorLevelPositive, indicatorLevelNegative, indicatorPrivate;

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
        indicatorPrivate = v.findViewById(R.id.giveaway_list_indicator_private);

        this.activity = activity;
        this.fragment = fragment;
        this.adapter = adapter;

        v.setOnClickListener(this);
        v.setOnCreateContextMenuListener(this);
    }

    public void setFrom(Giveaway giveaway) {
        giveawayName.setText(giveaway.getTitle());
        giveawayTime.setText(giveaway.getTimeRemaining());

        StringBuilder sb = new StringBuilder();
        if (giveaway.getCopies() > 1)
            sb.append(giveaway.getCopies()).append(" copies | ");
        sb.append(giveaway.getPoints()).append("P | ");
        if (giveaway.getLevel() > 0)
            sb.append("L").append(giveaway.getLevel()).append(" | ");
        sb.append(giveaway.getEntries()).append(" entries");

        giveawayDetails.setText(sb);

        // giveaway_image
        if (giveaway.getGameId() != Game.NO_APP_ID) {
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
        } else {
            giveawayImage.setImageResource(android.R.color.transparent);
        }

        Utils.setBackgroundDrawable(activity, itemContainer, giveaway.isEntered());

        // Check all the indicators
        indicatorWhitelist.setVisibility(giveaway.isWhitelist() ? View.VISIBLE : View.GONE);
        indicatorGroup.setVisibility(giveaway.isGroup() ? View.VISIBLE : View.GONE);
        indicatorLevelPositive.setVisibility(giveaway.isLevelPositive() ? View.VISIBLE : View.GONE);
        indicatorLevelNegative.setVisibility(giveaway.isLevelNegative() ? View.VISIBLE : View.GONE);
        indicatorPrivate.setVisibility(giveaway.isPrivate() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        Giveaway giveaway = (Giveaway) adapter.getItem(getAdapterPosition());
        if (giveaway.getGiveawayId() != null && giveaway.getName() != null) {
            Intent intent = new Intent(activity, DetailActivity.class);
            intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, giveaway);

            activity.startActivityForResult(intent, MainActivity.REQUEST_LOGIN_PASSIVE);
        } else {
            Toast.makeText(activity, R.string.private_giveaway, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        // Are we logged in & do we have a token to submit with our "form"?
        if (SteamGiftsUserData.getCurrent().isLoggedIn() && adapter.getXsrfToken() != null) {
            // Which giveaway is this even for?
            final Giveaway giveaway = (Giveaway) adapter.getItem(getAdapterPosition());

            // We only know this giveaway exists, not the link to it.
            if (giveaway.getGiveawayId() == null || giveaway.getName() == null || !giveaway.isOpen())
                return;

            // Header
            menu.setHeaderTitle(giveaway.getTitle());

            if (giveaway.isEntered()) {
                menu.add(Menu.NONE, 1, Menu.NONE, String.format(activity.getString(R.string.leave_giveaway_with_points), giveaway.getPoints())).setOnMenuItemClickListener(this);
            } else {
                menu.add(Menu.NONE, 2, Menu.NONE, String.format(activity.getString(R.string.enter_giveaway_with_points), giveaway.getPoints())).setOnMenuItemClickListener(this).setEnabled(giveaway.getPoints() <= SteamGiftsUserData.getCurrent().getPoints() && giveaway.getLevel() <= SteamGiftsUserData.getCurrent().getLevel() && !SteamGiftsUserData.getCurrent().getName().equals(giveaway.getCreator()));
            }

            if (giveaway.getInternalGameId() > 0 && fragment instanceof GiveawayListFragment) {
                menu.add(Menu.NONE, 3, Menu.NONE, R.string.hide_game).setOnMenuItemClickListener(this);
            }
        } else {
            Log.d(TAG, "Not showing context menu for giveaway. (xsrf-token: " + adapter.getXsrfToken() + ")");
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        Giveaway giveaway = (Giveaway) adapter.getItem(getAdapterPosition());
        switch (item.getItemId()) {
            case 1:
                fragment.requestEnterLeave(giveaway.getGiveawayId(), GiveawayDetailFragment.ENTRY_DELETE, adapter.getXsrfToken());
                return true;
            case 2:
                fragment.requestEnterLeave(giveaway.getGiveawayId(), GiveawayDetailFragment.ENTRY_INSERT, adapter.getXsrfToken());
                return true;
            case 3:
                ((GiveawayListFragment) fragment).requestHideGame(giveaway.getInternalGameId());
                return true;
        }
        return false;
    }
}
