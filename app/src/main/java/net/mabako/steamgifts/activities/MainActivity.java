package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.BuildConfig;
import net.mabako.steamgifts.R;
import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.IGiveawayUpdateNotification;
import net.mabako.steamgifts.tasks.LogoutTask;
import net.mabako.steamgifts.web.WebUserData;

public class MainActivity extends BaseActivity implements IGiveawayUpdateNotification {
    public static final String ARGS_QUERY = "query";

    private static final String TAG = MainActivity.class.getSimpleName();

    private AccountHeader accountHeader;
    private Drawer drawer;
    private ProfileDrawerItem profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        setupNavBar();

        // savedInstanceState is non-null if a fragment state is saved from a previous configuration.
        if (savedInstanceState == null) {
            // Load a default fragment to show all giveaways
            loadFragment(GiveawayListFragment.newInstance(GiveawayListFragment.Type.ALL, getIntent().getStringExtra(ARGS_QUERY)));
            drawer.setSelection(R.string.navigation_giveaways_all, false);
        }
    }

    /**
     * Triggered upon the user logging in or logging out.
     */
    public void onAccountChange() {
        // Reconfigure our navigation bar items.
        reconfigureNavBarItems();

        super.onAccountChange();

        loadFragment(GiveawayListFragment.newInstance(GiveawayListFragment.Type.ALL, getIntent().getExtras() != null ? getIntent().getExtras().getString(ARGS_QUERY, null) : null));
        drawer.setSelection(R.string.navigation_giveaways_all, false);
    }

    private void setupNavBar() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(R.drawable.guy).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });

        // Account?
        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(new ColorDrawable(ContextCompat.getColor(this, R.color.colorPrimary)))
                .withSelectionListEnabledForSingleProfile(false)
                .build();

        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar((Toolbar) findViewById(R.id.toolbar))
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true)
                .withAccountHeader(accountHeader)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (drawerItem.getIdentifier()) {
                            case R.string.login:
                                requestLogin();
                                break;

                            case R.string.logout:
                                new LogoutTask(MainActivity.this, WebUserData.getCurrent().getSessionId()).execute();

                                WebUserData.clear();
                                onAccountChange();
                                break;

                            case R.string.feedback:
                                String email = "lizacarvelli+steamgifts+" + BuildConfig.VERSION_NAME + "+" + BuildConfig.VERSION_CODE + "@gmail.com";
                                Log.v(TAG, "Email to " + email);

                                Intent intent = new Intent(Intent.ACTION_SENDTO);
                                intent.setType("text/plain");
                                intent.setData(Uri.parse("mailto:"));
                                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                                intent.putExtra(Intent.EXTRA_SUBJECT, "SteamGifts for Android Feedback");
                                try {
                                    startActivity(intent);
                                } catch (android.content.ActivityNotFoundException ex) {
                                    Snackbar.make(findViewById(R.id.swipeContainer), "No mail clients installed", Snackbar.LENGTH_LONG);
                                }
                                break;

                            case R.string.navigation_giveaways_all:
                            case R.string.navigation_giveaways_group:
                            case R.string.navigation_giveaways_wishlist:
                            case R.string.navigation_giveaways_new:
                                loadFragment(GiveawayListFragment.newInstance(GiveawayListFragment.Type.find(drawerItem.getIdentifier()), getIntent().getExtras() != null ? getIntent().getExtras().getString(ARGS_QUERY, null) : null));
                                break;

                            default:
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
                        // Are we even logged in?
                        if (WebUserData.getCurrent().isLoggedIn()) {
                            // Format the string
                            String newInfo = String.format(getString(R.string.drawer_profile_info), WebUserData.getCurrent().getLevel(), WebUserData.getCurrent().getPoints());

                            // Is this still up-to-date?
                            if (!newInfo.equals(profile.getEmail().toString())) {
                                profile.withEmail(newInfo);
                                accountHeader.updateProfile(profile);
                            }
                        }
                    }
                })
                .build();

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

        WebUserData account = WebUserData.getCurrent();
        if (account.isLoggedIn()) {
            profile = new ProfileDrawerItem().withName(account.getName()).withEmail(account.getSessionId()).withIdentifier(1);

            if (account.getImageUrl() != null && !account.getImageUrl().isEmpty())
                profile.withIcon(account.getImageUrl());

            accountHeader.addProfile(profile, 0);
        } else {
            profile = new ProfileDrawerItem().withName(getString(R.string.guest)).withEmail("Not logged in").withIcon(R.drawable.guy).withIdentifier(1);
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

        // Discussions, some time
        // drawer.addItem(new SectionDrawerItem().withName(R.string.navigation_discussions));


        drawer.addItems(new DividerDrawerItem());
        // Feedback
        drawer.addItem(new PrimaryDrawerItem().withName(R.string.feedback).withIdentifier(R.string.feedback).withSelectable(false).withIcon(FontAwesome.Icon.faw_comment));

        // Provide a way to log out.
        if (account.isLoggedIn()) {
            drawer.addItem(new PrimaryDrawerItem().withName(R.string.logout).withIdentifier(R.string.logout).withSelectable(false).withIcon(FontAwesome.Icon.faw_sign_out));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "Activity result for " + requestCode + " => " + resultCode);
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == BaseActivity.RESPONSE_LOGIN_SUCCESSFUL) {
                    onAccountChange();
                    Snackbar.make(findViewById(R.id.swipeContainer), "Welcome, " + WebUserData.getCurrent().getName() + "!", Snackbar.LENGTH_LONG).show();
                } else
                    Snackbar.make(findViewById(R.id.swipeContainer), "Login failed", Snackbar.LENGTH_LONG).show();
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onUpdateGiveawayStatus(@NonNull String giveawayId, boolean entered) {
        Fragment fragment = getCurrentFragment();
        if (fragment instanceof IGiveawayUpdateNotification) {
            ((IGiveawayUpdateNotification) fragment).onUpdateGiveawayStatus(giveawayId, entered);
        }
    }
}
