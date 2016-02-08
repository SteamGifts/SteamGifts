package net.mabako.steam.store;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import net.mabako.Constants;
import net.mabako.steam.store.data.Picture;
import net.mabako.steam.store.data.Space;
import net.mabako.steam.store.data.Text;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

public class StoreAppFragment extends StoreFragment {
    private static final String TAG = StoreAppFragment.class.getSimpleName();

    public static StoreAppFragment newInstance(int appId, boolean refreshOnCreate) {
        StoreAppFragment fragment = new StoreAppFragment();

        Bundle args = new Bundle();
        args.putString("app", String.valueOf(appId));
        args.putBoolean("refresh", refreshOnCreate);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getArguments().getBoolean("refresh", false))
            refresh();
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadAppTask();
    }

    private class LoadAppTask extends LoadStoreTask {
        @Override
        protected Connection getConnection() {
            return Jsoup
                    .connect("http://store.steampowered.com/api/appdetails/")
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT)
                    .data("appids", getArguments().getString("app"))
                    .data("l", "en");
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    JSONObject sub = jsonObject.getJSONObject(getArguments().getString("app"));

                    // Were we successful in fetching the details?
                    if (sub.getBoolean("success")) {
                        JSONObject data = sub.getJSONObject("data");

                        List<IEndlessAdaptable> items = new ArrayList<IEndlessAdaptable>();

                        // Game name
                        items.add(new Text("<h1>" + TextUtils.htmlEncode(data.getString("name")) + "</h1>", true));

                        // Game description.
                        if (data.has("about_the_game"))
                            items.add(new Text(data.getString("about_the_game"), true));

                        // Release?
                        if (data.has("release_date"))
                            items.add(new Text("<strong>Release:</strong> " + data.getJSONObject("release_date").getString("date"), true, true));

                        // Genres
                        if (data.has("genres")) {
                            JSONArray genres = data.getJSONArray("genres");
                            if (genres.length() > 0) {
                                StringBuilder sb = new StringBuilder("<strong>Genre:</strong> ");
                                for (int i = 0; i < genres.length(); ++i) {
                                    if (i > 0)
                                        sb.append(", ");

                                    sb.append(genres.getJSONObject(i).getString("description"));
                                }
                                items.add(new Text(sb.toString(), true));
                            }
                        }

                        // Space!
                        items.add(new Space());

                        // Some screenshots
                        if (data.has("screenshots")) {
                            JSONArray screenshots = data.getJSONArray("screenshots");
                            for (int i = 0; i < screenshots.length(); ++i) {
                                items.add(new Picture(screenshots.getJSONObject(i).getString("path_thumbnail")));
                            }
                        }

                        if (data.has("legal_notice"))
                            items.add(new Text(data.getString("legal_notice"), true, R.layout.endless_scroll_end, false));

                        addItems(items, true);
                    } else throw new Exception("not successful");
                } catch (Exception e) {
                    Log.e(TAG, "Exception during loading store app", e);
                    Toast.makeText(getContext(), "Unable to load Store App", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "no JSON object");
                Toast.makeText(getContext(), "Unable to load Store App", Toast.LENGTH_LONG).show();
            }

            getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }
}
