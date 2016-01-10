package net.mabako.store;

import android.widget.Toast;

import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class StoreAppFragment extends StoreFragment {
    public static StoreAppFragment newInstance(int appId) {
        StoreAppFragment fragment = new StoreAppFragment();
        fragment.appId = appId;
        return fragment;
    }

    @Override
    public LoadStoreTask getTaskToStart() {
        return new LoadAppTask();
    }

    private class LoadAppTask extends LoadStoreTask {
        @Override
        protected Connection getConnection() {
            return Jsoup
                    .connect("http://store.steampowered.com/api/appdetails/")
                    .data("appids", String.valueOf(appId))
                    .data("l", "en");

        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {

                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to load Store App", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "Unable to load Store App", Toast.LENGTH_LONG).show();
            }
        }
    }
}
