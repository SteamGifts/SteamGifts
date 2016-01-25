package net.mabako.steamgifts.activities.settings;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.fragments.HiddenGamesFragment;

public class ViewHiddenGamesActivity extends CommonActivity {
    public static final String ARG_QUERY = "query";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_one_fragment);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.preference_sg_hidden_games);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            loadFragment(HiddenGamesFragment.newInstance(getIntent().getStringExtra(ARG_QUERY)));
        }
    }
}