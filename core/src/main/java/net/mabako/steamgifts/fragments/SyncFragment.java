package net.mabako.steamgifts.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import net.mabako.Constants;
import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.activities.UrlHandlingActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;
import net.mabako.steamgifts.tasks.AjaxTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class SyncFragment extends Fragment {
    private static final String SAVED_XSRF = "xsrf";

    private SyncTask syncTask;
    private LoadSyncDetailsTask loadSyncDetailsTask;

    private String xsrfToken;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            xsrfToken = savedInstanceState.getString(SAVED_XSRF);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(SAVED_XSRF, xsrfToken);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sync, container, false);

        if (!SteamGiftsUserData.getCurrent(getContext()).isLoggedIn()) {
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
                    UrlHandlingActivity.getIntentForUri(getContext(), Uri.parse("http://steamcommunity.com/my/edit/settings"), true).start(getActivity());
                }
            });

            loadSyncDetailsTask = new LoadSyncDetailsTask(this);
            loadSyncDetailsTask.execute();

            return view;
        }
    }

    public void onSyncDetailsLoaded(String xsrfToken, String lastSyncTime) {
        this.xsrfToken = xsrfToken;

        getView().findViewById(R.id.sync_now).setEnabled(true);

        TextView lastSyncView = (TextView) getView().findViewById(R.id.sync_time);
        lastSyncView.setVisibility(lastSyncTime == null ? View.GONE : View.VISIBLE);
        lastSyncView.setText(lastSyncTime);

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
    private static class LoadSyncDetailsTask extends AsyncTask<Void, Void, String[]> {
        private static final String TAG = LoadSyncDetailsTask.class.getSimpleName();
        private final SyncFragment fragment;

        private LoadSyncDetailsTask(SyncFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected String[] doInBackground(Void... params) {
            Log.d(TAG, "Fetching sync details");

            try {
                // Fetch the Giveaway page

                Connection jsoup = Jsoup.connect("https://www.steamgifts.com/account/profile/sync")
                        .userAgent(Constants.JSOUP_USER_AGENT)
                        .timeout(Constants.JSOUP_TIMEOUT)
                        .cookie("PHPSESSID", SteamGiftsUserData.getCurrent(fragment.getContext()).getSessionId());
                Document document = jsoup.get();

                SteamGiftsUserData.extract(fragment.getContext(), document);

                // Fetch the xsrf token
                Element xsrfToken = document.select("input[name=xsrf_token]").first();
                Element lastSyncTime = document.select(".form__sync-data .notification").first();
                if (xsrfToken != null) {
                    return new String[]{xsrfToken.attr("value"), lastSyncTime == null ? null : lastSyncTime.text()};
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching URL", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String[] s) {
            if (s != null) {
                fragment.onSyncDetailsLoaded(s[0], s[1]);
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
            super(fragment, fragment.getContext(), xsrfToken, "sync");
        }

        @Override
        protected void addExtraParameters(Connection connection) {

        }

        @Override
        protected void onPostExecute(Connection.Response response) {
            if (response != null && response.statusCode() == 200) {
                try {
                    Log.v(TAG, "Response to JSON request: " + response.body());
                    JSONObject root = new JSONObject(response.body());

                    if ("success".equals(root.getString("type"))) {
                        Activity activity = getFragment().getActivity();
                        activity.setResult(CommonActivity.RESPONSE_SYNC_SUCCESSFUL);
                        activity.finish();
                        return;
                    } else {
                        String message = root.getString("msg");
                        if (message != null) {
                            Toast.makeText(getFragment().getContext(), message, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                } catch (JSONException e) {
                    Log.e(TAG, "Failed to parse JSON object", e);
                }
            }
            Toast.makeText(getFragment().getContext(), "Could not sync.", Toast.LENGTH_SHORT).show();
        }
    }
}
