package net.mabako.steamgifts.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.GiveawayExtras;
import net.mabako.steamgifts.tasks.LoadGiveawayDetailsTask;

import org.w3c.dom.Text;

public class GiveawayDetailFragment extends Fragment {
    private static final String TAG = GiveawayDetailFragment.class.getSimpleName();

    public static final String ARG_GIVEAWAY = "giveaway";

    /**
     * Content to show for the giveaway details.
     */
    private Giveaway giveaway;

    private LoadGiveawayDetailsTask task;
    private TextView enterGiveaway;
    private TextView leaveGiveaway;
    private TextView timeRemaining;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_GIVEAWAY)) {
            giveaway = (Giveaway) getArguments().getSerializable(ARG_GIVEAWAY);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CoordinatorLayout layout = (CoordinatorLayout) super.onCreateView(inflater, container, savedInstanceState);

        Activity activity = getActivity();
        final CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        appBarLayout.setTitle(giveaway.getTitle());

        ImageView toolbarImage = (ImageView) activity.findViewById(R.id.toolbar_image);
        if (toolbarImage != null) {
            Picasso.with(getContext()).load("http://cdn.akamai.steamstatic.com/steam/" + giveaway.getType().name().toLowerCase() + "s/" + giveaway.getGameId() + "/header.jpg").into(toolbarImage, new Callback() {
                @Override
                public void onSuccess() {
                    appBarLayout.setExpandedTitleTextAppearance(R.style.TransparentText);
                }

                @Override
                public void onError() {

                }
            });
        }

        enterGiveaway = (TextView) getActivity().findViewById(R.id.enter);
        leaveGiveaway = (TextView) getActivity().findViewById(R.id.leave);
        timeRemaining = (TextView) getActivity().findViewById(R.id.remaining);

        if (giveaway != null) {
            setupGiveawayCard();
        }

        task = new LoadGiveawayDetailsTask(this, giveaway.getGiveawayId());
        task.execute();

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        task.cancel(true);
    }

    private void setupGiveawayCard() {
        // Format the "Enter (__P)" text to include the points
        enterGiveaway.setText(String.format(getString(R.string.enter_giveaway), giveaway.getPoints()));
        leaveGiveaway.setText(String.format(getString(R.string.leave_giveaway), giveaway.getPoints()));

        // Show the creator
        ((TextView) getActivity().findViewById(R.id.user)).setText("{faw-user} " + giveaway.getCreator());

        // Time remaining
        toggleRemainingTime();
        timeRemaining.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleRemainingTime();
            }
        });


        // TODO: Add comment

        // Link to Steam
        getActivity().findViewById(R.id.steam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://store.steampowered.com/" + giveaway.getType().name().toLowerCase() + "/" + giveaway.getGameId() + "/"));
                startActivity(intent);
            }
        });
    }

    private void toggleRemainingTime() {
        String shortVariant = giveaway.getTimeRemaining();
        String longVariant = giveaway.getTimeRemainingLong();

        Log.d(TAG, "Current time text: " + timeRemaining.getText());
        timeRemaining.setText("{faw-clock-o} " + (timeRemaining.getText().toString().contains(shortVariant) ? longVariant : shortVariant));
    }

    public void setExtras(GiveawayExtras extras) {
        getActivity().findViewById(R.id.progressBar).setVisibility(View.GONE);
        if (extras.getDescription() != null) {
            TextView description = (TextView) getActivity().findViewById(R.id.description);

            CharSequence desc = Html.fromHtml(extras.getDescription());
            desc = desc.subSequence(0, desc.length() - 2);

            description.setText(desc);
            description.setVisibility(View.VISIBLE);
            description.setMovementMethod(LinkMovementMethod.getInstance());
        }

        // We have a token to send with an ajax request now, so we can possibly enter or leave this giveaway.
        if(extras.getXsrfToken() != null) {
            getActivity().findViewById(R.id.comment).setVisibility(View.VISIBLE);

            if(extras.isEntered())
                leaveGiveaway.setVisibility(View.VISIBLE);
            else
                enterGiveaway.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "No XSRF Token for Giveaway...");
        }
    }
}
