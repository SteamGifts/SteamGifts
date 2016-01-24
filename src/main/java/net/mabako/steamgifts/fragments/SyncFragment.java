package net.mabako.steamgifts.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.activities.WebViewActivity;

public class SyncFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync, container, false);

        view.findViewById(R.id.sync_now).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();
                activity.setResult(CommonActivity.RESPONSE_SYNC_SUCCESSFUL);
                activity.finish();
            }
        });

        view.findViewById(R.id.privacy_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WebViewActivity.class);
                intent.putExtra(WebViewActivity.ARG_URL, "http://steamcommunity.com/my/edit/settings");

                getActivity().startActivity(intent);
            }
        });

        return view;
    }
}
