package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import net.mabako.sgtools.SGToolsDetailFragment;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicDiscussion;
import net.mabako.steamgifts.data.BasicGiveaway;
import net.mabako.steamgifts.fragments.DetailFragment;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.fragments.FragmentAdapter;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.fragments.profile.CreatedListFragment;
import net.mabako.steamgifts.fragments.profile.EnteredListFragment;
import net.mabako.steamgifts.fragments.profile.MessageListFragment;
import net.mabako.steamgifts.fragments.profile.WonListFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.receivers.CheckForNewMessages;

import java.io.Serializable;
import java.util.UUID;

public class DetailActivity extends CommonActivity {
    public static final String ARG_NOTIFICATIONS = "notifications";

    /**
     * If we have a {@link net.mabako.steamgifts.fragments.DetailFragment.CommentContextInfo} instance, mark the comment associated with this instance as read.
     */
    public static final String ARG_MARK_CONTEXT_READ = "mark-context-read";

    private ViewPager pager = null;
    private FragmentAdapter pagerAdapter = null;
    private TabLayout tabLayout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initLayout(savedInstanceState);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initLayout(Bundle savedInstanceState) {
        DetailFragment.CommentContextInfo commentContext = (DetailFragment.CommentContextInfo) getIntent().getSerializableExtra(DetailFragment.ARG_COMMENT_CONTEXT);

        // Were we requested to mark this comment as read? This is the case if we click on a notification for a single comment.
        if (commentContext != null && getIntent().hasExtra(ARG_MARK_CONTEXT_READ))
            CheckForNewMessages.setLastDismissedCommentId(this, commentContext.getCommentId());

        Serializable serializable = getIntent().getSerializableExtra(GiveawayDetailFragment.ARG_GIVEAWAY);
        if (serializable != null) {
            String pref = PreferenceManager.getDefaultSharedPreferences(this).getString("preference_giveaway_load_images", "details;list");
            setContentView(pref.contains("details") ? R.layout.activity_giveaway_detail : R.layout.activity_paged_fragments_no_tabs);
            loadPagedFragments(GiveawayDetailFragment.newInstance((BasicGiveaway) serializable, commentContext));
            return;
        }

        serializable = getIntent().getSerializableExtra(DiscussionDetailFragment.ARG_DISCUSSION);
        if (serializable != null) {
            setContentView(R.layout.activity_one_fragment);
            if (savedInstanceState == null)
                loadFragment(DiscussionDetailFragment.newInstance((BasicDiscussion) serializable, commentContext));
            return;
        }

        String user = getIntent().getStringExtra(UserDetailFragment.ARG_USER);
        if (user != null) {
            setContentView(R.layout.activity_one_fragment);
            if (savedInstanceState == null)
                loadFragment(UserDetailFragment.newInstance(user));
            return;
        }

        serializable = getIntent().getSerializableExtra(SGToolsDetailFragment.ARG_UUID);
        if (serializable != null) {
            setContentView(R.layout.activity_detail_expanding_toolbar);
            if (savedInstanceState == null)
                loadFragment(SGToolsDetailFragment.newInstance((UUID) serializable));
            return;
        }

        if (getIntent().hasExtra(ARG_NOTIFICATIONS)) {
            setContentView(R.layout.activity_paged_fragments);
            loadPagedFragments(new MessageListFragment(), new WonListFragment(), new EnteredListFragment(), new CreatedListFragment());

            // Depending on what notifications are currently shown, bring the relevant tab up first.
            SteamGiftsUserData u = SteamGiftsUserData.getCurrent(this);
            if (u.getWonNotification() > 0)
                pager.setCurrentItem(1);
            else if (u.getMessageNotification() > 0)
                pager.setCurrentItem(0);
            else if (u.getCreatedNotification() > 0)
                pager.setCurrentItem(3);
            return;
        }

        throw new IllegalStateException("no detail activity");
    }

    @Override
    protected void onAccountChange() {
        super.onAccountChange();

        if (getCurrentFragment() instanceof GiveawayDetailFragment)
            ((GiveawayDetailFragment) getCurrentFragment()).reload();
    }

    @Override
    public String getNestingStringForHomePressed() {
        return getCurrentFragment() == null ? "unknown" : getCurrentFragment().getClass().getSimpleName();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == WriteCommentActivity.REQUEST_COMMENT) {
            if (resultCode == WriteCommentActivity.COMMENT_SENT) {
                Toast.makeText(this, R.string.comment_sent, Toast.LENGTH_SHORT).show();
            } else if (resultCode == WriteCommentActivity.COMMENT_NOT_SENT) {
                Toast.makeText(this, R.string.comment_not_sent, Toast.LENGTH_SHORT).show();
            }
        }

        if (data != null && data.hasExtra(CLOSE_NESTED) && getNestingStringForHomePressed().equals(data.getStringExtra(CLOSE_NESTED))) {
            Intent newData = new Intent();
            newData.putExtra(CLOSE_NESTED, getNestingStringForHomePressed());

            setResult(0, newData);
            finish();
        }

        if (requestCode == REQUEST_LOGIN_SGTOOLS && getCurrentFragment() instanceof SGToolsDetailFragment) {
            if (resultCode == RESPONSE_LOGIN_SGTOOLS_SUCCESSFUL) {
                // reload, basically.
                Serializable serializable = getIntent().getSerializableExtra(SGToolsDetailFragment.ARG_UUID);
                loadFragment(SGToolsDetailFragment.newInstance((UUID) serializable));
            } else {
                Toast.makeText(this, "Could not log in to sgtools.info", Toast.LENGTH_LONG).show();
                finish();
            }
        }

        if (requestCode == REQUEST_SYNC && getCurrentFragment() instanceof GiveawayDetailFragment) {
            if (resultCode == RESPONSE_SYNC_SUCCESSFUL) {
                // let's reload
                ((GiveawayDetailFragment) getCurrentFragment()).reload();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Initialize the {@link ViewPager} adapter.
     *
     * @param fragments
     */
    private void loadPagedFragments(Fragment... fragments) {
        pager = (ViewPager) findViewById(R.id.viewPager);

        pagerAdapter = new TitledPagerAdapter(this, pager, fragments);
        pager.setAdapter(pagerAdapter);
        pager.addOnPageChangeListener(pagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        if (tabLayout != null) {
            tabLayout.setupWithViewPager(pager);
        }
    }

    /**
     * Returns the first fragment if this is page-able, otherwise the only fragment.
     *
     * @return the fragment
     */
    @Override
    public Fragment getCurrentFragment() {
        return pagerAdapter == null ? super.getCurrentFragment() : pagerAdapter.getItem(0);
    }

    public void addFragment(Fragment fragment) {
        if (pagerAdapter == null)
            throw new IllegalStateException("not a paged view");

        pagerAdapter.add(fragment);
    }

    /**
     * Adds a fragment that is removed as soon as it is swiped away.
     *
     * @param transientFragment the fragment to set
     */
    public void setTransientFragment(Fragment transientFragment) {
        if (pagerAdapter == null)
            throw new IllegalStateException("not a paged view");

        pagerAdapter.setTransientFragment(transientFragment);
        pager.setCurrentItem(pagerAdapter.getCount() - 1, true);
    }

    public void addFragmentUnlessExists(Fragment fragment) {
        if (pagerAdapter == null)
            throw new IllegalStateException("not a paged view");

        for (Fragment f : pagerAdapter.getItems())
            if (fragment.getClass().equals(f.getClass()))
                return;

        addFragment(fragment);
    }

    /**
     * Pager adapter that uses the fragment's title for a page title
     */
    private class TitledPagerAdapter extends FragmentAdapter {
        public TitledPagerAdapter(AppCompatActivity activity, ViewPager viewPager, Fragment... fragments) {
            super(activity, viewPager, fragments);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getFragmentTitle(getItem(position));
        }
    }
}