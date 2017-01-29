package net.mabako.steamgifts.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.mabako.steam.store.StoreAppFragment;
import net.mabako.steam.store.StoreSubFragment;
import net.mabako.steamgifts.ApplicationTemplate;
import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.MainActivity;
import net.mabako.steamgifts.activities.UrlHandlingActivity;
import net.mabako.steamgifts.activities.WriteCommentActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicGiveaway;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.GiveawayExtras;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.fragments.interfaces.IHasHideableGiveaways;
import net.mabako.steamgifts.fragments.util.GiveawayDetailsCard;
import net.mabako.steamgifts.fragments.util.GiveawayListFragmentStack;
import net.mabako.steamgifts.persistentdata.SavedGiveaways;
import net.mabako.steamgifts.tasks.EnterLeaveGiveawayTask;
import net.mabako.steamgifts.tasks.LoadGiveawayDetailsTask;
import net.mabako.steamgifts.tasks.UpdateGiveawayFilterTask;

import java.io.Serializable;
import java.util.Locale;

public class GiveawayDetailFragment extends DetailFragment implements IHasEnterableGiveaways, IHasHideableGiveaways {
    public static final String ARG_GIVEAWAY = "giveaway";

    public static final String ENTRY_INSERT = "entry_insert";
    public static final String ENTRY_DELETE = "entry_delete";

    private static final String TAG = GiveawayDetailFragment.class.getSimpleName();
    private static final String SAVED_GIVEAWAY = "giveaway";

    private boolean fragmentAdded = false;

    /**
     * Content to show for the giveaway details.
     */
    private BasicGiveaway giveaway;
    private GiveawayDetailsCard giveawayCard;
    private EnterLeaveGiveawayTask enterLeaveTask;
    private Activity activity;
    private SavedGiveaways savedGiveaways;

    public static Fragment newInstance(@NonNull BasicGiveaway giveaway, @Nullable CommentContextInfo context) {
        GiveawayDetailFragment fragment = new GiveawayDetailFragment();

        Bundle args = new Bundle();
        args.putSerializable(SAVED_GIVEAWAY, giveaway);
        args.putSerializable(SAVED_COMMENT_CONTEXT, context);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            giveaway = (BasicGiveaway) getArguments().getSerializable(SAVED_GIVEAWAY);
            giveawayCard = new GiveawayDetailsCard();

            // Add the cardview for the Giveaway details
            adapter.setStickyItem(giveawayCard);
        } else {
            giveaway = (BasicGiveaway) savedInstanceState.getSerializable(SAVED_GIVEAWAY);
            giveawayCard = (GiveawayDetailsCard) adapter.getStickyItem();
        }

        adapter.setFragmentValues(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_GIVEAWAY, giveaway);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        savedGiveaways = new SavedGiveaways(context);

        if (context instanceof Activity)
            this.activity = (Activity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (savedGiveaways != null) {
            savedGiveaways.close();
            savedGiveaways = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);

        if (giveaway instanceof Giveaway) {
            onPostGiveawayLoaded((Giveaway) giveaway, true);
        } else {
            Log.d(TAG, "Loading activity for basic giveaway " + giveaway.getGiveawayId());
        }

        setHasOptionsMenu(true);

        return layout;
    }

    public void reload() {
        fetchItems(1);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTaskEx(int page) {
        String url = giveaway.getGiveawayId();
        if (giveaway instanceof Giveaway)
            url += "/" + ((Giveaway) giveaway).getName();
        else if (getCommentContext() != null)
            url += "/" + getCommentContext().getDetailName();

        return new LoadGiveawayDetailsTask(this, url, page, !(giveaway instanceof Giveaway));
    }

    public void addItems(GiveawayExtras extras, int page) {
        if (extras == null)
            return;

        addItems(extras.getComments(), page == 1, extras.getXsrfToken());

        // We should always have a giveaway instance at this point of time, as
        // #onPostGiveawayLoaded is called prior to this method.
        if (!(giveaway instanceof Giveaway))
            throw new IllegalStateException("#onPostGiveawayLoaded was probably not called");
        ((Giveaway) giveaway).setTitle(extras.getTitle());
        updateTitle();

        giveawayCard.setExtras(extras);
        if (getActivity() != null)
            getActivity().supportInvalidateOptionsMenu();
        adapter.setStickyItem(giveawayCard);
    }

    @Override
    public void requestEnterLeave(String giveawayId, String enterOrDelete, String xsrfToken) {
        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);

        enterLeaveTask = new EnterLeaveGiveawayTask(this, getContext(), giveawayId, xsrfToken, enterOrDelete);
        enterLeaveTask.execute();
    }

    /**
     * This is called for the current game, but not for other games up the stack - only list fragments.
     *
     * @param giveawayId ID of the giveaway
     * @param what       what kind of action was executed
     * @param success    whether or not the action was successful
     * @param propagate
     */
    @Override
    public void onEnterLeaveResult(String giveawayId, String what, Boolean success, boolean propagate) {
        Log.v(TAG, "Enter Leave Result -> " + what + ", " + success);
        if (success == Boolean.TRUE) {

            GiveawayExtras extras = giveawayCard.getExtras();
            extras.setEntered(ENTRY_INSERT.equals(what));
            ((Giveaway) giveaway).setEntered(extras.isEntered());

            giveawayCard.setExtras(extras);
            adapter.setStickyItem(giveawayCard);
        } else {
            Log.e(TAG, "Probably an error catching the result...");
        }

        if (propagate)
            GiveawayListFragmentStack.onEnterLeaveResult(giveawayId, what, success);
    }

    public void onEntered() {
        onEnterLeaveResult(giveaway.getGiveawayId(), ENTRY_INSERT, true, true);
    }

    /**
     * Set the details from the task started by {@link #fetchItems(int)}.
     *
     * @param giveaway giveaway this is for
     */
    private void onPostGiveawayLoaded(Giveaway giveaway, boolean ignoreExisting) {
        // Called this twice, eh...
        if (this.giveaway instanceof Giveaway && !ignoreExisting)
            return;

        this.giveaway = giveaway;
        giveawayCard.setGiveaway(giveaway);

        updateTitle();
        final CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
        if (appBarLayout != null && ((ApplicationTemplate) getActivity().getApplication()).allowGameImages()) {
            ImageView toolbarImage = (ImageView) getActivity().findViewById(R.id.toolbar_image);
            if (toolbarImage != null) {
                Picasso.with(getContext()).load("http://cdn.akamai.steamstatic.com/steam/" + giveaway.getType().name().toLowerCase(Locale.ENGLISH) + "s/" + giveaway.getGameId() + "/header.jpg").into(toolbarImage, new Callback() {
                    @Override
                    public void onSuccess() {
                        appBarLayout.setExpandedTitleTextAppearance(R.style.TransparentText);
                    }

                    @Override
                    public void onError() {

                    }
                });
            }
        }


        // Re-build the options menu, which may not be created if no giveaway was present.
        getActivity().supportInvalidateOptionsMenu();

        if (getActivity() instanceof DetailActivity && giveaway.getGameId() != Game.NO_APP_ID && !fragmentAdded) {
            ((DetailActivity) getActivity()).addFragmentUnlessExists(giveaway.getType() == Game.Type.APP ? StoreAppFragment.newInstance(giveaway.getGameId(), false) : StoreSubFragment.newInstance(giveaway.getGameId()));
            fragmentAdded = true;
        }

        if (savedGiveaways != null && savedGiveaways.exists(giveaway.getGiveawayId())) {
            // refresh the saved giveaway... this is probably only noteworthy if, for some reason, the giveaway has begun after we starred it.
            savedGiveaways.add(giveaway, giveaway.getGiveawayId());
        }
    }

    public void onPostGiveawayLoaded(Giveaway giveaway) {
        onPostGiveawayLoaded(giveaway, false);
    }

    @Override
    public void showProfile(String user) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(UserDetailFragment.ARG_USER, user);
        getActivity().startActivity(intent);
    }

    @Override
    public void showProfile(long steamID64) {
        throw new UnsupportedOperationException("Fetching user details by steamID64");
    }

    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        if (giveaway instanceof Giveaway) {
            inflater.inflate(R.menu.giveaway_menu, menu);
            menu.findItem(R.id.open_steam_store).setVisible(((Giveaway) giveaway).getGameId() > 0);
            menu.findItem(R.id.hide_game).setEnabled(((Giveaway) giveaway).getInternalGameId() > 0 && giveawayCard.getExtras() != null && giveawayCard.getExtras().getXsrfToken() != null);

            if (savedGiveaways != null) {
                boolean isSaved = savedGiveaways.exists(giveaway.getGiveawayId());
                menu.findItem(R.id.add_saved_element).setVisible(!isSaved);
                menu.findItem(R.id.remove_saved_element).setVisible(isSaved);
            }

            menu.findItem(R.id.more_like_this).setEnabled(((Giveaway) giveaway).getTitle() != null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.open_steam_store) {
            if (this.giveaway instanceof Giveaway) {
                Giveaway giveaway = (Giveaway) this.giveaway;
                Log.i(TAG, "Opening Steam Store entry for game " + giveaway.getGameId());

                UrlHandlingActivity.getIntentForUri(activity, Uri.parse("http://store.steampowered.com/" + giveaway.getType().name().toLowerCase(Locale.ENGLISH) + "/" + giveaway.getGameId() + "/"), true).start(activity);
            }
            return true;
        } else if (itemId == R.id.hide_game) {
            new UpdateGiveawayFilterTask<>(this, giveawayCard.getExtras().getXsrfToken(), UpdateGiveawayFilterTask.HIDE, ((Giveaway) giveaway).getInternalGameId(), ((Giveaway) giveaway).getTitle()).execute();
            return true;
        } else if (itemId == R.id.add_saved_element) {
            if (giveaway instanceof Giveaway && savedGiveaways.add((Giveaway) giveaway, giveaway.getGiveawayId())) {
                getActivity().supportInvalidateOptionsMenu();
                Toast.makeText(getContext(), R.string.added_saved_giveaway, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (itemId == R.id.remove_saved_element) {
            if (giveaway instanceof Giveaway && savedGiveaways.remove(giveaway.getGiveawayId())) {
                getActivity().supportInvalidateOptionsMenu();
                Toast.makeText(getContext(), R.string.removed_saved_giveaway, Toast.LENGTH_SHORT).show();

                GiveawayListFragmentStack.onRemoveSavedGiveaway(giveaway.getGiveawayId());
            }
            return true;
        } else if (itemId == R.id.more_like_this) {
            Intent intent = new Intent(getContext(), MainActivity.class);
            intent.putExtra(MainActivity.ARG_TYPE, GiveawayListFragment.Type.ALL);
            intent.putExtra(MainActivity.ARG_QUERY, ((Giveaway) giveaway).getTitle());
            intent.putExtra(MainActivity.ARG_NO_DRAWER, true);
            getActivity().startActivityForResult(intent, CommonActivity.REQUEST_LOGIN);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This is called for the current game, but not for other games up the stack - only list fragments.
     *
     * @param internalGameId
     * @param propagate
     * @param title
     */
    @Override
    public void onHideGame(long internalGameId, boolean propagate, String title) {
        activity.finish();

        if (propagate)
            GiveawayListFragmentStack.onHideGame(internalGameId);
    }

    @NonNull
    @Override
    protected Serializable getDetailObject() {
        return giveaway;
    }

    @Nullable
    @Override
    protected String getDetailPath() {
        if (giveaway instanceof Giveaway)
            return "giveaway/" + giveaway.getGiveawayId() + "/" + ((Giveaway) giveaway).getName();

        return null;
    }

    @Override
    protected String getTitle() {
        return giveaway instanceof Giveaway ? ((Giveaway) giveaway).getTitle() : null;
    }

    /**
     * Update the title in the CollapsingToolbarLayout or Actionbar, depending on which is used.
     */
    private void updateTitle() {
        Log.v(TAG, "Setting title to " + getTitle());
        final CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(getTitle());
        } else {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null)
                actionBar.setTitle(getTitle());
        }
    }

    @Override
    protected void addExtraForCommentIntent(@NonNull Intent intent, @Nullable Comment parentComment) {
        if (parentComment == null // no parent comment
                && giveaway instanceof Giveaway // giveaway loaded
                && !((Giveaway) giveaway).isEntered() // haven't entered the giveaway yet
                && ((Giveaway) giveaway).isOpen() // it is actually open (not in the past, not in the future)
                && adapter.getXsrfToken() != null // we can do soemthing with the giveaway
                && giveawayCard.getExtras() != null // giveaway loaded #2
                && giveawayCard.getExtras().isEnterable()) // we can actually enter
            intent.putExtra(WriteCommentActivity.GIVEAWAY_ID, giveaway.getGiveawayId());
    }
}
