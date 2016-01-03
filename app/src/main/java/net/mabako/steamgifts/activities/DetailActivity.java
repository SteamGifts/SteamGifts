package net.mabako.steamgifts.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.mikepenz.iconics.context.IconicsContextWrapper;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;

import java.io.Serializable;

public class DetailActivity extends BaseActivity {
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
                loadFragment(GiveawayDetailFragment.newInstance((Giveaway) serializable));
                return;
            }

            serializable = getIntent().getSerializableExtra(DiscussionDetailFragment.ARG_DISCUSSION);
            if (serializable != null) {
                setContentView(R.layout.activity_discussion_detail);
                loadFragment(DiscussionDetailFragment.newInstance((Discussion) serializable));
                return;
            }
        }
    }

    /**
     * Handle "Up" navigation.
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Allow icons to be used in {@link android.widget.TextView}
     *
     * @param newBase
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(IconicsContextWrapper.wrap(newBase));
    }

    @Override
    protected void onAccountChange() {
        super.onAccountChange();
        ((GiveawayDetailFragment) getCurrentFragment()).reload();
    }
}