package net.mabako.steamgifts.adapters.viewholder;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import net.mabako.steamgifts.activities.UrlHandlingActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.MessageHeader;

public class MessageHeaderViewHolder extends RecyclerView.ViewHolder {
    private final TextView text;
    private final Context context;

    public MessageHeaderViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;

        text = (TextView) itemView.findViewById(R.id.text);
    }

    public void setFrom(final MessageHeader message) {
        text.setText(message.getTitle());

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(UrlHandlingActivity.getIntentForUri(context, Uri.parse(message.getUrl())));
            }
        };
        itemView.setOnClickListener(listener);
        text.setOnClickListener(listener);
    }
}
