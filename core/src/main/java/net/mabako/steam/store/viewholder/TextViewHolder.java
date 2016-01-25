package net.mabako.steam.store.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.adapters.viewholder.Utils;
import net.mabako.steam.store.data.Text;

public class TextViewHolder extends RecyclerView.ViewHolder {
    private final Context context;
    private final TextView textView;

    public TextViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;

        textView = (TextView) itemView.findViewById(R.id.text);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    public void setFrom(Text text) {
        if (TextUtils.isEmpty(text.getText()))
            textView.setText(null);
        else if (text.isHtml())
            textView.setText(Utils.fromHtml(context, text.getText()));
        else
            textView.setText(text.getText());
    }
}
