package net.mabako.steamgifts.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.activities.WebViewActivity;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.tasks.AjaxTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class SyncFragment extends Fragment {
    private static final String TAG = SyncFragment.class.getSimpleName();

    private SyncTask syncTask;
    private LoadSyncDetailsTask loadSyncDetailsTask;
    private String xsrfToken;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync, container, false);

        if (!SteamGiftsUserData.getCurrent().isLoggedIn()) {
            getActivity().setResult(Activity.RESULT_CANCELED);
            getActivity().finish();

            return view;
        } else {
            View syncView = view.findViewById(R.id.sync_now);
            syncView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    syncTask = new SyncTask(SyncFragment.this, xsrfToken);
                    syncTask.execute();
                }
            });
            syncView.setEnabled(false);

            view.findViewById(R.id.privacy_settings).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), WebViewActivity.class);
                    intent.putExtra(WebViewActivity.ARG_URL, "http://steamcommunity.com/my/edit/settings");

                    getActivity().startActivity(intent);
                }
            });

            loadSyncDetailsTask = new LoadSyncDetailsTask(this);
            loadSyncDetailsTask.execute();

            return view;
        }
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;

        getView().findViewById(R.id.sync_now).setEnabled(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (syncTask != null) {
            syncTask.cancel(true);
            syncTask = null;
        }

        if (loadSyncDetailsTask != null) {
            loadSyncDetailsTask.cancel(true);
            loadSyncDetailsTask = null;
        }
    }

    /**
     * Fetch the XSRF token needed to sync.
     */
    private static class LoadSyncDetailsTask extends AsyncTask<Void, Void, String> {
        private static final String TAG = LoadSyncDetailsTask.class.getSimpleName();
        private final SyncFragment fragment;

        private LoadSyncDetailsTask(SyncFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.d(TAG, "Fetching sync details");

            try {
                // Fetch the Giveaway page

                Connection jsoup = Jsoup.connect("http://www.steamgifts.com/account/profile/sync");
                jsoup.cookie("PHPSESSID", SteamGiftsUserData.getCurrent().getSessionId());
                Document document = jsoup.get();

                SteamGiftsUserData.extract(document);

                // Fetch the xsrf token
                Element xsrfToken = document.select("input[name=xsrf_token]").first();
                if (xsrfToken != null)
                    return xsrfToken.attr("value");
            } catch (Exception e) {
                Log.e(TAG, "Error fetching URL", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s != null) {
                fragment.setXsrfToken(s);
            } else {
                Toast.makeText(fragment.getContext(), "Error fetching xsrf token?", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Actually sync!
     */
    private static class SyncTask extends AjaxTask<SyncFragment> {
        private static final String TAG = SyncTask.class.getSimpleName();

        SyncTask(SyncFragment fragment, String xsrfToken) {
            super(fragment, xsrfToken, "sync");
        }

        @Override
        protected void addExtraParameters(Connection connection) {

        }

        @Override
        protected void onPostExecute(Connection.Response response) {
            if (response.statusCode() == 200) {
                try {
                    Log.v(TAG, "Response to JSON request: " + response.body());
                    JSONObject root = new JSONObject(response.body());

                    if ("success".equals(root.getString("type"))) {
                        Activity activity = getFragment().getActivity();
                        activity.setResult(CommonActivity.RESPONSE_SYNC_SUCCESSFUL);
                        activity.finish();
                    } else {
                        String message = root.getString("msg");
                        if (message != null) {
                            Toast.makeText(getFragment().getContext(), message, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getFragment().getContext(), "Could not sync.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    return;
                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse JSON object", e);
                }
            }
        }
    }
}
