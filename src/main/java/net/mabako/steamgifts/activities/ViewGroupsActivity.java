package net.mabako.steamgifts.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.fragments.GiveawayGroupListFragment;

public class ViewGroupsActivity extends CommonActivity {
    public static final String TITLE = "title";
    public static final String PATH = "path";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_one_fragment);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra(TITLE));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            loadFragment(GiveawayGroupListFragment.newInstance(getIntent().getStringExtra(TITLE), getIntent().getStringExtra(PATH)));
        }
    }
}
