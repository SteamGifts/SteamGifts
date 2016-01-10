package net.mabako.steamgifts.adapters.viewholder;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.activities.ViewGroupsActivity;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.GiveawayExtras;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.util.GiveawayDetailsCard;
import net.mabako.steamgifts.web.SteamGiftsUserData;

import java.util.ArrayList;
import java.util.List;

public class GiveawayCardViewHolder extends RecyclerView.ViewHolder {
    private GiveawayDetailFragment fragment;

    private final View progressBar;
    private final TextView user;
    private final TextView timeRemaining;
    private final TextView timeCreated;
    private final TextView description;

    private final Button enterGiveaway;
    private final Button leaveGiveaway;
    private final Button commentGiveaway;
    private final Button loginButton;
    private final Button errorMessage;
    private final Button indicator;
    private final View separator;

    public GiveawayCardViewHolder(View v, final GiveawayDetailFragment fragment) {
        super(v);
        this.fragment = fragment;

        progressBar = v.findViewById(R.id.progressBar);
        user = (TextView) v.findViewById(R.id.user);
        timeRemaining = (TextView) v.findViewById(R.id.remaining);
        timeCreated = (TextView) v.findViewById(R.id.created);
        description = (TextView) v.findViewById(R.id.description);
        description.setMovementMethod(LinkMovementMethod.getInstance());
        separator = v.findViewById(R.id.separator);

        enterGiveaway = (Button) v.findViewById(R.id.enter);
        leaveGiveaway = (Button) v.findViewById(R.id.leave);
        commentGiveaway = (Button) v.findViewById(R.id.comment);
        errorMessage = (Button) v.findViewById(R.id.error);
        loginButton = (Button) v.findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CommonActivity) fragment.getActivity()).requestLogin();
            }
        });
        indicator = (Button) v.findViewById(R.id.indicator);
    }

    public void setFrom(final GiveawayDetailsCard card) {
        final Giveaway giveaway = card.getGiveaway();
        final GiveawayExtras extras = card.getExtras();

        for (View view : new View[]{enterGiveaway, leaveGiveaway, commentGiveaway, loginButton, errorMessage, description, indicator, user, timeRemaining, timeCreated, separator})
            view.setVisibility(View.GONE);

        if (giveaway == null) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            user.setText("{faw-user} " + giveaway.getCreator());
            user.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.showProfile(giveaway.getCreator());
                }
            });

            timeRemaining.setText("{faw-clock-o} " + giveaway.getTimeRemaining());
            timeCreated.setText("{faw-calendar-o} " + giveaway.getTimeCreated());
            for (View view : new View[]{user, timeRemaining, timeCreated, separator})
                view.setVisibility(View.VISIBLE);

            enterGiveaway.setText(String.format(String.valueOf(itemView.getContext().getText(R.string.enter_giveaway_with_points)), giveaway.getPoints()));
            leaveGiveaway.setText(String.format(String.valueOf(itemView.getContext().getText(R.string.leave_giveaway_with_points)), giveaway.getPoints()));

            if (extras == null) {
                // Still loading...
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);

                if (extras.getDescription() != null) {
                    description.setText(Utils.fromHtml(fragment.getActivity(), extras.getDescription()));
                    description.setVisibility(View.VISIBLE);
                }

                if (extras.getXsrfToken() != null && extras.getErrorMessage() == null) {
                    if (!extras.isEntered())
                        enterGiveaway.setVisibility(View.VISIBLE);
                    else
                        leaveGiveaway.setVisibility(View.VISIBLE);
                } else if (extras.getErrorMessage() != null) {
                    errorMessage.setText(extras.getErrorMessage());
                    errorMessage.setVisibility(View.VISIBLE);
                } else if (!SteamGiftsUserData.getCurrent().isLoggedIn()) {
                    loginButton.setVisibility(View.VISIBLE);
                }

                if (extras.getXsrfToken() != null)
                    commentGiveaway.setVisibility(View.VISIBLE);

                enterGiveaway.setEnabled(true);
                leaveGiveaway.setEnabled(true);

                setupIndicators(giveaway);
            }

            enterGiveaway.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    enterGiveaway.setEnabled(false);
                    fragment.requestEnterLeave(giveaway.getGiveawayId(), GiveawayDetailFragment.ENTRY_INSERT, extras.getXsrfToken());
                }
            });

            leaveGiveaway.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    leaveGiveaway.setEnabled(false);
                    fragment.requestEnterLeave(giveaway.getGiveawayId(), GiveawayDetailFragment.ENTRY_DELETE, extras.getXsrfToken());
                }
            });

            commentGiveaway.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.requestComment(null);
                }
            });
        }
    }

    private void setupIndicators(final Giveaway giveaway) {
        List<Spannable> spans = new ArrayList<>();

        if (giveaway.isPrivate())
            spans.add(new SpannableString("{faw-lock} "));

        if (giveaway.isWhitelist())
            spans.add(new SpannableString("{faw-heart} "));

        if (giveaway.isGroup())
            spans.add(new SpannableString("{faw-users} "));

        if (giveaway.isLevelPositive())
            spans.add(new SpannableString("L" + giveaway.getLevel()));

        if (giveaway.isLevelNegative()) {
            Spannable span = new SpannableString("L" + giveaway.getLevel());
            span.setSpan(new ForegroundColorSpan(fragment.getResources().getColor(R.color.giveawayIndicatorColorLevelTooHigh)), 0, span.toString().length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
            spans.add(span);
        }

        if (!spans.isEmpty()) {
            indicator.setVisibility(View.VISIBLE);

            CharSequence text = TextUtils.concat(spans.toArray(new Spannable[0]));
            indicator.setText(text);

            if (giveaway.isGroup()) {
                indicator.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(fragment.getContext(), ViewGroupsActivity.class);
                        intent.putExtra(ViewGroupsActivity.TITLE, giveaway.getTitle());
                        intent.putExtra(ViewGroupsActivity.PATH, giveaway.getGiveawayId() + "/" + giveaway.getName());

                        fragment.getActivity().startActivityForResult(intent, CommonActivity.REQUEST_LOGIN_PASSIVE);
                    }
                });
            } else {
                indicator.setOnClickListener(null);
            }
        }
    }
}
