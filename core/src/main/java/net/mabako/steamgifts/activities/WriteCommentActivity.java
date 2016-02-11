package net.mabako.steamgifts.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.Toast;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.SingleCommentFragment;
import net.mabako.steamgifts.fragments.WriteCommentFragment;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.tasks.AjaxTask;
import net.mabako.steamgifts.tasks.EditCommentTask;
import net.mabako.steamgifts.tasks.EnterLeaveGiveawayTask;
import net.mabako.steamgifts.tasks.PostCommentTask;

public class WriteCommentActivity extends BaseActivity implements DialogInterface.OnDismissListener {
    private static final String TAG = WriteCommentActivity.class.getSimpleName();

    private static final String FRAGMENT_TAG_PARENT_COMMENT = "fragment-parent";
    private static final String FRAGMENT_TAG_WRITER = "fragment-writer";

    private static final String SAVED_ENTERED = "entered";

    public static final int REQUEST_COMMENT = 7;
    public static final int REQUEST_COMMENT_EDIT = 8;
    public static final int COMMENT_SENT = 9;
    public static final int COMMENT_EDIT_SENT = 10;

    public static final String XSRF_TOKEN = "xsrf-token";
    public static final String PARENT = "parent";
    public static final String PATH = "path";
    public static final String TITLE = "title";
    public static final String EDIT_COMMENT = "comment-to-edit";
    public static final String GIVEAWAY_ID = "giveaway-id";
    private AjaxTask<?> task;

    private ProgressDialog progressDialog;
    private boolean entered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent().getStringExtra(XSRF_TOKEN) == null) {
            finish();
            return;
        }

        setContentView(R.layout.activity_write_comment);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getIntent().getStringExtra(TITLE));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent().getSerializableExtra(PARENT) != null) {
            Log.d(TAG, "Loading parent comment");
            loadFragment(R.id.fragment_container, new SingleCommentFragment(), FRAGMENT_TAG_PARENT_COMMENT);

            // If the fragment is smaller than the scrollview, resize the scrollview to be smaller as well.
            final ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
            scrollView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    View commentLayout = findViewById(R.id.fragment_container);

                    float density = getResources().getDisplayMetrics().density;

                    int commentViewHeight = commentLayout.getMeasuredHeight();
                    int aboutHalfAScreen = getWindow().getDecorView().getHeight() / 2;

                    // TODO Closing the keyboard does not expand the comment view to a "good" height (i.e. half the screen) again, but this method appears to be called.
                    if (scrollView.getLayoutParams().height > aboutHalfAScreen || scrollView.getMeasuredHeight() < commentViewHeight || commentViewHeight == 0) {
                        // "about half"
                        scrollView.getLayoutParams().height = aboutHalfAScreen;
                    } else {
                        // 8 is an arbitrary number that simply shows no scrollbar. Might be some padding constant?
                        scrollView.getLayoutParams().height = (int) (((commentViewHeight + 8) * density) + 0.5f);
                    }
                }
            });
        } else {
            Log.d(TAG, "No parent comment");
            findViewById(R.id.scrollView).setVisibility(View.GONE);
            findViewById(R.id.separator).setVisibility(View.GONE);
        }

        if (savedInstanceState == null) {
            loadFragment(R.id.fragment_container2, WriteCommentFragment.newInstance((Comment) getIntent().getSerializableExtra(EDIT_COMMENT), getIntent().getStringExtra(GIVEAWAY_ID)), FRAGMENT_TAG_WRITER);
        } else {
            entered = savedInstanceState.getBoolean(SAVED_ENTERED, false);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        outState.putBoolean(SAVED_ENTERED, entered);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    /**
     * Editing is finished, so send the comment to the server.
     *
     * @param comment if set, we're editing a comment, and use this comment's id for the id we wish to submit
     * @param text    text of either the new comment, or the edited comment
     */
    public void submit(@Nullable Comment comment, String text) {
        showProgressDialog(R.string.sending_comment);

        if (comment == null) {
            Comment parent = (Comment) getIntent().getSerializableExtra(PARENT);
            final long parentId = parent != null ? parent.getId() : 0;

            task = new PostCommentTask(this, getIntent().getStringExtra(PATH), getIntent().getStringExtra(XSRF_TOKEN), text, parentId) {
                @Override
                protected void onSuccess() {
                    Intent data = new Intent();
                    data.putExtra("parent", parentId);
                    data.putExtra("entered", entered);
                    setResult(COMMENT_SENT, data);
                    finish();
                }

                @Override
                protected void onFail() {
                    Toast.makeText(WriteCommentActivity.this, R.string.comment_not_sent, Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            };
            task.execute();
        } else {
            task = new EditCommentTask(this, getIntent().getStringExtra(XSRF_TOKEN), text, comment) {
                @Override
                protected void onFail() {
                    Toast.makeText(WriteCommentActivity.this, R.string.comment_not_sent, Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            };
            task.execute();
        }
    }

    /**
     * Editing is finished, so send the enter + comment to the server. This method does not accept a
     * parent comment, since we're not allowing enter + comment on child comments.
     *
     * @param text text of either the new comment, or the edited comment
     */
    public void submitAndEnter(final String text, String giveawayId) {
        showProgressDialog(R.string.entering);

        task = new EnterLeaveGiveawayTask(new IHasEnterableGiveaways() {
            @Override
            public void requestEnterLeave(String giveawayId, String what, String xsrfToken) {

            }

            @Override
            public void onEnterLeaveResult(String giveawayId, String what, Boolean success, boolean propagate) {
                if (success) {
                    Log.v(TAG, "entered giveaway " + giveawayId);

                    WriteCommentFragment fragment = (WriteCommentFragment) getCurrentFragment(FRAGMENT_TAG_WRITER);
                    fragment.onEntered();
                    entered = true;

                    submit(null, text);
                } else {
                    Toast.makeText(WriteCommentActivity.this, R.string.unable_to_enter, Toast.LENGTH_SHORT).show();
                    hideProgressDialog();
                }
            }
        }, this, giveawayId, getIntent().getStringExtra(XSRF_TOKEN), GiveawayDetailFragment.ENTRY_INSERT);
        task.execute();
    }

    private void showProgressDialog(@StringRes int res) {
        if (progressDialog != null) {
            progressDialog.setMessage(getString(res));
        } else {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(res));
            progressDialog.setIndeterminate(true);
            progressDialog.setOnDismissListener(this);
            progressDialog.show();
        }
    }

    private void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * Progress dialog was dismissed...
     *
     * @param dialog the dismissed progress dialog
     */
    @Override
    public void onDismiss(DialogInterface dialog) {
        if (task != null) {
            task.cancel(true);
            task = null;
        }
        hideProgressDialog();
    }
}
