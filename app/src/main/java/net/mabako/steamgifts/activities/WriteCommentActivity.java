package net.mabako.steamgifts.activities;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.SingleCommentFragment;
import net.mabako.steamgifts.fragments.WriteCommentFragment;
import net.mabako.steamgifts.tasks.PostCommentTask;

public class WriteCommentActivity extends BaseActivity {
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
            loadFragment(R.id.fragment_container, new SingleCommentFragment(), "parent");
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

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return false;
        }
    }
}
