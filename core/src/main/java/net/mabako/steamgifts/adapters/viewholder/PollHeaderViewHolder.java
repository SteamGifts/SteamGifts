package net.mabako.steamgifts.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Poll;

public class PollHeaderViewHolder extends RecyclerView.ViewHolder {
    private final TextView question, description;

    public PollHeaderViewHolder(View itemView) {
        super(itemView);

        question = (TextView) itemView.findViewById(R.id.poll_question);
        description = (TextView) itemView.findViewById(R.id.poll_description);
    }

    public void setFrom(Poll.Header header) {
        question.setText(header.getQuestion());

        if (!TextUtils.isEmpty(header.getDescription())) {
            description.setText(header.getDescription());
        } else {
            description.setVisibility(View.GONE);
        }
    }
}
