package net.mabako.steam.store;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import net.mabako.steam.store.data.Picture;
import net.mabako.steam.store.data.Text;
import net.mabako.steamgifts.core.R;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class StoreAppFragment extends StoreFragment {
    public static StoreAppFragment newInstance(int appId) {
        StoreAppFragment fragment = new StoreAppFragment();

        Bundle args = new Bundle();
        args.putString("app", String.valueOf(appId));
        fragment.setArguments(args);

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

                        // Game name
                        adapter.add(new Text("<h1>" + TextUtils.htmlEncode(data.getString("name")) + "</h1>", true));

                        // Game description.
                        adapter.add(new Text(data.getString("about_the_game"), true));

                        // Release?
                        adapter.add(new Text("<strong>Release:</strong> " + data.getJSONObject("release_date").getString("date"), true));

                        // Genres
                        JSONArray genres = data.getJSONArray("genres");
                        if (genres.length() > 0) {
                            StringBuilder sb = new StringBuilder("<strong>Genre:</strong> ");
                            for (int i = 0; i < genres.length(); ++i) {
                                if (i > 0)
                                    sb.append(", ");

                                sb.append(genres.getJSONObject(i).getString("description"));
                            }
                            adapter.add(new Text(sb.toString(), true));
                        }

                        // Space!
                        adapter.add(null);

                        // Some screenshots
                        JSONArray screenshots = data.getJSONArray("screenshots");
                        for (int i = 0; i < screenshots.length(); ++i) {
                            JSONObject screenshot = screenshots.getJSONObject(i);

                            // TODO higher quality images?
                            adapter.add(new Picture(screenshot.getString("path_thumbnail")));
                        }
                    } else throw new Exception("not successful");
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to load Store App", Toast.LENGTH_LONG).show();
                    loaded = false;
                }
            } else {
                Toast.makeText(getContext(), "Unable to load Store App", Toast.LENGTH_LONG).show();
                loaded = false;
            }

            getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }
}
