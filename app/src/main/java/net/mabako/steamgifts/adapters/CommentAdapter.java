package net.mabako.steamgifts.adapters;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.mabako.steamgifts.adapters.viewholder.CommentViewHolder;
import net.mabako.steamgifts.adapters.viewholder.GiveawayCardViewHolder;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.util.GiveawayDetailsCard;

import java.util.List;

/**
 * Adapter to hold comments for a giveaway/discussion.
 */
public class CommentAdapter extends EndlessAdapter {
    /**
     * Amount of top-level items on a full comments page.
     */
    private static final int ITEMS_PER_PAGE = 25;

    /**
     * Colors, reinitialized in {@link #setColors()}.
     */
    private int[] colors = {android.R.color.holo_blue_dark, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_red_dark};

    private final GiveawayDetailFragment fragment;

    public CommentAdapter(GiveawayDetailFragment fragment, RecyclerView view, EndlessAdapter.OnLoadListener loadListener) {
        super(view, loadListener);

        this.fragment = fragment;

        setColors();
    }

    private void setColors() {
        for (int i = 0; i < colors.length; ++i)
            colors[i] = ContextCompat.getColor(fragment.getContext(), colors[i]);
    }

    @Override
    public RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        switch (viewType) {
            case Comment.VIEW_LAYOUT:
                return new CommentViewHolder(view);

            case GiveawayDetailsCard.VIEW_LAYOUT:
                return new GiveawayCardViewHolder(view, fragment);
        }
        return null;
    }

    @Override
    public void onBindActualViewHolder(RecyclerView.ViewHolder h, int position) {
        if (h instanceof CommentViewHolder) {
            CommentViewHolder holder = (CommentViewHolder) h;
            Comment comment = (Comment) getItem(position);

            holder.setFrom(comment, colors);
        } else if (h instanceof GiveawayCardViewHolder) {
            GiveawayCardViewHolder holder = (GiveawayCardViewHolder) h;
            GiveawayDetailsCard card = (GiveawayDetailsCard) getItem(position);

            holder.setFrom(card);
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        if (items.size() < ITEMS_PER_PAGE)
            return false;

        int rootLevelComments = 0;
        for (IEndlessAdaptable adaptable : items)
            if (adaptable instanceof Comment && ((Comment) adaptable).getDepth() == 0)
                ++rootLevelComments;

        return rootLevelComments == ITEMS_PER_PAGE;
    }
}
