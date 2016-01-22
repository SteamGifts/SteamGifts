package net.mabako.steamgifts.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.mabako.steamgifts.adapters.viewholder.CommentViewHolder;
import net.mabako.steamgifts.adapters.viewholder.MessageHeaderViewHolder;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.MessageHeader;
import net.mabako.steamgifts.fragments.profile.MessageListFragment;

import java.util.List;

public class MessageAdapter extends EndlessAdapter {
    private final MessageListFragment fragment;

    public MessageAdapter(MessageListFragment fragment, @NonNull OnLoadListener listener) {
        super(listener);
        this.fragment = fragment;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        switch (viewType) {
            case Comment.VIEW_LAYOUT:
                return new CommentViewHolder(view, fragment.getActivity(), fragment);

            case MessageHeader.VIEW_LAYOUT:
                return new MessageHeaderViewHolder(view);
        }

        throw new IllegalStateException();
    }

    @Override
    protected void onBindActualViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessageHeaderViewHolder) {
            ((MessageHeaderViewHolder) holder).setFrom((MessageHeader) getItem(position));
        } else if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).setFrom((Comment) getItem(position));
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        return items.size() > 0;
    }
}
