package net.mabako.steamgifts.adapters.viewholder;


import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Comment;

public class CommentViewHolder extends RecyclerView.ViewHolder {
    private final TextView commentAuthor;
    private final TextView commentTime;
    private final TextView commentContent;
    private final View commentIndent;
    private final View commentMarker;

    public CommentViewHolder(View v) {
        super(v);
        commentAuthor = (TextView) v.findViewById(R.id.user);
        commentTime = (TextView) v.findViewById(R.id.time);
        commentContent = (TextView) v.findViewById(R.id.content);
        commentMarker = v.findViewById(R.id.comment_marker);
        commentIndent = v.findViewById(R.id.comment_indent);

        commentContent.setMovementMethod(LinkMovementMethod.getInstance());

        // OnClickListener...
    }

    public void setFrom(Comment comment, int[] colors) {
        commentAuthor.setText(comment.getAuthor());
        commentTime.setText(comment.getTimeAgo());

        CharSequence desc = Html.fromHtml(comment.getContent());
        desc = desc.subSequence(0, desc.length() - 2);
        commentContent.setText(desc);

        // Space before the marker
        ViewGroup.LayoutParams params = commentIndent.getLayoutParams();
        params.width = commentMarker.getLayoutParams().width * comment.getDepth();
        commentIndent.setLayoutParams(params);

        // Marker
        commentMarker.setBackgroundColor(colors[comment.getDepth() % colors.length]);
        commentMarker.invalidate();

    }
}
