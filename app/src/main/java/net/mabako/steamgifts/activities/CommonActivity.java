package net.mabako.steamgifts.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.fragments.IFragmentNotifications;
import net.mabako.steamgifts.web.WebUserData;

public class CommonActivity extends BaseActivity {
    private static final String TAG = CommonActivity.class.getSimpleName();
    private static final String FRAGMENT_TAG = "Fragment Root";

    public static final int REQUEST_LOGIN = 3;
    public static final int REQUEST_LOGIN_PASSIVE = 4;

    public static final int RESPONSE_LOGIN_SUCCESSFUL = 5;

    public void requestLogin() {
        startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
    }

    protected void loadFragment(Fragment fragment) {
        super.loadFragment(R.id.fragment_container, fragment, FRAGMENT_TAG);

        // Update the title.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            Log.v(TAG, "Current Fragment is a " + fragment.getClass().getName());
            if (fragment instanceof IFragmentNotifications) {
                String title = null;
                int resource = ((IFragmentNotifications) fragment).getTitleResource();
                String extra = ((IFragmentNotifications) fragment).getExtraTitle();

                if (resource != 0) {
                    if (extra != null && !extra.isEmpty())
                        title = String.format("%s: %s", extra, getString(resource));
                    else
                        title = getString(resource);
                } else
                    title = extra;

                actionBar.setTitle(title);
                Log.v(TAG, "Setting Toolbar title to " + title);
            } else
                actionBar.setTitle(R.string.app_name);
        }
    }

    public Fragment getCurrentFragment() {
        return getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN:
            case REQUEST_LOGIN_PASSIVE:
                // Do not show an explicit notification.
                if (resultCode == RESPONSE_LOGIN_SUCCESSFUL && WebUserData.getCurrent().isLoggedIn())
                    onAccountChange();

                // Pass on the result.
                setResult(resultCode);

                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
