package net.mabako.steamgifts.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.mikepenz.iconics.context.IconicsContextWrapper;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.BasicDiscussion;
import net.mabako.steamgifts.data.BasicGiveaway;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.UserDetailFragment;

import java.io.Serializable;

public class DetailActivity extends CommonActivity {
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
                loadFragment(GiveawayDetailFragment.newInstance((BasicGiveaway) serializable));
                return;
            }

            serializable = getIntent().getSerializableExtra(DiscussionDetailFragment.ARG_DISCUSSION);
            if (serializable != null) {
                setContentView(R.layout.activity_detail);
                loadFragment(DiscussionDetailFragment.newInstance((BasicDiscussion) serializable));
                return;
            }

            String user = getIntent().getStringExtra(UserDetailFragment.ARG_USER);
            if (user != null) {
                setContentView(R.layout.activity_detail);
                loadFragment(UserDetailFragment.newInstance(user));
                return;
            }
        }
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
    public String getNestingStringForHomePressed() {
        return getCurrentFragment() == null ? "unknown" : getCurrentFragment().getClass().getSimpleName();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null && data.hasExtra(CLOSE_NESTED) && getNestingStringForHomePressed().equals(data.getStringExtra(CLOSE_NESTED))) {
            Intent newData = new Intent();
            newData.putExtra(CLOSE_NESTED, getNestingStringForHomePressed());

            setResult(0, newData);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}