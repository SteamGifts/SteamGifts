package net.mabako.steamgifts.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.SingleCommentFragment;
import net.mabako.steamgifts.fragments.WriteCommentFragment;
import net.mabako.steamgifts.tasks.PostCommentTask;

public class WriteCommentActivity extends BaseActivity {
    private static final String TAG = WriteCommentActivity.class.getSimpleName();

    public static final int REQUEST_COMMENT = 7;
    public static final int COMMENT_SENT = 8;
    public static final int COMMENT_NOT_SENT = 8;

    public static final String XSRF_TOKEN = "xsrf-token";
    public static final String PARENT = "parent";
    public static final String PATH = "path";
    public static final String TITLE = "title";
    private PostCommentTask task;

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
            loadFragment(R.id.fragment_container, new SingleCommentFragment(), "parent");

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

        loadFragment(R.id.fragment_container2, new WriteCommentFragment(), "writer");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (task != null)
            task.cancel(true);
    }

    public void submit(String text) {
        Comment comment = (Comment) getIntent().getSerializableExtra(PARENT);
        int parentId = 0;
        if (comment != null)
            parentId = comment.getId();

        task = new PostCommentTask(this, getIntent().getStringExtra(PATH), getIntent().getStringExtra(XSRF_TOKEN), text, parentId);
        task.execute();
    }
}
