package net.mabako.steamgifts.adapters.viewholder;

import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.BaseActivity;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.GiveawayExtras;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.WriteCommentFragment;
import net.mabako.steamgifts.fragments.util.GiveawayDetailsCard;
import net.mabako.steamgifts.web.WebUserData;

public class GiveawayCardViewHolder extends RecyclerView.ViewHolder {
    private GiveawayDetailFragment fragment;

    private final View progressBar;
    private final TextView user;
    private final TextView timeRemaining;
    private final TextView description;

    private final Button enterGiveaway;
    private final Button leaveGiveaway;
    private final Button commentGiveaway;
    private final Button loginButton;
    private final Button errorMessage;

    public GiveawayCardViewHolder(View v, final GiveawayDetailFragment fragment) {
        super(v);
        this.fragment = fragment;

        progressBar = v.findViewById(R.id.progressBar);
        user = (TextView) v.findViewById(R.id.user);
        timeRemaining = (TextView) v.findViewById(R.id.remaining);
        description = (TextView) v.findViewById(R.id.description);

        enterGiveaway = (Button) v.findViewById(R.id.enter);
        leaveGiveaway = (Button) v.findViewById(R.id.leave);
        commentGiveaway = (Button) v.findViewById(R.id.comment);
        errorMessage = (Button) v.findViewById(R.id.error);
        loginButton = (Button) v.findViewById(R.id.login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseActivity) fragment.getActivity()).requestLogin();
            }
        });
    }

    public void setFrom(final GiveawayDetailsCard card) {
        Giveaway giveaway = card.getGiveaway();
        GiveawayExtras extras = card.getExtras();

        user.setText("{faw-user} " + giveaway.getCreator());
        timeRemaining.setText("{faw-clock-o} " + giveaway.getTimeRemaining());

        enterGiveaway.setText(String.format(String.valueOf(itemView.getContext().getText(R.string.enter_giveaway)), giveaway.getPoints()));
        leaveGiveaway.setText(String.format(String.valueOf(itemView.getContext().getText(R.string.leave_giveaway)), giveaway.getPoints()));

        for (View view : new View[]{enterGiveaway, leaveGiveaway, commentGiveaway, loginButton, errorMessage, description})
            view.setVisibility(View.GONE);

        if (extras == null) {
            // Still loading...
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);

            if (extras.getDescription() != null) {
                CharSequence desc = Html.fromHtml(extras.getDescription());
                desc = desc.subSequence(0, desc.length() - 2);

                description.setText(desc);
                description.setVisibility(View.VISIBLE);
                description.setMovementMethod(LinkMovementMethod.getInstance());

                description.setVisibility(View.VISIBLE);
            }

            if (extras.getXsrfToken() != null) {
                if (!extras.isEntered())
                    enterGiveaway.setVisibility(View.VISIBLE);
                else
                    leaveGiveaway.setVisibility(View.VISIBLE);

                commentGiveaway.setVisibility(View.VISIBLE);
            } else if (extras.getErrorMessage() != null) {
                errorMessage.setText(extras.getErrorMessage());
                errorMessage.setVisibility(View.VISIBLE);
            } else if (!WebUserData.getCurrent().isLoggedIn()) {
                loginButton.setVisibility(View.VISIBLE);
            }

            enterGiveaway.setEnabled(true);
            leaveGiveaway.setEnabled(true);
        }

        enterGiveaway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterGiveaway.setEnabled(false);
                fragment.requestEnterLeave(GiveawayDetailFragment.ENTRY_INSERT);
            }
        });

        leaveGiveaway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leaveGiveaway.setEnabled(false);
                fragment.requestEnterLeave(GiveawayDetailFragment.ENTRY_DELETE);
            }
        });

        commentGiveaway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = fragment.getActivity().getSupportFragmentManager();
                WriteCommentFragment wcf = WriteCommentFragment.newInstance(card.getGiveaway().getGiveawayId(), card.getGiveaway().getName(), card.getExtras().getXsrfToken());
                wcf.show(fm, "writecomment");
            }
        });
    }
}
