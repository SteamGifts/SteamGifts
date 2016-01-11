package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import net.mabako.sgtools.SGToolsDetailFragment;
import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.BasicDiscussion;
import net.mabako.steamgifts.data.BasicGiveaway;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.fragments.profile.MessageListFragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DetailActivity extends CommonActivity {
    public static final String ARG_NOTIFICATIONS = "notifications";

    private SimplePagerAdapter pagerAdapter = null;
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
        // savedInstanceState is non-null if a fragment state is saved from a previous configuration.
        if (savedInstanceState == null) {
            Serializable serializable = getIntent().getSerializableExtra(GiveawayDetailFragment.ARG_GIVEAWAY);
            if (serializable != null) {
                setContentView(R.layout.activity_giveaway_detail);
                loadPagedFragments(GiveawayDetailFragment.newInstance((BasicGiveaway) serializable));
                return;
            }

            serializable = getIntent().getSerializableExtra(DiscussionDetailFragment.ARG_DISCUSSION);
            if (serializable != null) {
                setContentView(R.layout.activity_one_fragment);
                loadFragment(DiscussionDetailFragment.newInstance((BasicDiscussion) serializable));
                return;
            }

            String user = getIntent().getStringExtra(UserDetailFragment.ARG_USER);
            if (user != null) {
                setContentView(R.layout.activity_one_fragment);
                loadFragment(UserDetailFragment.newInstance(user));
                return;
            }

            serializable = getIntent().getSerializableExtra(SGToolsDetailFragment.ARG_UUID);
            if (serializable != null) {
                setContentView(R.layout.activity_detail_expanding_toolbar);
                loadFragment(SGToolsDetailFragment.newInstance((UUID) serializable));
                return;
            }

            if (getIntent().hasExtra(ARG_NOTIFICATIONS)) {
                setContentView(R.layout.activity_paged_fragments);
                loadPagedFragments(new MessageListFragment());
                return;
            }

            throw new IllegalStateException("no detail activity");
        }
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

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Initialize the {@link ViewPager} adapter.
     *
     * @param fragments
     */
    private void loadPagedFragments(Fragment... fragments) {
        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);

        pagerAdapter = new SimplePagerAdapter(getSupportFragmentManager(), fragments);
        pager.setAdapter(pagerAdapter);

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
     * Simple fragment adapter that basically just holds a list of... fragments, without any fancy schmuck.
     */
    private class SimplePagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments = new ArrayList<>();

        public SimplePagerAdapter(FragmentManager fm, Fragment... fragments) {
            super(fm);
            this.fragments.addAll(Arrays.asList(fragments));
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return getFragmentTitle(getItem(position));
        }

        public void add(Fragment fragment) {
            fragments.add(fragment);
            notifyDataSetChanged();
        }
    }
}