package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
import net.mabako.steamgifts.web.WebUserData;

/**
 * Common baseline for all activities with navbar.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final int REQUEST_LOGIN = 3;
    public static final String PREF_KEY_SESSION_ID = "session-id";
    public static final String PREF_ACCOUNT = "account";
    public static final String PREF_KEY_USERNAME = "username";
    public static final String PREF_KEY_IMAGE = "image-url";


    private AccountHeader accountHeader;
    private Drawer drawer;

    abstract void fetchItems(int page);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load session & username if possible
        SharedPreferences sp = getSharedPreferences(PREF_ACCOUNT, MODE_PRIVATE);
        if (sp.contains(PREF_KEY_SESSION_ID) && sp.contains(PREF_KEY_USERNAME)) {
            WebUserData.getCurrent().setSessionId(sp.getString(PREF_KEY_SESSION_ID, null));
            WebUserData.getCurrent().setName(sp.getString(PREF_KEY_USERNAME, null));
            WebUserData.getCurrent().setImageUrl(sp.getString(PREF_KEY_IMAGE, null));
        } else {
            WebUserData.clear();
        }
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
        spEditor.commit();
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
                .withHeaderBackground(R.drawable.header)
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
                                startActivityForResult(new Intent(BaseActivity.this, LoginActivity.class), REQUEST_LOGIN);
                                break;

                            case R.string.logout:
                                WebUserData.clear();
                                onAccountChange();

                                Snackbar.make(findViewById(R.id.swipeContainer), R.string.logged_out, Snackbar.LENGTH_SHORT).show();
                                break;

                            case R.string.navigation_giveaways_all:
                            case R.string.navigation_giveaways_group:
                            case R.string.navigation_giveaways_wishlist:
                            case R.string.navigation_giveaways_new:
                                Intent intent = new Intent(BaseActivity.this, GiveawaysActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                                intent.putExtra("type", GiveawaysActivity.Type.find(drawerItem.getIdentifier()));

                                startActivity(intent);
                                finish();

                                break;

                            default:
                                return false;
                        }

                        drawer.closeDrawer();
                        return true;
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
            ProfileDrawerItem profile = new ProfileDrawerItem().withName(account.getName()).withEmail(account.getSessionId());

            if (account.getImageUrl() != null && !account.getImageUrl().isEmpty())
                profile.withIcon(account.getImageUrl());

            accountHeader.addProfile(profile, 0);
        } else
            accountHeader.addProfile(new ProfileDrawerItem().withName(getString(R.string.guest)).withEmail("Not logged in").withIcon(R.drawable.guy), 0);


        // Rebuild all items
        drawer.removeAllItems();

        // If we're not logged in, log in is the top.
        if (!account.isLoggedIn())
            drawer.addItem(new PrimaryDrawerItem().withName(R.string.login).withIdentifier(R.string.login).withSelectable(false));

        // All different giveaway views
        drawer.addItems(
                new SectionDrawerItem().withName(R.string.navigation_giveaways).withDivider(!account.isLoggedIn()),
                new PrimaryDrawerItem().withName(R.string.navigation_giveaways_all).withIdentifier(R.string.navigation_giveaways_all));

        // If we're logged in, we can look at group and wishlist giveaways.
        if (account.isLoggedIn()) {
            drawer.addItems(
                    new PrimaryDrawerItem().withName(R.string.navigation_giveaways_group).withIdentifier(R.string.navigation_giveaways_group),
                    new PrimaryDrawerItem().withName(R.string.navigation_giveaways_wishlist).withIdentifier(R.string.navigation_giveaways_wishlist));
        }
        drawer.addItems(new PrimaryDrawerItem().withName(R.string.navigation_giveaways_new).withIdentifier(R.string.navigation_giveaways_new));

        // Discussions, some time
        // drawer.addItem(new SectionDrawerItem().withName(R.string.navigation_discussions));

        // Provide a way to log out.
        if (account.isLoggedIn()) {
            drawer.addItems(
                    new DividerDrawerItem(),
                    new PrimaryDrawerItem().withName(R.string.logout).withIdentifier(R.string.logout));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("BaseActivity", "Activity result for " + requestCode + " => " + resultCode);
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
