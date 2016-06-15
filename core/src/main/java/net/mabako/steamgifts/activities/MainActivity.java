package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import net.mabako.steamgifts.ApplicationTemplate;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.DiscussionListFragment;
import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.SavedFragment;
import net.mabako.steamgifts.fragments.TradeListFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.intro.IntroActivity;
import net.mabako.steamgifts.persistentdata.IPointUpdateNotification;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.tasks.LogoutTask;

import java.io.Serializable;

public class MainActivity extends CommonActivity implements IPointUpdateNotification {
    public static final String ARG_TYPE = "type";
    public static final String ARG_QUERY = "query";
    public static final String ARG_NO_DRAWER = "no-drawer";

    private Navbar navbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one_fragment);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        SteamGiftsUserData.addUpdateHandler(this);
        onUpdatePoints(SteamGiftsUserData.getCurrent(this).getPoints());

        boolean noDrawer = getIntent().getBooleanExtra(ARG_NO_DRAWER, false);
        if (!noDrawer)
            navbar = new Navbar(this);

        // savedInstanceState is non-null if a fragment state is saved from a previous configuration.
        if (savedInstanceState == null) {
            ((ApplicationTemplate) getApplication()).showBetaNotification(this, true);

            // Load a default fragment to show all giveaways
            Serializable type = getIntent().getSerializableExtra(ARG_TYPE);
            if (type == null)
                type = GiveawayListFragment.Type.ALL;

            String query = getIntent().getStringExtra(ARG_QUERY);


            if (type instanceof GiveawayListFragment.Type) {
                loadFragment(GiveawayListFragment.newInstance((GiveawayListFragment.Type) type, query, navbar == null));

                if (navbar != null)
                    navbar.setSelection(((GiveawayListFragment.Type) type).getNavbarResource());
            } else if (type instanceof DiscussionListFragment.Type) {
                loadFragment(DiscussionListFragment.newInstance((DiscussionListFragment.Type) type, null));

                if (navbar != null)
                    navbar.setSelection(((DiscussionListFragment.Type) type).getNavbarResource());
            } else if (type instanceof TradeListFragment.Type) {
                loadFragment(TradeListFragment.newInstance((TradeListFragment.Type) type, null));

                if (navbar != null)
                    navbar.setSelection(((TradeListFragment.Type) type).getNavbarResource());
            }
        } else {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof IActivityTitle) {
                updateTitle(fragment);
            }
        }

        IntroActivity.showIntroIfNeccessary(this, IntroActivity.INTRO_MAIN, IntroActivity.INTRO_MAIN_VERSION);
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
        navbar.reconfigure();

        super.onAccountChange();

        loadFragment(GiveawayListFragment.newInstance(GiveawayListFragment.Type.ALL, null, navbar == null));

        if (navbar != null)
            navbar.setSelection(R.string.navigation_giveaways_all);
    }

    @Override
    public void loadFragment(Fragment fragment) {
        super.loadFragment(fragment);
        onUpdatePoints(SteamGiftsUserData.getCurrent(this).getPoints());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN:
                if (resultCode == CommonActivity.RESPONSE_LOGIN_SUCCESSFUL) {
                    onAccountChange();
                    Snackbar.make(findViewById(R.id.swipeContainer), "Welcome, " + SteamGiftsUserData.getCurrent(this).getName() + "!", Snackbar.LENGTH_LONG).show();
                } else
                    Snackbar.make(findViewById(R.id.swipeContainer), "Login failed", Snackbar.LENGTH_LONG).show();
                break;

            case REQUEST_SETTINGS:
                if (resultCode == RESPONSE_LOGOUT) {
                    new LogoutTask(MainActivity.this, SteamGiftsUserData.getCurrent(this).getSessionId()).execute();

                    SteamGiftsUserData.clear();
                    onAccountChange();
                } else {
                    Fragment fragment = getCurrentFragment();

                    if (navbar != null)
                        navbar.reconfigure();

                    // force an entire fragment reload if this is something giveaway reloaded
                    if (fragment instanceof GiveawayListFragment) {
                        loadFragment(GiveawayListFragment.newInstance(((GiveawayListFragment) fragment).getType(), null, false));

                        if (navbar != null)
                            navbar.setSelection(((GiveawayListFragment) fragment).getType().getNavbarResource());
                    } else if (fragment instanceof SavedFragment) {
                        loadFragment(new SavedFragment());
                    }
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
            if (SteamGiftsUserData.getCurrent(this).isLoggedIn() && getCurrentFragment() instanceof GiveawayListFragment) {
                actionBar.setSubtitle(String.format("%dP", newPoints));
            } else {
                actionBar.setSubtitle(null);
            }
        }
    }
}
