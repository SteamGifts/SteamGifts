package net.mabako.steamgifts.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.mikepenz.iconics.context.IconicsContextWrapper;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.util.WriteCommentListener;
import net.mabako.steamgifts.tasks.PostCommentTask;

public class DetailActivity extends BaseActivity implements WriteCommentListener {
    private static final String TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null if a fragment state is saved from a previous configuration.
        if (savedInstanceState == null) {
            loadFragment(GiveawayDetailFragment.newInstance((Giveaway) getIntent().getSerializableExtra(GiveawayDetailFragment.ARG_GIVEAWAY)));
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

    @Override
    public void submit(String giveawayId, String xsrfToken, String message) {
        new PostCommentTask(this, giveawayId, xsrfToken, message).execute();
    }
}