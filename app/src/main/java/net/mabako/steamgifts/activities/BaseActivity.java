package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.web.WebUserData;

/**
 * Common baseline for all activities with navbar.
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final int REQUEST_LOGIN = 3;
    private Drawer drawer;

    abstract void fetchItems(int page);

    public void onAccountChange() {
        addNavBarItems();
    }

    protected void addNavBarItems() {
        drawer.removeHeader();
        drawer.removeAllItems();

        // Account?
        WebUserData account = WebUserData.getCurrent();
        if (account.isLoggedIn()) {
            // Add a fancy header
        } else
            drawer.addItem(new PrimaryDrawerItem().withName("Log In").withIdentifier(R.string.title_activity_login).withSelectable(false));

        // All different giveaway views
        drawer.addItems(
                new SectionDrawerItem().withName(R.string.navigation_giveaways),
                new PrimaryDrawerItem().withName(R.string.navigation_giveaways_all).withSetSelected(true).withIdentifier(R.string.navigation_giveaways_all));

        // If we're logged in, we can look at group and wishlist giveaways.
        if (account.isLoggedIn()) {
            drawer.addItems(
                    new PrimaryDrawerItem().withName(R.string.navigation_giveaways_group).withIdentifier(R.string.navigation_giveaways_group),
                    new PrimaryDrawerItem().withName(R.string.navigation_giveaways_wishlist).withIdentifier(R.string.navigation_giveaways_wishlist)
            );
        }
        drawer.addItems(new PrimaryDrawerItem().withName(R.string.navigation_giveaways_new).withIdentifier(R.string.navigation_giveaways_new));

        // Discussions, some time
        // drawer.addItem(new SectionDrawerItem().withName(R.string.navigation_discussions));
    }

    protected void setupNavBar() {
        /*
        AccountHeader header = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.header)
                .addProfiles(
                        new ProfileDrawerItem().withName("kt").withEmail("Level 9 Wizard").withIcon(R.drawable.guy)
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean current) {
                        return false;
                    }
                })
                .build();
        */
        DrawerBuilder db = new DrawerBuilder()
                .withActivity(this)
                .withToolbar((Toolbar) findViewById(R.id.toolbar))
                .withTranslucentStatusBar(false)
                .withActionBarDrawerToggle(true);
        // .withAccountHeader(header)

        drawer = db
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        switch (drawerItem.getIdentifier()) {
                            case R.string.title_activity_login:
                                startActivityForResult(new Intent(BaseActivity.this, LoginActivity.class), REQUEST_LOGIN);
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

        addNavBarItems();
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
