package net.mabako.steamgifts.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.compat.PreferenceFragment;

public class SettingsActivity extends BaseActivity {
    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_one_fragment);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        loadFragment(R.id.fragment_container, new SettingsFragment(), "settings");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
