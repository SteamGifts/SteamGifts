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

import net.mabako.steamgifts.BuildConfig;
import net.mabako.steamgifts.R;

public class AboutFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.fragment_about, container, false);

        ((TextView) layout.findViewById(R.id.version)).setText(String.format("Version %s (Build %d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

        layout.findViewById(R.id.contact).setOnClickListener(new View.OnClickListener() {
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
                intent.setData(Uri.parse("https://github.com/mabako/SteamGifts-app"));

                startActivity(intent);
            }
        });

        return layout;
    }
}
