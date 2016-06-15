package net.mabako.steamgifts.adapters.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Trade;
import net.mabako.steamgifts.fragments.TradeDetailFragment;

import java.util.Locale;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class TradeListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private final EndlessAdapter adapter;
    private final Activity activity;

    private final View itemContainer;
    private final TextView tradeTitle;
    private final TextView tradeAuthor;
    private final ImageView tradeAuthorAvatar;
    private final TextView tradeTime;
    private final TextView tradeScoreNegative;
    private final TextView tradeScorePositive;

    public TradeListItemViewHolder(View itemView, Activity activity, EndlessAdapter adapter) {
        super(itemView);
        this.adapter = adapter;
        this.activity = activity;

        itemContainer = itemView.findViewById(R.id.list_item);
        tradeTitle = (TextView) itemView.findViewById(R.id.trade_title);
        tradeAuthor = (TextView) itemView.findViewById(R.id.trade_author);
        tradeAuthorAvatar = (ImageView) itemView.findViewById(R.id.author_avatar);
        tradeTime = (TextView) itemView.findViewById(R.id.trade_time);

        tradeScorePositive = (TextView) itemView.findViewById(R.id.trade_score_positive);
        tradeScoreNegative = (TextView) itemView.findViewById(R.id.trade_score_negative);

        itemView.setOnClickListener(this);
    }

    @SuppressWarnings("deprecation")
    public void setFrom(Trade trade) {
        String title = trade.getTitle();
        if (title.contains("[H]") && title.contains("[W]"))
            title = title.replace("[W]", "\n[W]");
        else if (title.contains("(H)") && title.contains("(W)"))
            title = title.replace("(W)", "\n(W)");
        tradeTitle.setText(title);

        tradeAuthor.setText(trade.getCreator());
        tradeTime.setText(trade.getRelativeCreatedTime(activity));

        tradeScorePositive.setText(String.format(Locale.ENGLISH, "+%d", trade.getCreatorScorePositive()));
        tradeScoreNegative.setText(String.format(Locale.ENGLISH, "-%d", trade.getCreatorScoreNegative()));

        StringUtils.setBackgroundDrawable(activity, itemContainer, trade.isLocked());
        Picasso.with(activity).load(trade.getCreatorAvatar()).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into(tradeAuthorAvatar);
    }

    @Override
    public void onClick(View v) {
        Trade trade = (Trade) adapter.getItem(getAdapterPosition());

        Intent intent = new Intent(activity, DetailActivity.class);
        intent.putExtra(TradeDetailFragment.ARG_TRADE, trade);

        activity.startActivityForResult(intent, CommonActivity.REQUEST_LOGIN_PASSIVE);
    }
}
