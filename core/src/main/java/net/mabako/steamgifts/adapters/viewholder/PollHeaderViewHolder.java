package net.mabako.steamgifts.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Poll;

public class PollHeaderViewHolder extends RecyclerView.ViewHolder {
    private final TextView text;

    public PollHeaderViewHolder(View itemView) {
        super(itemView);

        text = (TextView) itemView.findViewById(R.id.text);
    }

    public void setFrom(Poll.Header header) {
        text.setText(header.getText());
    }
}
