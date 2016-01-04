package net.mabako.steamgifts.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Discussion;
import net.mabako.steamgifts.data.DiscussionExtras;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.fragments.ICommentableFragment;
import net.mabako.steamgifts.fragments.util.DiscussionDetailsCard;

public class DiscussionCardViewHolder extends RecyclerView.ViewHolder {
    private final ICommentableFragment fragment;

    private final View progressBar;
    private final TextView discussionTime;
    private final TextView user;
    private final TextView description;

    private final Button commentDiscussion;

    public DiscussionCardViewHolder(View v, final DiscussionDetailFragment fragment) {
        super(v);
        this.fragment = fragment;

        progressBar = v.findViewById(R.id.progressBar);
        user = (TextView) v.findViewById(R.id.user);
        discussionTime = (TextView) v.findViewById(R.id.time);
        description = (TextView) v.findViewById(R.id.description);
        description.setMovementMethod(LinkMovementMethod.getInstance());

        commentDiscussion = (Button) v.findViewById(R.id.comment);
    }

    public void setFrom(final DiscussionDetailsCard card) {
        Discussion discussion = card.getDiscussion();
        DiscussionExtras extras = card.getExtras();

        user.setText("{faw-user} " + discussion.getCreator());
        discussionTime.setText("{faw-clock-o} " + discussion.getTimeCreated());

        for (View view : new View[]{commentDiscussion, description})
            view.setVisibility(View.GONE);

        if (extras == null) {
            // Still loading...
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);

            if (extras.getDescription() != null) {
                CharSequence desc = Html.fromHtml(extras.getDescription(), null, new CustomHtmlTagHandler());
                desc = desc.subSequence(0, desc.length() - 2);

                description.setText(desc);
                description.setVisibility(View.VISIBLE);
            }

            if(extras.getXsrfToken() != null)
                commentDiscussion.setVisibility(View.VISIBLE);
        }

        commentDiscussion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.requestComment(null);
            }
        });
    }
}
