package net.mabako.steamgifts.adapters.viewholder;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Discussion;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class DiscussionListItemViewHolder extends RecyclerView.ViewHolder {
    private final View itemContainer;
    private final TextView discussionName;
    private final TextView discussionAuthor;
    private final ImageView discussionAuthorAvatar;
    private final TextView discussionTime;

    private final Activity activity;

    public DiscussionListItemViewHolder(View itemView, Activity activity) {
        super(itemView);
        this.activity = activity;

        itemContainer = itemView.findViewById(R.id.list_item);
        discussionName = (TextView) itemView.findViewById(R.id.discussion_name);
        discussionAuthor = (TextView) itemView.findViewById(R.id.discussion_author);
        discussionAuthorAvatar = (ImageView) itemView.findViewById(R.id.author_avatar);
        discussionTime = (TextView) itemView.findViewById(R.id.discussion_time);
    }

    public void setFrom(Discussion discussion) {
        discussionName.setText(discussion.getName());
        discussionAuthor.setText(discussion.getCreator());
        discussionTime.setText(discussion.getTimeCreated());

        itemContainer.setBackgroundResource(discussion.isLocked() ? R.color.md_blue_grey_100 : android.R.color.background_light);

        Picasso.with(activity).load(discussion.getCreatorAvatar()).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into(discussionAuthorAvatar);
    }
}
