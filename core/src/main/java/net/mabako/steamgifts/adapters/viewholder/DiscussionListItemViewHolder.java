package net.mabako.steamgifts.adapters.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class DiscussionListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final View itemContainer;
    private final TextView discussionTitle;
    private final TextView discussionAuthor;
    private final ImageView discussionAuthorAvatar;
    private final TextView discussionTime;

    private final EndlessAdapter adapter;
    private final Activity activity;

    public DiscussionListItemViewHolder(View itemView, Activity activity, EndlessAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
        this.activity = activity;

        itemContainer = itemView.findViewById(R.id.list_item);
        discussionTitle = (TextView) itemView.findViewById(R.id.discussion_title);
        discussionAuthor = (TextView) itemView.findViewById(R.id.discussion_author);
        discussionAuthorAvatar = (ImageView) itemView.findViewById(R.id.author_avatar);
        discussionTime = (TextView) itemView.findViewById(R.id.discussion_time);

        itemView.setOnClickListener(this);
    }

    public void setFrom(Discussion discussion) {
        StringBuilder text = new StringBuilder();
        if (discussion.isPoll())
            text.append("{faw-align-left} ");
        text.append(discussion.getTitle());

        discussionTitle.setText(text);
        discussionAuthor.setText(discussion.getCreator());
        discussionTime.setText(discussion.getRelativeCreatedTime(activity));

        StringUtils.setBackgroundDrawable(activity, itemContainer, discussion.isLocked());

        Picasso.with(activity).load(discussion.getCreatorAvatar()).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into(discussionAuthorAvatar);
    }

    @Override
    public void onClick(View v) {
        Discussion discussion = (Discussion) adapter.getItem(getAdapterPosition());

        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(DiscussionDetailFragment.ARG_DISCUSSION, discussion);

        activity.startActivityForResult(intent, CommonActivity.REQUEST_LOGIN_PASSIVE);
    }
}
