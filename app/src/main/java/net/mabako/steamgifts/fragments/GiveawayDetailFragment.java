package net.mabako.steamgifts.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.CommentAdapter;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.GiveawayExtras;
import net.mabako.steamgifts.fragments.util.GiveawayDetailsCard;
import net.mabako.steamgifts.tasks.EnterLeaveGiveawayTask;
import net.mabako.steamgifts.tasks.LoadGiveawayDetailsTask;

import java.util.ArrayList;

public class GiveawayDetailFragment extends Fragment {
    public static final String ARG_GIVEAWAY = "giveaway";
    public static final String ENTRY_INSERT = "entry_insert";
    public static final String ENTRY_DELETE = "entry_delete";
    private static final String TAG = GiveawayDetailFragment.class.getSimpleName();
    private static Activity parent;
    /**
     * Content to show for the giveaway details.
     */
    private Giveaway giveaway;
    private GiveawayDetailsCard giveawayCard;
    private LoadGiveawayDetailsTask task;
    private EnterLeaveGiveawayTask enterLeaveTask;
    private RecyclerView listView;
    private CommentAdapter adapter;

    public static Fragment newInstance(Giveaway giveaway) {
        GiveawayDetailFragment fragment = new GiveawayDetailFragment();
        fragment.giveaway = giveaway;
        return fragment;
    }

    public static void setParent(Activity parent) {
        // TODO better way of using this?
        GiveawayDetailFragment.parent = parent;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_giveaway_detail, container, false);

        final Activity activity = getActivity();
        final CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        appBarLayout.setTitle(giveaway.getTitle());

        ImageView toolbarImage = (ImageView) activity.findViewById(R.id.toolbar_image);
        if (toolbarImage != null) {
            Picasso.with(getContext()).load("http://cdn.akamai.steamstatic.com/steam/" + giveaway.getType().name().toLowerCase() + "s/" + giveaway.getGameId() + "/header.jpg").into(toolbarImage, new Callback() {
                @Override
                public void onSuccess() {
                    appBarLayout.setExpandedTitleTextAppearance(R.style.TransparentText);
                }

                @Override
                public void onError() {

                }
            });
        }

        listView = (RecyclerView) layout.findViewById(R.id.list);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CommentAdapter(this, listView, new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                Log.v(TAG, "Load more items...");
                fetchItems(page);
            }
        });
        listView.setAdapter(adapter);

        // Add the cardview for the Giveaway details
        giveawayCard = new GiveawayDetailsCard(giveaway);
        adapter.setStickyItem(giveawayCard);

        fetchItems(1);
        setHasOptionsMenu(true);

        return layout;
    }

    public void reload() {
        fetchItems(1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        task.cancel(true);

        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);
    }

    private void fetchItems(int page) {
        Log.d(TAG, "Fetching giveaways on page " + page + " for giveaway " + giveaway.getGiveawayId());

        if (task != null)
            task.cancel(true);

        task = new LoadGiveawayDetailsTask(this, giveaway.getGiveawayId() + "/" + giveaway.getName(), page);
        task.execute();
    }

    public void addDetails(GiveawayExtras extras, int page) {
        giveaway.setTimeRemaining(extras.getTimeRemaining());
        giveawayCard.setExtras(extras);
        adapter.setStickyItem(giveawayCard);

        if (page == 1)
            adapter.clear();
        adapter.finishLoading(new ArrayList<IEndlessAdaptable>(extras.getComments()));
    }

    public void requestEnterLeave(String enterOrDelete) {
        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);

        enterLeaveTask = new EnterLeaveGiveawayTask(this, giveaway.getGiveawayId(), giveawayCard.getExtras().getXsrfToken(), enterOrDelete);
        enterLeaveTask.execute();
    }

    public void onEnterLeaveResult(String what, Boolean success) {
        Log.v(TAG, "Enter Leave Result -> " + what + ", " + success);
        if (success == Boolean.TRUE) {

            GiveawayExtras extras = giveawayCard.getExtras();
            giveaway.setEntered(ENTRY_INSERT.equals(what));
            extras.setEntered(giveaway.isEntered());

            giveawayCard.setExtras(extras);
            adapter.setStickyItem(giveawayCard);

            if (parent instanceof IGiveawayUpdateNotification) {
                ((IGiveawayUpdateNotification) parent).onUpdateGiveawayStatus(giveaway.getGiveawayId(), extras.isEntered());
            } else {
                Log.d(TAG, "No parent giveaway to update status");
            }
        } else {
            Log.e(TAG, "Probably an error catching the result...");
        }
    }

    public void requestComment(String xsrfToken, Integer parentComment) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        WriteCommentFragment wcf = WriteCommentFragment.newInstance(giveaway.getGiveawayId(), giveaway.getName(), xsrfToken);
        wcf.show(fm, "writecomment");
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.giveaway_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.open_steam_store:
                Log.i(TAG, "Opening Steam Store entry for game " + giveaway.getGameId());

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://store.steampowered.com/" + giveaway.getType().name().toLowerCase() + "/" + giveaway.getGameId() + "/"));
                startActivity(intent);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
