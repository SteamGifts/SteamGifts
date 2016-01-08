package net.mabako.steamgifts.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.User;
import net.mabako.steamgifts.tasks.LoadUserDetailsTask;

import java.io.Serializable;
import java.util.List;

public class UserDetailFragment extends Fragment implements IUserNotifications {
    private static final String TAG = UserDetailFragment.class.getSimpleName();
    public static final String ARG_USER = "user";

    private User user;

    private CustomPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tabLayout;

    public static UserDetailFragment newInstance(String userName) {
        UserDetailFragment fragment = new UserDetailFragment();
        fragment.user = new User(userName);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_user, container, false);

        ActionBar toolbar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        toolbar.setTitle(user.getName());

        viewPagerAdapter = new CustomPagerAdapter(getActivity().getSupportFragmentManager());
        viewPager = (ViewPager) layout.findViewById(R.id.viewPager);
        viewPager.setAdapter(viewPagerAdapter);

        tabLayout = (TabLayout) layout.findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        return layout;
    }

    @Override
    public void onUserUpdated(User user) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        actionBar.setTitle(user.getName());
        actionBar.setSubtitle(getString(R.string.user_level, user.getLevel()));

        // Refresh tabs
        for (int i = 0; i < viewPagerAdapter.getCount(); ++i)
            tabLayout.getTabAt(i).setText(viewPagerAdapter.getPageTitle(i));
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
            fragment.allowSearch = false;
            fragment.iUserNotification = iUserNotification;
            return fragment;
        }

        @Override
        protected GiveawayAdapter createAdapter(RecyclerView listView) {
            return new GiveawayAdapter(getActivity(), listView, new EndlessAdapter.OnLoadListener() {
                @Override
                public void onLoad(int page) {
                    fetchItems(page);
                }
            }, null, 25);
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
        protected void fetchItems(int page) {
            new LoadUserDetailsTask(this, path, page, user).execute();
        }

        @Override
        protected Serializable getType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getTitleResource() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getExtraTitle() {
            throw new UnsupportedOperationException();
        }

        /**
         * Load the "won" giveaways only if the user's been navigating to that tab.
         *
         * @param isVisibleToUser
         */
        @Override
        public void setUserVisibleHint(boolean isVisibleToUser) {
            super.setUserVisibleHint(isVisibleToUser);
            if (isVisibleToUser && !loadItemsInitially) {
                loadItemsInitially = true;
                fetchItems(1);
            }
        }

        @Override
        public void onUserUpdated(User user) {
            iUserNotification.onUserUpdated(user);
        }
    }
}
