package net.mabako.steamgifts.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Discussion;

public class DiscussionListItemViewHolder extends RecyclerView.ViewHolder {
    private final View itemContainer;
    private final TextView discussionName;
    private final TextView discussionDetails;

    public DiscussionListItemViewHolder(View itemView) {
        super(itemView);

        itemContainer = itemView.findViewById(R.id.list_item);
        discussionName = (TextView) itemView.findViewById(R.id.discussion_name);
        discussionDetails = (TextView) itemView.findViewById(R.id.discussion_details);
    }

    public void setFrom(Discussion discussion) {
        discussionName.setText(discussion.getName());
    }
}
