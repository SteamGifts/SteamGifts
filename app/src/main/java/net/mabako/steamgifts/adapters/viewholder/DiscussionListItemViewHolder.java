package net.mabako.steamgifts.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Discussion;

public class DiscussionListItemViewHolder extends RecyclerView.ViewHolder {
    private final View itemContainer;
    private final TextView discussionName;
    private final TextView discussionAuthor;
    private final TextView discussionTime;

    public DiscussionListItemViewHolder(View itemView) {
        super(itemView);

        itemContainer = itemView.findViewById(R.id.list_item);
        discussionName = (TextView) itemView.findViewById(R.id.discussion_name);
        discussionAuthor = (TextView) itemView.findViewById(R.id.discussion_author);
        discussionTime = (TextView) itemView.findViewById(R.id.discussion_time);
    }

    public void setFrom(Discussion discussion) {
        discussionName.setText(discussion.getName());
    }
}
