package net.mabako.steamgifts.adapters.viewholder;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicDiscussion;
import net.mabako.steamgifts.data.BasicGiveaway;
import net.mabako.steamgifts.fragments.DiscussionDetailFragment;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;

import java.io.Serializable;

public class CommentContextViewHolder extends RecyclerView.ViewHolder {
    public static final int VIEW_LAYOUT = R.layout.comment_in_context;
    private final Context context;

    private final TextView text;

    public CommentContextViewHolder(View itemView, Context context) {
        super(itemView);

        this.context = context;
        text = (TextView) itemView.findViewById(R.id.text);
    }

    public void setFrom(SerializableHolder from) {
        final Serializable serializable = from.serializable;
        text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailActivity.class);
                if (serializable instanceof BasicGiveaway) {
                    intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, serializable);
                } else if (serializable instanceof BasicDiscussion) {
                    intent.putExtra(DiscussionDetailFragment.ARG_DISCUSSION, serializable);
                } else {
                    Log.w("CommentContextVH", "Calling CommentContextViewHolder.text.onClick, but serializable is " + (serializable == null ? "[null]" : serializable.getClass().getSimpleName()));
                    return;
                }

                context.startActivity(intent);
            }
        });
    }

    public static class SerializableHolder implements IEndlessAdaptable, Serializable {
        private static final long serialVersionUID = 1876122223753554650L;
        private Serializable serializable;

        public SerializableHolder(Serializable serializable) {
            this.serializable = serializable;
        }

        @Override
        @LayoutRes
        public int getLayout() {
            return VIEW_LAYOUT;
        }
    }
}
