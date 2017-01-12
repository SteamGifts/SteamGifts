package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.holder.StringHolder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.DiscussionListFragment;
import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.SavedFragment;
import net.mabako.steamgifts.fragments.SearchableListFragment;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.intro.IntroActivity;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.receivers.AbstractNotificationCheckReceiver;

public class Navbar {
    private final CommonActivity activity;

    private AccountHeader accountHeader;

    @NonNull
    private Drawer drawer;

    private CustomProfileDrawerItem profile;

    public Navbar(final CommonActivity activity) {
        this.activity = activity;

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
        TypedArray ta = activity.getTheme().obtainStyledAttributes(attrs);

        // Account?
        accountHeader = new CustomAccountHeaderBuilder()
                .withActivity(activity)
                .withCompactStyle(true)
                .withHeaderBackground(ta.getDrawable(0))
                .withSelectionListEnabledForSingleProfile(false)
                .withOnAccountHeaderProfileImageListener(new AccountHeader.OnAccountHeaderProfileImageListener() {
                    @Override
                    public boolean onProfileImageClick(View view, IProfile profile, boolean current) {
                        if (SteamGiftsUserData.getCurrent(activity).isLoggedIn()) {
                            Intent intent = new Intent(activity, DetailActivity.class);
                            intent.putExtra(UserDetailFragment.ARG_USER, SteamGiftsUserData.getCurrent(activity).getName());
                            activity.startActivity(intent);
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

        drawer = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar((Toolbar) activity.findViewById(R.id.toolbar))
                .withTranslucentStatusBar(true)
                .withActionBarDrawerToggle(true)
                .withAccountHeader(accountHeader)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // Stop searching, if any is done
                        Fragment fragment = activity.getCurrentFragment();
                        if (fragment instanceof SearchableListFragment)
                            ((SearchableListFragment) fragment).stopSearch();

                        long identifier = drawerItem.getIdentifier();
                        if (identifier == R.string.login) {
                            activity.requestLogin();

                        } else if (identifier == R.string.navigation_help) {
                            IntroActivity.showIntro(activity, IntroActivity.INTRO_MAIN);

                        } else if (identifier == R.string.navigation_about) {
                            activity.startActivity(new Intent(activity, AboutActivity.class));

                        } else if (identifier == R.string.preferences) {
                            activity.startActivityForResult(new Intent(activity, SettingsActivity.class), CommonActivity.REQUEST_SETTINGS);

                        } else if (identifier == R.string.navigation_saved_elements) {
                            activity.loadFragment(new SavedFragment());
                            ActionBar actionBar = activity.getSupportActionBar();
                            if (actionBar != null)
                                actionBar.setSubtitle(null);
                        } else {
                            for (GiveawayListFragment.Type type : GiveawayListFragment.Type.values()) {
                                if (type.getNavbarResource() == identifier) {
                                    activity.loadFragment(GiveawayListFragment.newInstance(type, null, false));
                                    break;
                                }
                            }

                            for (DiscussionListFragment.Type type : DiscussionListFragment.Type.values()) {
                                if (type.getNavbarResource() == identifier) {
                                    activity.loadFragment(DiscussionListFragment.newInstance(type, null));
                                    ActionBar actionBar = activity.getSupportActionBar();
                                    if (actionBar != null)
                                        actionBar.setSubtitle(null);
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
                        SteamGiftsUserData user = SteamGiftsUserData.getCurrent(activity);
                        if (user.isLoggedIn()) {
                            // Format the string
                            String newInfo = String.format(activity.getString(R.string.drawer_profile_info), user.getLevel(), user.getPoints());

                            // Is this still up-to-date?
                            if (!newInfo.equals(profile.getEmail().toString())) {
                                profile.withEmail(newInfo);
                            }

                            if (user.hasNotifications()) {
                                StringBuilder sb = new StringBuilder();

                                if (user.getCreatedNotification() > 0)
                                    sb.append("{faw-gift} ").append(user.getCreatedNotification());

                                if (user.getWonNotification() > 0)
                                    sb.append(" {faw-trophy} ").append(user.getWonNotification());

                                if (user.getMessageNotification() > 0)
                                    sb.append(" {faw-envelope} ").append(user.getMessageNotification());

                                profile.withNotifications(sb.toString());
                            } else {
                                profile.withNotifications("{faw-envelope}");
                            }
                        } else {
                            profile.withNotifications(null);
                        }
                        accountHeader.updateProfile(profile);
                    }
                })
                .build();
        drawer.getRecyclerView().setVerticalScrollBarEnabled(false);

        reconfigure();
    }

    /**
     * Rebuilds the list of all navigation items, in particular:
     * <ol>
     * <li>the header, indicating whether or not the user is logged in</li>
     * <li>list of accessible giveaway filters (new, group, wishlist, all)</li>
     * </ol>
     */
    public void reconfigure() {
        // Rebuild the header.
        accountHeader.clear();

        // Update the account header.
        SteamGiftsUserData account = SteamGiftsUserData.getCurrent(activity);
        if (account.isLoggedIn()) {
            profile = (CustomProfileDrawerItem) new CustomProfileDrawerItem().withName(account.getName()).withEmail("...").withIdentifier(1);

            if (account.getImageUrl() != null && !account.getImageUrl().isEmpty())
                profile.withIcon(account.getImageUrl());

            accountHeader.addProfile(profile, 0);
        } else {
            profile = (CustomProfileDrawerItem) new CustomProfileDrawerItem().withName(activity.getString(R.string.guest)).withEmail("Not logged in").withIcon(R.drawable.default_avatar).withIdentifier(1);
            accountHeader.addProfile(profile, 0);
        }


        // Rebuild all items
        drawer.removeAllItems();

        // If we're not logged in, log in is the top.
        if (!account.isLoggedIn())
            drawer.addItem(new PrimaryDrawerItem().withName(R.string.login).withIdentifier(R.string.login).withSelectable(false).withIcon(FontAwesome.Icon.faw_sign_in));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String mode = sharedPreferences.getString("preference_sidebar_discussion_list", "full");

        addGiveawayItems(account);

        if ("compact".equals(mode))
            drawer.addItem(new DividerDrawerItem());
        addDiscussionItems(account, mode);

        drawer.addItems(new DividerDrawerItem());
        drawer.addItem(new PrimaryDrawerItem().withName(R.string.navigation_saved_elements).withIdentifier(R.string.navigation_saved_elements).withIcon(FontAwesome.Icon.faw_star));
        drawer.addItem(new PrimaryDrawerItem().withName(R.string.preferences).withIdentifier(R.string.preferences).withSelectable(false).withIcon(FontAwesome.Icon.faw_cog));
        drawer.addItem(new PrimaryDrawerItem().withName(R.string.navigation_help).withIdentifier(R.string.navigation_help).withSelectable(false).withIcon(FontAwesome.Icon.faw_question));
        drawer.addItem(new PrimaryDrawerItem().withName(R.string.navigation_about).withIdentifier(R.string.navigation_about).withSelectable(false).withIcon(FontAwesome.Icon.faw_info));
    }

    /**
     * Add all Navbar icons related to giveaways.
     */
    private void addGiveawayItems(SteamGiftsUserData account) {
        // All different giveaway views
        drawer.addItems(
                new SectionDrawerItem().withName(R.string.navigation_giveaways).withDivider(!account.isLoggedIn()),
                new PrimaryDrawerItem().withName(R.string.navigation_giveaways_all).withIdentifier(R.string.navigation_giveaways_all).withIcon(FontAwesome.Icon.faw_gift));

        // If we're logged in, we can look at group and wishlist giveaways.
        if (account.isLoggedIn()) {
            drawer.addItems(
                    new PrimaryDrawerItem().withName(R.string.navigation_giveaways_group).withIdentifier(R.string.navigation_giveaways_group).withIcon(FontAwesome.Icon.faw_users),
                    new PrimaryDrawerItem().withName(R.string.navigation_giveaways_wishlist).withIdentifier(R.string.navigation_giveaways_wishlist).withIcon(FontAwesome.Icon.faw_heart),
                    new PrimaryDrawerItem().withName(R.string.navigation_giveaways_recommended).withIdentifier(R.string.navigation_giveaways_recommended).withIcon(FontAwesome.Icon.faw_thumbs_up));
        }
        drawer.addItems(new PrimaryDrawerItem().withName(R.string.navigation_giveaways_new).withIdentifier(R.string.navigation_giveaways_new).withIcon(FontAwesome.Icon.faw_refresh));
    }

    /**
     * Add all discussion-related items.
     */
    private void addDiscussionItems(SteamGiftsUserData account, String mode) {
        if ("full".equals(mode)) {
            // Full mode: Show all different categories in the navbar
            drawer.addItem(new SectionDrawerItem().withName(R.string.navigation_discussions).withDivider(true));
            for (DiscussionListFragment.Type type : DiscussionListFragment.Type.values()) {
                // We only want to have 'Created Discussions' if we're actually logged in.
                if (type == DiscussionListFragment.Type.CREATED && !account.isLoggedIn())
                    continue;

                drawer.addItem(new PrimaryDrawerItem().withName(type.getNavbarResource()).withIdentifier(type.getNavbarResource()).withIcon(type.getIcon()));
            }
        } else if ("compact".equals(mode)) {
            // Compact mode: we only add a single item called 'Discussions' that links to all discussions,
            // and there's a menu item in the 'discussions' list to switch between different categories.
            drawer.addItem(new PrimaryDrawerItem().withName(R.string.navigation_discussions).withIdentifier(DiscussionListFragment.Type.ALL.getNavbarResource()).withIcon(DiscussionListFragment.Type.ALL.getIcon()));
        }
    }

    public void setSelection(@StringRes int resourceId) {
        drawer.setSelection(resourceId, false);
    }

    /**
     * Make sure we initialize notification information whenever our profile display is rebuilt.
     */
    public class CustomAccountHeaderBuilder extends AccountHeaderBuilder {
        @Override
        protected void buildProfiles() {
            super.buildProfiles();

            TextView notificationText = (TextView) mAccountHeader.findViewById(R.id.material_drawer_account_header_notifications);
            CustomProfileDrawerItem profile = (CustomProfileDrawerItem) mCurrentProfile;

            if(profile != null) {
                if (profile.notifications == null) {
                    notificationText.setText("");
                    notificationText.setOnClickListener(null);
                } else {
                    profile.notifications.applyTo(notificationText);
                    notificationText.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (SteamGiftsUserData.getCurrent(activity).isLoggedIn()) {
                                Intent intent = new Intent(activity, DetailActivity.class);
                                intent.putExtra(DetailActivity.ARG_NOTIFICATIONS, AbstractNotificationCheckReceiver.NotificationId.NO_TYPE);
                                activity.startActivity(intent);
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * A custom profile that supports notifications text as an extra property.
     */
    public class CustomProfileDrawerItem extends ProfileDrawerItem {
        protected StringHolder notifications;

        public ProfileDrawerItem withNotifications(String notifications) {
            this.notifications = new StringHolder(notifications);
            return this;
        }

        public StringHolder getNotifications() {
            return notifications;
        }
    }
}
