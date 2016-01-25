package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.settings.SettingsActivity;
import net.mabako.steamgifts.fragments.DiscussionListFragment;
import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.SavedGiveawaysFragment;
import net.mabako.steamgifts.fragments.SearchableListFragment;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.intro.Intro;
import net.mabako.steamgifts.persistentdata.IPointUpdateNotification;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.tasks.LogoutTask;

import java.io.Serializable;

public class MainActivity extends CommonActivity implements IPointUpdateNotification {
    public static final String ARG_TYPE = "type";

    private static final String TAG = MainActivity.class.getSimpleName();

    private AccountHeader accountHeader;
    private Drawer drawer;
    private ProfileDrawerItem profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        SteamGiftsUserData.addUpdateHandler(this);
        onUpdatePoints(SteamGiftsUserData.getCurrent().getPoints());

        setupNavBar();

        // savedInstanceState is non-null if a fragment state is saved from a previous configuration.
        if (savedInstanceState == null) {
            // Load a default fragment to show all giveaways
            Serializable type = getIntent().getSerializableExtra(ARG_TYPE);
            if (type == null)
                type = GiveawayListFragment.Type.ALL;

            if (type instanceof GiveawayListFragment.Type) {
                loadFragment(GiveawayListFragment.newInstance((GiveawayListFragment.Type) type, null));
                drawer.setSelection(((GiveawayListFragment.Type) type).getNavbarResource(), false);
            } else if (type instanceof DiscussionListFragment.Type) {
                loadFragment(DiscussionListFragment.newInstance((DiscussionListFragment.Type) type, null));
                drawer.setSelection(((DiscussionListFragment.Type) type).getNavbarResource(), false);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            Intro.showIntroIfNeccessary(this, Intro.INTRO_MAIN, Intro.INTRO_MAIN_VERSION);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SteamGiftsUserData.removeUpdateHandler(this);
    }

    /**
     * Triggered upon the user logging in or logging out.
     */
    public void onAccountChange() {
        // Reconfigure our navigation bar items.
        reconfigureNavBarItems();

        super.onAccountChange();

        loadFragment(GiveawayListFragment.newInstance(GiveawayListFragment.Type.ALL, null));
        drawer.setSelection(R.string.navigation_giveaways_all, false);
    }

    @Override
    protected void loadFragment(Fragment fragment) {
        super.loadFragment(fragment);
        onUpdatePoints(SteamGiftsUserData.getCurrent().getPoints());
    }

    private void setupNavBar() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(R.drawable.default_avatar).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });

        int attrs[] = new int[]{R.attr.colorAccountHeader};
        TypedArray ta = getTheme().obtainStyledAttributes(attrs);

        // Account?
        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
                .withHeaderBackground(ta.getDrawable(0))
                .withSelectionListEnabledForSingleProfile(false)
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        if (SteamGiftsUserData.getCurrent().isLoggedIn()) {
                            Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                            intent.putExtra(UserDetailFragment.ARG_USER, SteamGiftsUserData.getCurrent().getName());
                            startActivity(intent);
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onProfileImageLongClick(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .build();

        accountHeader.getView().findViewById(R.id.material_drawer_account_header_notifications).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SteamGiftsUserData.getCurrent().isLoggedIn()) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                    intent.putExtra(DetailActivity.ARG_NOTIFICATIONS, true);
                    startActivity(intent);
                }
            }
        });

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar((Toolbar) findViewById(R.id.toolbar))
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withAccountHeader(accountHeader)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // Stop searching, if any is done
                        Fragment fragment = getCurrentFragment();
                        if (fragment instanceof SearchableListFragment)
                            ((SearchableListFragment) fragment).stopSearch();

                        switch (drawerItem.getIdentifier()) {
                            case R.string.login:
                                requestLogin();
                                break;

                            case R.string.logout:
                                new LogoutTask(MainActivity.this, SteamGiftsUserData.getCurrent().getSessionId()).execute();

                                SteamGiftsUserData.clear();
                                onAccountChange();
                                break;

                            case R.string.navigation_help:
                                Intro.showIntro(MainActivity.this, Intro.INTRO_MAIN);
                                break;

                            case R.string.navigation_about:
                                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                                break;

                            case R.string.preferences:
                                startActivityForResult(new Intent(MainActivity.this, SettingsActivity.class), REQUEST_SETTINGS);
                                break;

                            case R.string.navigation_giveaways_saved:
                                loadFragment(new SavedGiveawaysFragment());
                                ActionBar actionBar = getSupportActionBar();
                                if (actionBar != null)
                                    actionBar.setSubtitle(null);
                                break;

                            default:
                                for (GiveawayListFragment.Type type : GiveawayListFragment.Type.values()) {
                                    if (type.getNavbarResource() == drawerItem.getIdentifier()) {
                                        loadFragment(GiveawayListFragment.newInstance(type, null));
                                        break;
                                    }
                                }

                                for (DiscussionListFragment.Type type : DiscussionListFragment.Type.values()) {
                                    if (type.getNavbarResource() == drawerItem.getIdentifier()) {
                                        loadFragment(DiscussionListFragment.newInstance(type, null));
                                        break;
                                    }
                                }


                                return false;
                        }

                        drawer.closeDrawer();
                        return true;
                    }
                })
                .withOnDrawerListener(new Drawer.OnDrawerListener() {
                    @Override
                    public void onDrawerOpened(View drawerView) {

                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {

                    }

                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        TextView notifications = (TextView) accountHeader.getView().findViewById(R.id.material_drawer_account_header_notifications);

                        // Are we even logged in?
                        SteamGiftsUserData user = SteamGiftsUserData.getCurrent();
                        if (user.isLoggedIn()) {
                            // Format the string
                            String newInfo = String.format(getString(R.string.drawer_profile_info), user.getLevel(), user.getPoints());

                            // Is this still up-to-date?
                            if (!newInfo.equals(profile.getEmail().toString())) {
                                profile.withEmail(newInfo);
                                accountHeader.updateProfile(profile);
                            }

                            if (user.hasNotifications()) {
                                StringBuilder sb = new StringBuilder();

                                if (user.getCreatedNotification() > 0)
                                    sb.append("{faw-gift} ").append(user.getCreatedNotification());

                                if (user.getWonNotification() > 0)
                                    sb.append(" {faw-trophy} ").append(user.getWonNotification());

                                if (user.getMessageNotification() > 0)
                                    sb.append(" {faw-envelope} ").append(user.getMessageNotification());

                                notifications.setText(sb.toString());
                                notifications.setVisibility(View.VISIBLE);
                            } else {
                                notifications.setText("{faw-envelope}");
                                notifications.setVisibility(View.VISIBLE);
                            }
                        } else {
                            notifications.setVisibility(View.GONE);
                        }
                    }
                })
                .build();
        drawer.getRecyclerView().setVerticalScrollBarEnabled(false);

        reconfigureNavBarItems();
    }

    /**
     * Rebuilds the list of all navigation items, in particular:
     * <ol>
     * <li>the header, indicating whether or not the user is logged in</li>
     * <li>list of accessible giveaway filters (new, group, wishlist, all)</li>
     * </ol>
     */
    private void reconfigureNavBarItems() {
        // Rebuild the header.
        accountHeader.clear();

        SteamGiftsUserData account = SteamGiftsUserData.getCurrent();
        if (account.isLoggedIn()) {
            profile = new ProfileDrawerItem().withName(account.getName()).withEmail("...").withIdentifier(1);

            if (account.getImageUrl() != null && !account.getImageUrl().isEmpty())
                profile.withIcon(account.getImageUrl());

            accountHeader.addProfile(profile, 0);
        } else {
            profile = new ProfileDrawerItem().withName(getString(R.string.guest)).withEmail("Not logged in").withIcon(R.drawable.default_avatar).withIdentifier(1);
            accountHeader.addProfile(profile, 0);
        }


        // Rebuild all items
        drawer.removeAllItems();

        // If we're not logged in, log in is the top.
        if (!account.isLoggedIn())
            drawer.addItem(new PrimaryDrawerItem().withName(R.string.login).withIdentifier(R.string.login).withSelectable(false).withIcon(FontAwesome.Icon.faw_sign_in));

        // All different giveaway views
        drawer.addItems(
                new SectionDrawerItem().withName(R.string.navigation_giveaways).withDivider(!account.isLoggedIn()),
                new PrimaryDrawerItem().withName(R.string.navigation_giveaways_all).withIdentifier(R.string.navigation_giveaways_all).withIcon(FontAwesome.Icon.faw_gift));

        // If we're logged in, we can look at group and wishlist giveaways.
        if (account.isLoggedIn()) {
            drawer.addItems(
                    new PrimaryDrawerItem().withName(R.string.navigation_giveaways_group).withIdentifier(R.string.navigation_giveaways_group).withIcon(FontAwesome.Icon.faw_users),
                    new PrimaryDrawerItem().withName(R.string.navigation_giveaways_wishlist).withIdentifier(R.string.navigation_giveaways_wishlist).withIcon(FontAwesome.Icon.faw_heart));
        }
        drawer.addItems(new PrimaryDrawerItem().withName(R.string.navigation_giveaways_new).withIdentifier(R.string.navigation_giveaways_new).withIcon(FontAwesome.Icon.faw_refresh));
        drawer.addItems(new PrimaryDrawerItem().withName(R.string.navigation_giveaways_saved).withIdentifier(R.string.navigation_giveaways_saved).withIcon(FontAwesome.Icon.faw_star));

        // Discussions, some time
        drawer.addItem(new SectionDrawerItem().withName(R.string.navigation_discussions).withDivider(true));
        for (DiscussionListFragment.Type type : DiscussionListFragment.Type.values()) {
            drawer.addItem(new PrimaryDrawerItem().withName(type.getNavbarResource()).withIdentifier(type.getNavbarResource()).withIcon(type.getIcon()));
        }

        drawer.addItems(new DividerDrawerItem());
        drawer.addItem(new PrimaryDrawerItem().withName(R.string.preferences).withIdentifier(R.string.preferences).withSelectable(false).withIcon(FontAwesome.Icon.faw_cog));

        // Provide a way to log out.
        if (account.isLoggedIn()) {
            drawer.addItem(new PrimaryDrawerItem().withName(R.string.logout).withIdentifier(R.string.logout).withSelectable(false).withIcon(FontAwesome.Icon.faw_sign_out));
        }

        drawer.addItem(new PrimaryDrawerItem().withName(R.string.navigation_help).withIdentifier(R.string.navigation_help).withSelectable(false).withIcon(FontAwesome.Icon.faw_question));
        drawer.addItem(new PrimaryDrawerItem().withName(R.string.navigation_about).withIdentifier(R.string.navigation_about).withSelectable(false).withIcon(FontAwesome.Icon.faw_info));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "Activity result for " + requestCode + " => " + resultCode);

        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == CommonActivity.RESPONSE_LOGIN_SUCCESSFUL) {
                    onAccountChange();
                    Snackbar.make(findViewById(R.id.swipeContainer), "Welcome, " + SteamGiftsUserData.getCurrent().getName() + "!", Snackbar.LENGTH_LONG).show();
                } else
                    Snackbar.make(findViewById(R.id.swipeContainer), "Login failed", Snackbar.LENGTH_LONG).show();
                break;

            case REQUEST_SETTINGS:
                Fragment fragment = getCurrentFragment();

                // force an entire fragment reload if this is something giveaway reloaded
                if (fragment instanceof GiveawayListFragment) {
                    loadFragment(GiveawayListFragment.newInstance(((GiveawayListFragment) fragment).getType(), null));
                } else if (fragment instanceof SavedGiveawaysFragment) {
                    loadFragment(new SavedGiveawaysFragment());
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onUpdatePoints(final int newPoints) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (SteamGiftsUserData.getCurrent().isLoggedIn() && getCurrentFragment() instanceof GiveawayListFragment) {
                actionBar.setSubtitle(String.format("%dP", newPoints));
            } else {
                actionBar.setSubtitle(null);
            }
        }
    }
}
