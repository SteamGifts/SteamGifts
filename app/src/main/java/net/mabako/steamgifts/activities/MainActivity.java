package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.fragments.GiveawaysFragment;
import net.mabako.steamgifts.fragments.IFragmentNotifications;
import net.mabako.steamgifts.tasks.LogoutTask;
import net.mabako.steamgifts.web.WebUserData;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_LOGIN = 3;
    public static final String PREF_KEY_SESSION_ID = "session-id";
    public static final String PREF_ACCOUNT = "account";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_IMAGE = "image-url";

    public static final String FRAGMENT_TAG = "Main Fragment Thing";
    private static final String TAG = MainActivity.class.getSimpleName();

    private AccountHeader accountHeader;
    private Drawer drawer;
    private ProfileDrawerItem profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load session & username if possible
        SharedPreferences sp = getSharedPreferences(PREF_ACCOUNT, MODE_PRIVATE);
        if (sp.contains(PREF_KEY_SESSION_ID) && sp.contains(PREF_KEY_USERNAME)) {
            WebUserData.getCurrent().setSessionId(sp.getString(PREF_KEY_SESSION_ID, null));
            WebUserData.getCurrent().setName(sp.getString(PREF_KEY_USERNAME, null));
            WebUserData.getCurrent().setImageUrl(sp.getString(PREF_KEY_IMAGE, null));
        } else {
            WebUserData.clear();
        }

        // Need to have this prior to loading a fragment, otherwise no title is shown.
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        setupNavBar();

        // savedInstanceState is non-null if a fragment state is saved from a previous configuration.
        if(savedInstanceState == null) {
            // Load a default fragment to show all giveaways
            loadFragment(GiveawaysFragment.newInstance(GiveawaysFragment.Type.ALL));
            drawer.setSelection(R.string.navigation_giveaways_all, false);
        }
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.fragment_container, fragment, FRAGMENT_TAG);

        ft.commitAllowingStateLoss();

        // Update the title.
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            Log.v(TAG, "Current Fragment is a " + fragment.getClass().getName());
            if(fragment instanceof IFragmentNotifications) {
                int resource = ((IFragmentNotifications) fragment).getTitleResource();
                actionBar.setTitle(resource);

                Log.v(TAG, "Setting Toolbar title to " + getString(resource));
            }
            else
                actionBar.setTitle(R.string.app_name);
        }
    }

    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    /**
     * Triggered upon the user logging in or logging out.
     */
    public void onAccountChange() {
        // Reconfigure our navigation bar items.
        reconfigureNavBarItems();

        // Persist all relevant data.
        SharedPreferences.Editor spEditor = getSharedPreferences(PREF_ACCOUNT, MODE_PRIVATE).edit();

        WebUserData account = WebUserData.getCurrent();
        if (account.isLoggedIn()) {
            spEditor.putString(PREF_KEY_SESSION_ID, account.getSessionId());
            spEditor.putString(PREF_KEY_USERNAME, account.getName());
            spEditor.putString(PREF_KEY_IMAGE, account.getImageUrl());
        } else {
            spEditor.remove(PREF_KEY_SESSION_ID);
            spEditor.remove(PREF_KEY_USERNAME);
            spEditor.remove(PREF_KEY_IMAGE);
        }
        spEditor.apply();

        loadFragment(GiveawaysFragment.newInstance(GiveawaysFragment.Type.ALL));
        drawer.setSelection(R.string.navigation_giveaways_all, false);
    }

    protected void setupNavBar() {
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
                                startActivityForResult(new Intent(MainActivity.this, LoginActivity.class), REQUEST_LOGIN);
                                break;

                            case R.string.logout:
                                new LogoutTask(MainActivity.this, WebUserData.getCurrent().getSessionId()).execute();

                                WebUserData.clear();
                                onAccountChange();
                                break;

                            case R.string.navigation_giveaways_all:
                            case R.string.navigation_giveaways_group:
                            case R.string.navigation_giveaways_wishlist:
                            case R.string.navigation_giveaways_new:
                                loadFragment(GiveawaysFragment.newInstance(GiveawaysFragment.Type.find(drawerItem.getIdentifier())));
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
    protected void reconfigureNavBarItems() {
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

        // Provide a way to log out.
        if (account.isLoggedIn()) {
            drawer.addItems(
                    new DividerDrawerItem(),
                    new PrimaryDrawerItem().withName(R.string.logout).withIdentifier(R.string.logout).withSelectable(false).withIcon(FontAwesome.Icon.faw_sign_out));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v(TAG, "Activity result for " + requestCode + " => " + resultCode);
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == 1 && WebUserData.getCurrent().isLoggedIn()) {
                    onAccountChange();
                    Snackbar.make(findViewById(R.id.swipeContainer), "Welcome, " + WebUserData.getCurrent().getName() + "!", Snackbar.LENGTH_LONG).show();
                } else
                    Snackbar.make(findViewById(R.id.swipeContainer), "Login failed", Snackbar.LENGTH_LONG).show();
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
