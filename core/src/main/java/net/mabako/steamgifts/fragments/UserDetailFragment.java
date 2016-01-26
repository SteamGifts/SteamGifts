package net.mabako.steamgifts.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import net.mabako.steamgifts.activities.WebViewActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.User;
import net.mabako.steamgifts.fragments.interfaces.IUserNotifications;
import net.mabako.steamgifts.tasks.LoadUserDetailsTask;

import java.io.Serializable;
import java.util.List;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class UserDetailFragment extends Fragment implements IUserNotifications {
    private static final String TAG = UserDetailFragment.class.getSimpleName();
    public static final String ARG_USER = "user";
    private static final String SAVED_USER = "user";

    private User user;

    private CustomPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

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

        // TODO ideally restore instance state
        user = (User) getArguments().getSerializable(SAVED_USER);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_user, container, false);

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (toolbar != null)
            toolbar.setTitle(user.getName());

        viewPagerAdapter = new CustomPagerAdapter(getActivity().getSupportFragmentManager());
        viewPager = (ViewPager) layout.findViewById(R.id.viewPager);
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout = (TabLayout) layout.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        setHasOptionsMenu(true);

        return layout;
    }

    @Override
    public void onUserUpdated(User user) {
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(user.getName());
                actionBar.setSubtitle(getString(R.string.user_level, user.getLevel()));
            }
        }

        // Avatar?
        Picasso.with(getContext()).load(user.getAvatar()).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                if (activity != null) {
                    ActionBar actionBar = activity.getSupportActionBar();
                    if (actionBar != null) {
                        Drawable drawable = new BitmapDrawable(getResources(), bitmap);
                        actionBar.setIcon(drawable);
                        actionBar.setHomeButtonEnabled(false);
                        actionBar.setDisplayShowHomeEnabled(true);
                        actionBar.setDisplayHomeAsUpEnabled(false);
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

        // Refresh tabs
        for (int i = 0; i < viewPagerAdapter.getCount(); ++i)
            tabLayout.getTabAt(i).setText(viewPagerAdapter.getPageTitle(i));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.user_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO white- and blacklisting
        int itemId = item.getItemId();
        if (itemId == R.id.open_steam_profile) {
            Intent intent = new Intent(getActivity(), WebViewActivity.class);
            intent.putExtra(WebViewActivity.ARG_URL, user.getUrl());
            getActivity().startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private class CustomPagerAdapter extends FragmentPagerAdapter {
        public CustomPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return UserGiveawayListFragment.newInstance(user, "", true, UserDetailFragment.this);
                case 1:
                    return UserGiveawayListFragment.newInstance(user, "/giveaways/won", false, UserDetailFragment.this);
            }
            return null;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (user.isLoaded()) {
                switch (position) {
                    case 0:
                        return String.format(getString(R.string.user_giveaways_created_count), user.getCreated(), user.getCreatedAmount());
                    case 1:
                        return String.format(getString(R.string.user_giveaway_won_count), user.getWon(), user.getWonAmount());
                }
            } else {
                switch (position) {
                    case 0:
                        return getString(R.string.user_giveaways_created);
                    case 1:
                        return getString(R.string.user_giveaway_won);
                }
            }
            return null;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    public static class UserGiveawayListFragment extends ListFragment<GiveawayAdapter> implements IUserNotifications {
        private User user;
        private String path;
        private IUserNotifications iUserNotification;

        public static UserGiveawayListFragment newInstance(User user, String path, boolean loadItemsInitially, IUserNotifications iUserNotification) {
            UserGiveawayListFragment fragment = new UserGiveawayListFragment();
            fragment.user = user;
            fragment.path = user.getName() + path;
            fragment.loadItemsInitially = loadItemsInitially;
            fragment.iUserNotification = iUserNotification;
            return fragment;
        }

        @Override
        protected GiveawayAdapter createAdapter() {
            return new GiveawayAdapter(getActivity(), new EndlessAdapter.OnLoadListener() {
                @Override
                public void onLoad(int page) {
                    fetchItems(page);
                }
            }, null, 25, PreferenceManager.getDefaultSharedPreferences(getContext()));
        }

        @Override
        public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems) {
            if (clearExistingItems && items == null && !user.isLoaded()) {
                Toast.makeText(getContext(), "User does not exist.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            } else {
                super.addItems(items, clearExistingItems);
            }
        }

        @Override
        protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
            return new LoadUserDetailsTask(this, path, page, user);
        }

        @Override
        protected Serializable getType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void onUserUpdated(User user) {
            iUserNotification.onUserUpdated(user);
        }
    }
}
