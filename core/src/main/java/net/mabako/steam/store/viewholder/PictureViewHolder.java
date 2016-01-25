package net.mabako.steam.store.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.core.R;
import net.mabako.steam.store.data.Picture;

public class PictureViewHolder extends RecyclerView.ViewHolder {
    private final Context context;

    public PictureViewHolder(View itemView, Context context) {
        super(itemView);
        this.context = context;
    }

    public void setFrom(Picture picture) {
        Picasso.with(context).load(picture.getUrl()).into((ImageView) itemView.findViewById(R.id.image));
    }
}
