package net.mabako.steamgifts.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import net.mabako.steamgifts.activities.WriteCommentActivity;
import net.mabako.steamgifts.adapters.CommentAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.adapters.viewholder.CommentContextViewHolder;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.interfaces.ICommentableFragment;
import net.mabako.steamgifts.tasks.DeleteCommentTask;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A list fragment that allows us to load a particular context for the comments, i.e. when following a permalink.
 */
public abstract class DetailFragment extends ListFragment<CommentAdapter> implements ICommentableFragment {
    private static final String TAG = "ContextualCommentFmt";

    public static final String ARG_COMMENT_CONTEXT = "comment-context";
    protected static final String SAVED_COMMENT_CONTEXT = ARG_COMMENT_CONTEXT;

    /**
     * How many parent comments should be displayed?
     */
    private static final int MAX_PARENT_DEPTH = 3;

    private CommentContextInfo commentContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            commentContext = (CommentContextInfo) getArguments().getSerializable(SAVED_COMMENT_CONTEXT);
        } else {
            commentContext = (CommentContextInfo) savedInstanceState.getSerializable(SAVED_COMMENT_CONTEXT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_COMMENT_CONTEXT, commentContext);
    }

    @NonNull
    @Override
    protected final CommentAdapter createAdapter() {
        return new CommentAdapter();
    }

    @Override
    protected final AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return getFetchItemsTaskEx(commentContext != null ? commentContext.getPage() : page);

    }

    protected abstract AsyncTask<Void, Void, ?> getFetchItemsTaskEx(int page);

    @Override
    protected Serializable getType() {
        return null;
    }

    public CommentContextInfo getCommentContext() {
        return commentContext;
    }

    @Override
    public void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems) {
        if (commentContext == null || items == null) {
            super.addItems(items, clearExistingItems);
        } else {
            Log.d(TAG, "Looking for Comment with permalink id " + commentContext.getCommentId());
            if (adapter.isViewInReverse())
                throw new IllegalStateException("Cannot view an adapter in reverse if comment context is set");

            // We've sorta put them in a flat list, which is, well, silly at best.
            for (int i = 0; i < items.size(); ++i) {
                Comment comment = (Comment) items.get(i);

                if (commentContext.getCommentId().equals(comment.getPermalinkId())) {
                    // We found the comment we want, hooray!
                    // Build a new comment context
                    List<IEndlessAdaptable> displayedComments = new ArrayList<>();

                    // Do we want to include the parent?
                    if (commentContext.isIncludeParentComment()) {
                        addParentComments(items, comment, displayedComments);

                        // we have the parent comments in reverse, so... let's reverse them
                        Collections.reverse(displayedComments);
                    }

                    // add the comment itself.
                    comment.setHighlighted(true);
                    displayedComments.add(comment);

                    // add all children
                    addChildComments(items, comment, displayedComments);

                    // Fix all the comment depths, we assume the first comment has the lowest depth here.
                    int depthDiff = ((Comment) displayedComments.get(0)).getDepth();
                    for (IEndlessAdaptable item : displayedComments) {
                        Comment c = (Comment) item;
                        c.setDepth(c.getDepth() - depthDiff);
                    }

                    // Add a 'view full discussion' to this.
                    displayedComments.add(0, new CommentContextViewHolder.SerializableHolder(getDetailObject()));

                    // add the items to the adapter.
                    super.addItems(displayedComments, clearExistingItems);

                    // Nothing more to do here.
                    return;
                }
            }
        }
    }

    /**
     * Adds all parent comments to this fragment.
     *
     * @param displayedComments the list of comments we should display after this comment.
     */
    private void addParentComments(List<? extends IEndlessAdaptable> loadedItems, Comment commentInContext, List<IEndlessAdaptable> displayedComments) {
        int depth = commentInContext.getDepth();

        // is this the a root comment?
        if (depth == 0)
            return;

        --depth;

        int commentPosition = loadedItems.indexOf(commentInContext);
        for (int i = commentPosition - 1; i >= 0; --i) {
            Comment c = (Comment) loadedItems.get(i);
            if (c.getDepth() == depth) {
                // this is the parent, boo-ya!
                displayedComments.add(c);

                --depth;
                if (commentInContext.getDepth() - depth > MAX_PARENT_DEPTH || depth < 0)
                    // We only care for a limited amount of parent comments.
                    break;
            }
        }
    }

    /**
     * Add all child comments.
     *
     * @param displayedComments the list of comments we should display after this comment.
     */
    private void addChildComments(List<? extends IEndlessAdaptable> loadedItems, Comment comment, List<IEndlessAdaptable> displayedComments) {
        for (int j = loadedItems.indexOf(comment) + 1; j < loadedItems.size(); ++j) {
            Comment child = (Comment) loadedItems.get(j);

            // this is no longer a child of our comment.
            if (child.getDepth() <= comment.getDepth())
                break;

            displayedComments.add(child);
        }
    }

    @NonNull
    protected abstract Serializable getDetailObject();

    @Nullable
    protected abstract String getDetailPath();

    protected abstract String getTitle();

    @Override
    public void requestComment(Comment parentComment) {
        Log.d(TAG, "request comment for " + parentComment);

        if (TextUtils.isEmpty(adapter.getXsrfToken()))
            throw new IllegalStateException("no xsrf token");

        String path = getDetailPath();
        if (path != null) {
            Intent intent = new Intent(getActivity(), WriteCommentActivity.class);
            intent.putExtra(WriteCommentActivity.XSRF_TOKEN, adapter.getXsrfToken());
            intent.putExtra(WriteCommentActivity.TITLE, getTitle());
            intent.putExtra(WriteCommentActivity.PATH, path);
            intent.putExtra(WriteCommentActivity.PARENT, parentComment);
            addExtraForCommentIntent(intent, parentComment);
            getActivity().startActivityForResult(intent, WriteCommentActivity.REQUEST_COMMENT);
        } else
            throw new IllegalStateException("got no path for the comment, not commenting...");
    }

    @Override
    public boolean canPostOrModifyComments() {
        return true;
    }

    /**
     * A way to extend {@link #requestComment(Comment)} in adding additional parameters
     *
     * @param intent        the intent to add parameters to
     * @param parentComment optional parent comment
     */
    protected void addExtraForCommentIntent(@NonNull Intent intent, @Nullable Comment parentComment) {
    }

    /**
     * Edit a specific comment.
     *
     * @param comment comment to edit
     */
    public void requestCommentEdit(Comment comment) {
        Log.d(TAG, "request comment edit for " + comment + ", xsrf-token: " + adapter.getXsrfToken());

        if (TextUtils.isEmpty(adapter.getXsrfToken()))
            throw new IllegalStateException("no xsrf token");

        Intent intent = new Intent(getActivity(), WriteCommentActivity.class);
        intent.putExtra(WriteCommentActivity.XSRF_TOKEN, adapter.getXsrfToken());

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        intent.putExtra(WriteCommentActivity.TITLE, actionBar == null ? getString(R.string.add_comment) : actionBar.getTitle());

        intent.putExtra(WriteCommentActivity.EDIT_COMMENT, comment);
        getActivity().startActivityForResult(intent, WriteCommentActivity.REQUEST_COMMENT_EDIT);
    }

    /**
     * Callback for a successful comment edit request.
     */
    public void onCommentEdited(long commentId, String newContent, String newEditableContent) {
        Comment comment = adapter.findItem(commentId);
        if (comment == null) {
            Log.d(TAG, "No comment with id " + commentId + " found");
            return;
        }

        comment.setContent(newContent);
        comment.setEditableContent(newEditableContent);

        if (adapter.notifyItemChanged(comment)) {
            Log.d(TAG, "Comment was edited & adapter triggered notify");
        } else {
            Log.w(TAG, "Unable to update comment " + comment);
        }
    }

    @Override
    public void deleteComment(Comment comment) {
        new DeleteCommentTask(this, getContext(), adapter.getXsrfToken(), comment.isDeleted() ? DeleteCommentTask.DO_UNDELETE : DeleteCommentTask.DO_DELETE, comment).execute();
    }

    public void onCommentDeleted(Comment comment) {
        adapter.replaceComment(comment);
    }

    /**
     * Context for a single comment.
     */
    public static class CommentContextInfo implements Serializable {
        private static final long serialVersionUID = -6422147335611873577L;

        private String detailName;
        private String commentId;
        private int page;
        private boolean includeParentComment;

        public String getDetailName() {
            return detailName;
        }

        public void setDetailName(String detailName) {
            this.detailName = detailName;
        }

        public String getCommentId() {
            return commentId;
        }

        public void setCommentId(String commentId) {
            this.commentId = commentId;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public boolean isIncludeParentComment() {
            return includeParentComment;
        }

        public void setIncludeParentComment(boolean includeParentComment) {
            this.includeParentComment = includeParentComment;
        }

        /**
         * If the uri has the format of https://www.steamgifts.com/giveaways/[id]/[name]#comment, or https://www.steamgifts.com/giveaways/[id]/[name]/search?page=27#comment, we do return the context.
         */
        public static CommentContextInfo fromUri(Uri uri) {
            if (TextUtils.isEmpty(uri.getFragment()))
                return null;

            Log.d(TAG, "Parsing Context info for " + uri.toString());

            CommentContextInfo info = new CommentContextInfo();

            // Parse the page.
            String queryPage = uri.getQueryParameter("page");
            if (queryPage != null) {
                info.setPage(Integer.valueOf(queryPage));
            } else {
                info.setPage(1);
            }

            // The comment ID we want to look at.
            info.setCommentId(uri.getFragment());
            info.setIncludeParentComment(true);

            // For /giveaways/abcde/bad-rats, this would be 'bad-rats'
            if (uri.getPathSegments().size() >= 3)
                info.setDetailName(uri.getPathSegments().get(2));

            return info;
        }
    }
}
