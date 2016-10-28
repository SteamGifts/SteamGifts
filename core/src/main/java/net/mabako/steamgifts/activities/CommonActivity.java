package net.mabako.steamgifts.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import com.mikepenz.iconics.context.IconicsContextWrapper;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicDiscussion;
import net.mabako.steamgifts.data.BasicGiveaway;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.UserDetailFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

public class CommonActivity extends BaseActivity {
    private static final String TAG = CommonActivity.class.getSimpleName();
    public static final String FRAGMENT_TAG = "Fragment Root";

    public static final int REQUEST_LOGIN = 3;
    public static final int REQUEST_LOGIN_PASSIVE = 4;
    public static final int REQUEST_LOGIN_SGTOOLS = 5;

    public static final int RESPONSE_LOGIN_SUCCESSFUL = 6;
    public static final int RESPONSE_LOGIN_SGTOOLS_SUCCESSFUL = 7;

    public static final int REQUEST_SYNC = 8;
    public static final int RESPONSE_SYNC_SUCCESSFUL = 9;

    public static final int REQUEST_SETTINGS = 10;
    public static final int RESPONSE_LOGOUT = 11;

    public void requestLogin() {
        startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_LOGIN);
    }

    public void loadFragment(Fragment fragment) {
        super.loadFragment(R.id.fragment_container, fragment, FRAGMENT_TAG);
        updateTitle(fragment);
    }

    protected void updateTitle(Fragment fragment) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String fragmentTitle = getFragmentTitle(fragment);
            actionBar.setTitle(fragmentTitle);

            if (fragment instanceof IActivityTitle) {
                String extraTitle = ((IActivityTitle) fragment).getExtraTitle();
                if (extraTitle != null && !extraTitle.equals(fragmentTitle)) {
                    actionBar.setSubtitle(extraTitle);
                }
            }
        }
    }

    @NonNull
    protected String getFragmentTitle(Fragment fragment) {
        String title = getString(R.string.app_name);
        if (fragment instanceof IActivityTitle) {
            int resource = ((IActivityTitle) fragment).getTitleResource();
            String extraTitle = ((IActivityTitle) fragment).getExtraTitle();

            if (resource != 0) {
                title = getString(resource);
            } else if (extraTitle != null) {
                title = extraTitle;
            }
        }
        return title;
    }

    public Fragment getCurrentFragment() {
        return getCurrentFragment(FRAGMENT_TAG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOGIN:
            case REQUEST_LOGIN_PASSIVE:
                // Do not show an explicit notification.
                if (resultCode == RESPONSE_LOGIN_SUCCESSFUL && SteamGiftsUserData.getCurrent(this).isLoggedIn())
                    onAccountChange();

                // Pass on the result.
                setResult(resultCode);

                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            event.startTracking();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Always-available "Go to ..." menu by long-pressing back.
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        // TODO allow this to be changed to normal overflow menus in the settings.
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            final CharSequence[] strings = new CharSequence[]{getString(R.string.go_to_giveaway), getString(R.string.go_to_discussion), getString(R.string.go_to_user)};

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.go_to);
            builder.setItems(strings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, final int dialogSelected) {
                    final View view = getLayoutInflater().inflate(R.layout.go_to_dialog, null);

                    AlertDialog.Builder builder = new AlertDialog.Builder(CommonActivity.this);
                    builder.setTitle(R.string.go_to);
                    builder.setMessage(strings[dialogSelected]);
                    builder.setView(view);
                    builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    });
                    final AlertDialog dialog = builder.create();
                    dialog.show();

                    // Discussion and giveaway ids can only be 5 chars long
                    final boolean limitLength = dialogSelected == 0 || dialogSelected == 1 || dialogSelected == 3;
                    if (limitLength)
                        ((EditText) dialog.findViewById(R.id.edit_text)).setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});

                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String target = ((EditText) view.findViewById(R.id.edit_text)).getText().toString();
                            if (target != null && (!limitLength || target.length() == 5)) {
                                Intent intent = new Intent(CommonActivity.this, DetailActivity.class);
                                switch (dialogSelected) {
                                    case 0:
                                        intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, new BasicGiveaway(target));
                                        break;
                                    case 1:
                                        intent.putExtra(DiscussionDetailFragment.ARG_DISCUSSION, new BasicDiscussion(target));
                                        break;
                                    case 2:
                                        intent.putExtra(UserDetailFragment.ARG_USER, target);
                                        break;
                                }
                                startActivity(intent);

                                dialog.dismiss();
                            }
                        }
                    });

                }
            });
            builder.show();
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
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
}
