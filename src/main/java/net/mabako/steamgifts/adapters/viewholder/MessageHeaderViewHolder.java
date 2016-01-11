package net.mabako.steamgifts.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.MessageHeader;

public class MessageHeaderViewHolder extends RecyclerView.ViewHolder {
    private final TextView text;

    public MessageHeaderViewHolder(View itemView) {
        super(itemView);

        text = (TextView) itemView.findViewById(R.id.text);
    }

    public void setFrom(MessageHeader message) {
        text.setText(message.getTitle());
    }
}
