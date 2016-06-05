package net.mabako.steamgifts.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.mabako.steamgifts.ApplicationTemplate;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.core.BuildConfig;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicDiscussion;

public class AboutFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_about, container, false);

        TextView versionText = (TextView) layout.findViewById(R.id.version);
        versionText.setText(String.format("Version %s (%s)", ((ApplicationTemplate) getActivity().getApplication()).getAppVersionName(), ((ApplicationTemplate) getActivity().getApplication()).getFlavor()));
        versionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), String.format("Build %d", ((ApplicationTemplate) getActivity().getApplication()).getAppVersionCode()), Toast.LENGTH_SHORT).show();
            }
        });

        layout.findViewById(R.id.forum_thread).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(DiscussionDetailFragment.ARG_DISCUSSION, new BasicDiscussion("Zpeq5"));
                getActivity().startActivity(intent);
            }
        });

        layout.findViewById(R.id.mail).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = "lizacarvelli+steamgifts+" + BuildConfig.VERSION_CODE + "@gmail.com";

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setType("text/plain");
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{email});
                intent.putExtra(Intent.EXTRA_SUBJECT, "SteamGifts for Android Feedback");
                try {
                    startActivity(intent);
                } catch (android.content.ActivityNotFoundException ex) {
                    Snackbar.make(getActivity().findViewById(R.id.fragment_container), "No mail clients installed", Snackbar.LENGTH_LONG);
                }
            }
        });

        layout.findViewById(R.id.source).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/SteamGifts/SteamGifts"));

                startActivity(intent);
            }
        });

        View paypalDonations = layout.findViewById(R.id.donate_via_paypal);
        if (((ApplicationTemplate) getActivity().getApplication()).allowPaypalDonations()) {
            paypalDonations.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=Y6WJZY2VJHC6G"));

                    startActivity(intent);
                }
            });
        } else {
            paypalDonations.setVisibility(View.GONE);
        }

        return layout;
    }
}
