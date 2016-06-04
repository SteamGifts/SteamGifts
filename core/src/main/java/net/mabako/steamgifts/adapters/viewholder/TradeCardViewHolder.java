package net.mabako.steamgifts.adapters.viewholder;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Trade;
import net.mabako.steamgifts.data.TradeExtras;
import net.mabako.steamgifts.fragments.TradeDetailFragment;
import net.mabako.steamgifts.fragments.interfaces.ICommentableFragment;
import net.mabako.steamgifts.fragments.util.TradeDetailsCard;

import java.util.Locale;

public class TradeCardViewHolder extends RecyclerView.ViewHolder {
    private final ICommentableFragment fragment;
    private final Context context;

    private final View progressBar;
    private final TextView tradeTime;
    private final TextView user;
    private final TextView description;
    private final View separator;
    private final View actionSeparator;
    private final TextView title;

    private final Button commentDiscussion;

    private final View scoreDivider;
    private final TextView scorePositive;
    private final TextView scoreNegative;

    public TradeCardViewHolder(View v, TradeDetailFragment fragment, Context context) {
        super(v);
        this.fragment = fragment;
        this.context = context;

        progressBar = v.findViewById(R.id.progressBar);
        user = (TextView) v.findViewById(R.id.user);
        tradeTime = (TextView) v.findViewById(R.id.time);
        description = (TextView) v.findViewById(R.id.description);
        description.setMovementMethod(LinkMovementMethod.getInstance());
        separator = v.findViewById(R.id.separator);
        actionSeparator = v.findViewById(R.id.action_separator);
        title = (TextView) v.findViewById(R.id.trade_title);

        scoreDivider = v.findViewById(R.id.trade_divider);
        scorePositive = (TextView) v.findViewById(R.id.trade_score_positive);
        scoreNegative = (TextView) v.findViewById(R.id.trade_score_negative);

        commentDiscussion = (Button) v.findViewById(R.id.comment);
    }

    public void setFrom(final TradeDetailsCard card) {
        final Trade trade = card.getTrade();
        TradeExtras extras = card.getExtras();

        for (View view : new View[]{commentDiscussion, description, tradeTime, user, scoreDivider, scorePositive, scoreNegative, title, separator, actionSeparator})
            view.setVisibility(View.GONE);

        if (trade == null) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            for (View view : new View[]{tradeTime, user, scoreDivider, scorePositive, scoreNegative, title})
                view.setVisibility(View.VISIBLE);

            title.setText(trade.getTitle());

            user.setText("{faw-user} " + trade.getCreator());
            user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.showProfile(trade.getCreator());
                }
            });
            scorePositive.setText(String.format(Locale.ENGLISH, "+%d", trade.getCreatorScorePositive()));
            scoreNegative.setText(String.format(Locale.ENGLISH, "-%d", trade.getCreatorScoreNegative()));
            tradeTime.setText("{faw-calendar-o} " + trade.getRelativeCreatedTime(context));

            if (extras == null) {
                // Still loading...
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);

                if (!TextUtils.isEmpty(extras.getDescription())) {
                    description.setText(StringUtils.fromHtml(((Fragment) fragment).getActivity(), extras.getDescription()));
                    description.setVisibility(View.VISIBLE);
                    separator.setVisibility(View.VISIBLE);
                }

                if (extras.getXsrfToken() != null) {
                    commentDiscussion.setVisibility(View.VISIBLE);
                    actionSeparator.setVisibility(View.VISIBLE);
                }
            }

            commentDiscussion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.requestComment(null);
                }
            });
        }

        AttachedImageUtils.setFrom(itemView, extras, (CommonActivity) (((Fragment) fragment).getActivity()));
    }
}
