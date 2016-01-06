package net.mabako.steamgifts.adapters.viewholder;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.ICommentableFragment;
import net.mabako.steamgifts.web.WebUserData;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
    private static final int MAX_VISIBLE_DEPTH = 11;

    private final ICommentableFragment fragment;

    private final TextView commentAuthor;
    private final TextView commentTime;
    private final TextView commentContent;
    private final ImageView commentImage;
    private final View commentIndent;
    private final View commentMarker;

    private final Context context;
    private View.OnClickListener writeCommentListener;

    public CommentViewHolder(View v, Context context, ICommentableFragment fragment) {
        super(v);
        this.fragment = fragment;
        this.context = context;

        commentAuthor = (TextView) v.findViewById(R.id.user);
        commentTime = (TextView) v.findViewById(R.id.time);

        commentContent = (TextView) v.findViewById(R.id.content);
        commentContent.setMovementMethod(LinkMovementMethod.getInstance());

        commentMarker = v.findViewById(R.id.comment_marker);
        commentIndent = v.findViewById(R.id.comment_indent);
        commentImage = (ImageView) v.findViewById(R.id.image);

        v.setOnCreateContextMenuListener(this);
    }

    public void setFrom(final Comment comment) {
        commentAuthor.setText(comment.getAuthor());
        commentTime.setText(comment.getTimeAgo());
        commentContent.setText(Utils.fromHtml(comment.getContent(), !comment.isDeleted()));

        // Space before the marker
        ViewGroup.LayoutParams params = commentIndent.getLayoutParams();
        params.width = commentMarker.getLayoutParams().width * Math.min(MAX_VISIBLE_DEPTH, comment.getDepth());
        commentIndent.setLayoutParams(params);

        Picasso.with(context).load(comment.getAvatar()).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into(commentImage);

        writeCommentListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.requestComment(comment);
            }
        };
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (WebUserData.getCurrent().isLoggedIn()) {
            menu.setHeaderTitle("Actions");

            menu.add(0, 1, 0, "Comment").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (writeCommentListener != null)
                        writeCommentListener.onClick(itemView);
                    return true;
                }
            });
        }
    }
}
