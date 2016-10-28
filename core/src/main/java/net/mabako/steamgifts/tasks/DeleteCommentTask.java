package net.mabako.steamgifts.tasks;

import android.content.Context;
import android.util.Log;

import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.DetailFragment;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class DeleteCommentTask extends AjaxTask<DetailFragment> {
    private static final String TAG = DeleteCommentTask.class.getSimpleName();

    public static final String DO_DELETE = "comment_delete";
    public static final String DO_UNDELETE = "comment_undelete";

    private final long commentId;
    private final int depth;

    public DeleteCommentTask(DetailFragment fragment, Context context, String xsrfToken, String what, Comment comment) {
        super(fragment, context, xsrfToken, what);
        commentId = comment.getId();
        depth = comment.getDepth();
    }

    @Override
    protected void addExtraParameters(Connection connection) {
        connection
                .data("comment_id", String.valueOf(commentId))
                .data("allow_replies", "1");
    }

    @Override
    protected void onPostExecute(Connection.Response response) {
        if (response != null && response.statusCode() == 200) {
            try {
                Log.v(TAG, "Response to JSON request: " + response.body());
                JSONObject root = new JSONObject(response.body());

                boolean success = "success".equals(root.getString("type"));
                if (success) {
                    Comment comment = Utils.loadComment(Jsoup.parse(root.getString("comment")), commentId, depth, false, Comment.Type.COMMENT);
                    getFragment().onCommentDeleted(comment);
                } else {
                    Log.w(TAG, "Could not " + getWhat() + " comment " + commentId + "?");
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse JSON object", e);
            }
        }
    }
}
