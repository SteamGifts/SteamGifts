package net.mabako.steamgifts.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.LoginActivity;
import net.mabako.steamgifts.activities.MainActivity;
import net.mabako.steamgifts.adapters.CommentAdapter;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.GiveawayExtras;
import net.mabako.steamgifts.fragments.util.WrappingLinearLayoutManager;
import net.mabako.steamgifts.tasks.EnterLeaveGiveawayTask;
import net.mabako.steamgifts.tasks.LoadAllGiveawaysTask;
import net.mabako.steamgifts.tasks.LoadGiveawayDetailsTask;

import java.util.ArrayList;

public class GiveawayDetailFragment extends Fragment {
    private static final String TAG = GiveawayDetailFragment.class.getSimpleName();

    public static final String ARG_GIVEAWAY = "giveaway";
    public static final String ENTRY_INSERT = "entry_insert";
    public static final String ENTRY_DELETE = "entry_delete";

    /**
     * Content to show for the giveaway details.
     */
    private Giveaway giveaway;
    private GiveawayExtras extras;

    private LoadGiveawayDetailsTask task;
    private TextView enterGiveaway;
    private TextView leaveGiveaway;
    private TextView timeRemaining;
    private TextView commentGiveaway;
    private EnterLeaveGiveawayTask enterLeaveTask;

    private RecyclerView listView;
    private CommentAdapter adapter;
    private Button loginButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_GIVEAWAY)) {
            giveaway = (Giveaway) getArguments().getSerializable(ARG_GIVEAWAY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CoordinatorLayout layout = (CoordinatorLayout) super.onCreateView(inflater, container, savedInstanceState);

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

        enterGiveaway = (TextView) activity.findViewById(R.id.enter);
        enterGiveaway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterGiveaway.setEnabled(false);

                if (enterLeaveTask != null)
                    enterLeaveTask.cancel(true);

                enterLeaveTask = new EnterLeaveGiveawayTask(GiveawayDetailFragment.this, giveaway.getGiveawayId(), extras.getXsrfToken(), ENTRY_INSERT);
                enterLeaveTask.execute();
            }
        });

        leaveGiveaway = (TextView) activity.findViewById(R.id.leave);
        leaveGiveaway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveGiveaway.setEnabled(false);

                if (enterLeaveTask != null)
                    enterLeaveTask.cancel(true);

                enterLeaveTask = new EnterLeaveGiveawayTask(GiveawayDetailFragment.this, giveaway.getGiveawayId(), extras.getXsrfToken(), ENTRY_DELETE);
                enterLeaveTask.execute();
            }
        });

        commentGiveaway = (TextView) activity.findViewById(R.id.comment);
        commentGiveaway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                WriteCommentFragment wcf = WriteCommentFragment.newInstance(giveaway.getGiveawayId(), giveaway.getName(), extras.getXsrfToken());
                wcf.show(fm, "writecomment");
            }
        });

        timeRemaining = (TextView) activity.findViewById(R.id.remaining);

        loginButton = (Button) activity.findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((DetailActivity) getActivity()).requestLogin();
            }
        });
        loginButton.setVisibility(View.GONE);

        listView = (RecyclerView) activity.findViewById(R.id.list);
        listView.setLayoutManager(new WrappingLinearLayoutManager(getActivity()));
        listView.setNestedScrollingEnabled(false);
        adapter = new CommentAdapter(getContext(), listView, new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {
                Log.v(TAG, "Load more items...");
                fetchItems(page);
            }
        });
        listView.setAdapter(adapter);

        if (giveaway != null) {
            setupGiveawayCard();
        }

        fetchItems(1);

        setHasOptionsMenu(true);

        return layout;
    }

    public void reload() {
        Log.d(TAG, "Reloading giveaway details");

        loginButton.setVisibility(View.GONE);
        enterGiveaway.setVisibility(View.GONE);
        leaveGiveaway.setVisibility(View.GONE);

        // remove all comments
        adapter.clear();

        // show the progress bar instead of the description again
        getActivity().findViewById(R.id.description).setVisibility(View.GONE);
        getActivity().findViewById(R.id.progressBar).setVisibility(View.VISIBLE);

        fetchItems(1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        task.cancel(true);

        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);
    }

    private void setupGiveawayCard() {
        // Format the "Enter (__P)" text to include the points
        enterGiveaway.setText(String.format(getString(R.string.enter_giveaway), giveaway.getPoints()));
        leaveGiveaway.setText(String.format(getString(R.string.leave_giveaway), giveaway.getPoints()));

        // Show the creator
        ((TextView) getActivity().findViewById(R.id.user)).setText("{faw-user} " + giveaway.getCreator());

        // Time remaining
        toggleRemainingTime();
        timeRemaining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRemainingTime();
            }
        });


        // TODO: Add comment
    }

    private void toggleRemainingTime() {
        String shortVariant = giveaway.getTimeRemaining();
        String longVariant = giveaway.getTimeRemainingLong();

        Log.d(TAG, "Current time text: " + timeRemaining.getText());
        timeRemaining.setText("{faw-clock-o} " + (timeRemaining.getText().toString().contains(shortVariant) ? longVariant : shortVariant));
    }

    public void fetchItems(int page) {
        Log.d(TAG, "Fetching giveaways on page " + page + " for giveaway " + giveaway.getGiveawayId());

        if (task != null)
            task.cancel(true);

        task = new LoadGiveawayDetailsTask(this, giveaway.getGiveawayId(), page);
        task.execute();
    }

    public void addDetails(GiveawayExtras extras, int page) {
        this.extras = extras;

        getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE);
        if (extras.getDescription() != null) {
            TextView description = (TextView) getActivity().findViewById(R.id.description);

            CharSequence desc = Html.fromHtml(extras.getDescription());
            desc = desc.subSequence(0, desc.length() - 2);

            description.setText(desc);
            description.setVisibility(View.VISIBLE);
            description.setMovementMethod(LinkMovementMethod.getInstance());
        }

        // We have a token to send with an ajax request now, so we can possibly enter or leave this giveaway.
        if (extras.getXsrfToken() != null) {
            getActivity().findViewById(R.id.comment).setVisibility(View.VISIBLE);

            loginButton.setVisibility(View.GONE);
            if (extras.isEntered())
                leaveGiveaway.setVisibility(View.VISIBLE);
            else
                enterGiveaway.setVisibility(View.VISIBLE);
        } else {
            Log.w(TAG, "No XSRF Token for Giveaway...");

            enterGiveaway.setVisibility(View.GONE);
            leaveGiveaway.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
        }


        Log.d(TAG, "Adding " + extras.getComments().size() + " comments");

        if (page == 1)
            adapter.clear();
        adapter.finishLoading(extras.getComments());
    }

    public void onEnterLeaveResult(String what, Boolean success) {
        if (success == Boolean.TRUE) {
            if (ENTRY_INSERT.equals(what)) {
                // We've just managed to enter the giveaway.
                enterGiveaway.setVisibility(View.GONE);
                leaveGiveaway.setVisibility(View.VISIBLE);
            } else {
                enterGiveaway.setVisibility(View.VISIBLE);
                leaveGiveaway.setVisibility(View.GONE);
            }
        } else if (success == Boolean.FALSE) {
            Log.e(TAG, "Definitively an error.");
        } else {
            Log.e(TAG, "Probably an error catching the result...");
        }

        enterGiveaway.setEnabled(true);
        leaveGiveaway.setEnabled(true);
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
