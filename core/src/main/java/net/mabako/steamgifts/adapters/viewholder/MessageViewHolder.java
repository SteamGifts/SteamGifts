package net.mabako.steamgifts.adapters.viewholder;

import android.content.Context;
import android.util.Log;
import android.view.View;

import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.profile.MessageListFragment;

public class MessageViewHolder extends CommentViewHolder {
    private final MessageListFragment fragment;

    public MessageViewHolder(View v, Context context, MessageListFragment fragment) {
        super(v, context, fragment);
        this.fragment = fragment;
    }

    @Override
    public void setFrom(final Comment comment) {
        super.setFrom(comment);

        if (comment.getPermalinkId() != null) {
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.navigateTo(comment);
                }
            };

            itemView.setOnClickListener(clickListener);
            commentContent.setOnClickListener(clickListener);
        } else {
            Log.d(MessageViewHolder.class.getSimpleName(), "Comment ain't got no permalink");
        }
    }
}
