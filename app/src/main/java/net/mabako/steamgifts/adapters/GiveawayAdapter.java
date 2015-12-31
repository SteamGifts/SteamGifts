package net.mabako.steamgifts.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.activities.MainActivity;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;

import java.util.ArrayList;
import java.util.List;

public class GiveawayAdapter extends EndlessAdapter<Giveaway, GiveawayAdapter.ViewHolder> {
    private static final int ITEMS_PER_PAGE = 50;
    private Activity context;
    public GiveawayAdapter(Activity context, RecyclerView view, OnLoadListener listener) {
        super(view, listener);
        this.context = context;
    }

    @Override
    protected ViewHolder onCreateActualViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.giveaway_item, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindActualViewHolder(ViewHolder holder, int position) {
        Giveaway giveaway = getItem(position);

        holder.giveawayName.setText(giveaway.getTitle());
        Picasso.with(context).load("http://cdn.akamai.steamstatic.com/steam/" + giveaway.getType().name().toLowerCase() + "s/" + giveaway.getGameId() + "/capsule_184x69.jpg").into(holder.giveawayImage);

        String str = giveaway.getPoints() + "P | " + giveaway.getEntries() + " entries";
        if (giveaway.getCopies() > 1)
            str = giveaway.getCopies() + " copies | " + str;
        holder.giveawayDetails.setText(str);
    }

    @Override
    protected boolean hasEnoughItems(List<Giveaway> items) {
        return items.size() == ITEMS_PER_PAGE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView giveawayDetails;
        private TextView giveawayName;
        private ImageView giveawayImage;

        public ViewHolder(View v) {
            super(v);
            giveawayName = (TextView) v.findViewById(R.id.giveaway_name);
            giveawayDetails = (TextView) v.findViewById(R.id.giveaway_details);
            giveawayImage = (ImageView) v.findViewById(R.id.giveaway_image);

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Giveaway giveaway = getItem(getAdapterPosition());

            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, giveaway);

            context.startActivityForResult(intent, MainActivity.REQUEST_LOGIN_PASSIVE);
        }
    }
}
