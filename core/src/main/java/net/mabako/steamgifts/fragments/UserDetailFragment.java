package net.mabako.steamgifts.fragments;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.UrlHandlingActivity;
import net.mabako.steamgifts.adapters.CommentAdapter;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicUser;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.User;
import net.mabako.steamgifts.fragments.interfaces.ICommentableFragment;
import net.mabako.steamgifts.fragments.interfaces.IHasWhitelistAndBlacklist;
import net.mabako.steamgifts.fragments.interfaces.IUserNotifications;
import net.mabako.steamgifts.tasks.LoadUserDetailsTask;
import net.mabako.steamgifts.tasks.LoadUserTradeFeedbackTask;
import net.mabako.steamgifts.tasks.UpdateWhitelistBlacklistTask;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class UserDetailFragment extends Fragment implements IUserNotifications, IHasWhitelistAndBlacklist {
    private static final String TAG = UserDetailFragment.class.getSimpleName();
    public static final String ARG_USER = "user";
    private static final String SAVED_USER = "user";

    private User user;
    private String xsrfToken;

    private CustomPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private Button whitelist, blacklist;

    private UpdateWhitelistBlacklistTask updateWhitelistBlacklistTask;

    public static UserDetailFragment newInstance(String userName) {
        UserDetailFragment fragment = new UserDetailFragment();

        Bundle args = new Bundle();
        args.putSerializable(SAVED_USER, new User(userName));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            user = (User) getArguments().getSerializable(SAVED_USER);
        } else {
            user = (User) savedInstanceState.getSerializable(SAVED_USER);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_USER, user);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (updateWhitelistBlacklistTask != null) {
            updateWhitelistBlacklistTask.cancel(true);
            updateWhitelistBlacklistTask = null;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_user, container, false);

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(getNonConfusingUsername());

        UserGiveawayListFragment fragmentSent = UserGiveawayListFragment.newInstance(user, "");
        fragmentSent.setiUserNotification(this);

        UserGiveawayListFragment fragmentWon = UserGiveawayListFragment.newInstance(user, "/giveaways/won");
        fragmentWon.setiUserNotification(this);

        UserTradeFeedbackListFragment fragmentPositiveFeedback = UserTradeFeedbackListFragment.newInstance(user, "positive");
        fragmentPositiveFeedback.setiUserNotification(this);

        UserTradeFeedbackListFragment fragmentNegativeFeedback = UserTradeFeedbackListFragment.newInstance(user, "negative");
        fragmentNegativeFeedback.setiUserNotification(this);

        viewPager = (ViewPager) layout.findViewById(R.id.viewPager);
        viewPagerAdapter = new CustomPagerAdapter((AppCompatActivity) getActivity(), viewPager, fragmentSent, fragmentWon, fragmentPositiveFeedback, fragmentNegativeFeedback);
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout = (TabLayout) layout.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        whitelist = (Button) layout.findViewById(R.id.whitelist);
        whitelist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestUserListed(user, What.WHITELIST, !user.isWhitelisted());
            }
        });

        blacklist = (Button) layout.findViewById(R.id.blacklist);
        blacklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestUserListed(user, What.BLACKLIST, !user.isBlacklisted());
            }
        });

        setHasOptionsMenu(true);

        return layout;
    }

    @Override
    public void onUserUpdated(User user) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getNonConfusingUsername());

                StringBuilder subtitle = new StringBuilder();
                if (user.getRole() != null)
                    subtitle.append(user.getRole()).append(" \u2022 ");
                subtitle.append(getString(R.string.user_level, user.getLevel()));

                actionBar.setSubtitle(subtitle);
            }
        }

        // Rescale the avatar to not take up the full navbar height.
        int attrs[] = new int[]{R.attr.actionBarSize};
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs);
        int size = (int) (ta.getDimensionPixelSize(0, 0) * 0.75f);

        Picasso.with(getContext()).load(user.getAvatar()).placeholder(R.drawable.default_avatar_mask).resize(size, size).transform(new RoundedCornersTransformation(20, 0)).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                if (activity != null) {
                    ActionBar actionBar = activity.getSupportActionBar();
                    if (actionBar != null) {
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        actionBar.setDisplayUseLogoEnabled(true);
                        actionBar.setDisplayShowHomeEnabled(true);
                        actionBar.setIcon(drawable);
                    }
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

        if (user.getId() != 0) {
            whitelist.setVisibility(View.VISIBLE);
            blacklist.setVisibility(View.VISIBLE);

            updateWhitelistBlacklistButtons();
        }

        // Refresh tabs
        for (int i = 0; i < viewPagerAdapter.getCount(); ++i)
            tabLayout.getTabAt(i).setText(viewPagerAdapter.getPageTitle(i));
    }

    /**
     * To prevent some confusion, we'll convert names with -some- characters to lowercase as well.
     * This helps in particular if you're called MuIIins, which is actually muiiins and not mullins.
     *
     * @return Lowercase and normal if the user name contains any of the blacklisted characters;
     * otherwise just the regular name
     */
    private String getNonConfusingUsername() {
        String name = user.getName();
        if (name.contains("I") || name.contains("O"))
            // If this list of characters changes, make sure name.toLowerCase() != name before formatting.
            return String.format("%s (%s)", name.toLowerCase(Locale.ENGLISH), name);
        return name;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.user_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.open_steam_profile) {
            UrlHandlingActivity.getIntentForUri(getContext(), Uri.parse("http://steamcommunity.com/profiles/" + user.getSteamID64()), true).start(getActivity());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void requestUserListed(BasicUser user, What what, boolean adding) {
        if (user == null || user.getId() == 0) {
            Log.w(TAG, "No user to white-/blacklist -- " + user);
            return;
        }

        if (xsrfToken == null) {
            Log.w(TAG, "No XSRF token to white-/blacklist");
            return;
        }

        if (updateWhitelistBlacklistTask != null)
            updateWhitelistBlacklistTask.cancel(true);

        updateWhitelistBlacklistTask = new UpdateWhitelistBlacklistTask(this, getContext(), xsrfToken, what, user, adding);
        updateWhitelistBlacklistTask.execute();
    }

    @Override
    public void onUserWhitelistOrBlacklistUpdated(BasicUser user, What what, boolean added) {
        Log.d(TAG, "user white/-blacklist updated: " + user.getName() + "; " + what + "; " + added);

        if (user instanceof User) {
            ((User) user).setBlacklisted(what == What.BLACKLIST && added);
            ((User) user).setWhitelisted(what == What.WHITELIST && added);
        }

        updateWhitelistBlacklistButtons();
    }

    @SuppressWarnings("ResourceAsColor")
    private void updateWhitelistBlacklistButtons() {
        int attrs[] = new int[]{android.R.attr.textColorPrimary};
        TypedArray ta = getContext().getTheme().obtainStyledAttributes(attrs);

        whitelist.setTextColor(user.isWhitelisted() ? ContextCompat.getColor(getContext(), R.color.colorAccent) : ta.getColor(0, 0));
        blacklist.setTextColor(user.isBlacklisted() ? ContextCompat.getColor(getContext(), R.color.colorAccent) : ta.getColor(0, 0));
    }

    private class CustomPagerAdapter extends FragmentAdapter {
        public CustomPagerAdapter(AppCompatActivity activity, ViewPager viewPager, Fragment... fragments) {
            super(activity, viewPager, fragments);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    if (user.isLoaded()) {
                        return String.format(getString(R.string.user_giveaways_created_count), user.getCreated(), user.getCreatedAmount());
                    } else {
                        return getString(R.string.user_giveaways_created);
                    }
                case 1:
                    if (user.isLoaded()) {
                        return String.format(getString(R.string.user_giveaway_won_count), user.getWon(), user.getWonAmount());
                    } else {
                        return getString(R.string.user_giveaway_won);
                    }
                case 2:
                    if (user.isFeedbackLoaded()) {
                        return String.format(getString(R.string.user_trade_feedback_positive_count), user.getPositiveFeedback());
                    } else {
                        return getString(R.string.user_trade_feedback_positive);
                    }
                case 3:
                    if (user.isFeedbackLoaded()) {
                        return String.format(getString(R.string.user_trade_feedback_negative_count), user.getNegativeFeedback());
                    } else {
                        return getString(R.string.user_trade_feedback_negative);
                    }
            }
            return null;
        }
    }

    public static class UserGiveawayListFragment extends ListFragment<GiveawayAdapter> implements IUserNotifications {
        private static final String SAVED_PATH = "rating";

        private User user;
        private String path;
        private IUserNotifications iUserNotification;

        public static UserGiveawayListFragment newInstance(User user, String path) {
            UserGiveawayListFragment fragment = new UserGiveawayListFragment();

            Bundle args = new Bundle();
            args.putSerializable(SAVED_USER, user);
            args.putString(SAVED_PATH, path);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            if (savedInstanceState == null) {
                user = (User) getArguments().getSerializable(SAVED_USER);
                path = getArguments().getString(SAVED_PATH);
            } else {
                user = (User) savedInstanceState.getSerializable(SAVED_USER);
                path = savedInstanceState.getString(SAVED_PATH);
            }

            super.onCreate(savedInstanceState);
            adapter.setFragmentValues(getActivity(), this, null);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable(SAVED_USER, user);
            outState.putString(SAVED_PATH, path);
        }

        @NonNull
        @Override
        protected GiveawayAdapter createAdapter() {
            return new GiveawayAdapter(25, PreferenceManager.getDefaultSharedPreferences(getContext()));
        }

        @Override
        public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems) {
            if (clearExistingItems && items == null && !user.isLoaded()) {
                Log.w(TAG + "/Giveaways", "User does not exist?");
                Toast.makeText(getContext(), "User does not exist.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            } else {
                super.addItems(items, clearExistingItems);
            }
        }

        @Override
        public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems, String xsrfToken) {
            super.addItems(items, clearExistingItems, xsrfToken);
            if (iUserNotification != null && iUserNotification instanceof UserDetailFragment)
                ((UserDetailFragment) iUserNotification).xsrfToken = xsrfToken;
        }

        @Override
        protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
            return new LoadUserDetailsTask(this, user.getName() + path, page, user);
        }

        @Override
        protected Serializable getType() {
            throw new UnsupportedOperationException();
        }

        // TODO should we care more about this not being properly reset after e.g. orientation is changed? Right now, there's not a whole lot of information we display in the first place.
        public void setiUserNotification(IUserNotifications iUserNotification) {
            this.iUserNotification = iUserNotification;
        }

        @Override
        public void onUserUpdated(User user) {
            if (iUserNotification != null)
                iUserNotification.onUserUpdated(user);
            else
                Log.d(TAG, "no iUserUpdateNotification");
        }
    }

    public static class UserTradeFeedbackListFragment extends ListFragment<CommentAdapter> implements IUserNotifications, ICommentableFragment {
        private static final String SAVED_RATING = "rating";

        private User user;
        private String rating;
        private IUserNotifications iUserNotification;

        public static UserTradeFeedbackListFragment newInstance(User user, String rating) {
            UserTradeFeedbackListFragment fragment = new UserTradeFeedbackListFragment();

            Bundle args = new Bundle();
            args.putSerializable(SAVED_USER, user);
            args.putString(SAVED_RATING, rating);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            if (savedInstanceState == null) {
                user = (User) getArguments().getSerializable(SAVED_USER);
                rating = getArguments().getString(SAVED_RATING);
            } else {
                user = (User) savedInstanceState.getSerializable(SAVED_USER);
                rating = savedInstanceState.getString(SAVED_RATING);
            }

            super.onCreate(savedInstanceState);
            adapter.setFragmentValues(this);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable(SAVED_USER, user);
            outState.putString(SAVED_RATING, rating);
        }

        @NonNull
        @Override
        protected CommentAdapter createAdapter() {
            return new CommentAdapter();
        }

        @Override
        protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
            return new LoadUserTradeFeedbackTask(this, user.getSteamID64(), rating, page, user);
        }

        @Override
        public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems) {
            if (clearExistingItems && items == null && !user.isLoaded()) {
                Log.w(TAG + "/Trades", "User does not exist?");
                Toast.makeText(getContext(), "User does not exist.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            } else {
                super.addItems(items, clearExistingItems);
            }
        }

        @Override
        public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems, String xsrfToken) {
            super.addItems(items, clearExistingItems, xsrfToken);
            if (iUserNotification != null && iUserNotification instanceof UserDetailFragment)
                ((UserDetailFragment) iUserNotification).xsrfToken = xsrfToken;
        }

        @Override
        protected Serializable getType() {
            throw new UnsupportedOperationException();
        }

        public void setiUserNotification(IUserNotifications iUserNotification) {
            this.iUserNotification = iUserNotification;
        }

        @Override
        public void onUserUpdated(User user) {
            iUserNotification.onUserUpdated(user);
        }

        @Override
        public void showProfile(String user) {
            throw new UnsupportedOperationException("Can't lookup user by name from trade feedback");
        }

        @Override
        public void showProfile(long steamID64) {
            Intent intent = UrlHandlingActivity.getIntentForUri(getContext(), Uri.parse("https://www.steamgifts.com/user/id/" + steamID64));
            getActivity().startActivity(intent);
        }

        @Override
        public void requestComment(Comment parentComment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteComment(Comment comment) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean canPostOrModifyComments() {
            return false;
        }
    }
}
