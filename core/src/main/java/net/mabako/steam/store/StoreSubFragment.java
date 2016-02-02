package net.mabako.steam.store;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import net.mabako.Constants;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Game;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;

public class StoreSubFragment extends StoreFragment {
    private static final String TAG = StoreSubFragment.class.getSimpleName();

    public static StoreSubFragment newInstance(int appId) {
        StoreSubFragment fragment = new StoreSubFragment();

        Bundle args = new Bundle();
        args.putString("sub", String.valueOf(appId));
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadSubTask();
    }

    public void showDetails(int appId) {
        DetailActivity activity = (DetailActivity) getActivity();
        activity.setTransientFragment(StoreAppFragment.newInstance(appId, true));
    }

    private class LoadSubTask extends LoadStoreTask {
        @Override
        protected Connection getConnection() {
            return Jsoup
                    .connect("http://store.steampowered.com/api/packagedetails/")
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT)
                    .data("packageids", getArguments().getString("sub"))
                    .data("l", "en");
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    JSONObject sub = jsonObject.getJSONObject(getArguments().getString("sub"));

                    // Were we successful in fetching the details?
                    if (sub.getBoolean("success")) {
                        JSONObject data = sub.getJSONObject("data");
                        JSONArray apps = data.getJSONArray("apps");

                        List<IEndlessAdaptable> games = new ArrayList<>();

                        for (int i = 0; i < apps.length(); ++i) {
                            JSONObject app = apps.getJSONObject(i);

                            Game game = new Game();
                            game.setType(Game.Type.APP);
                            game.setGameId(app.getInt("id"));
                            game.setName(app.getString("name"));

                            games.add(game);
                        }

                        addItems(games, true);
                    } else throw new Exception("not successful");
                } catch (Exception e) {
                    Log.e(TAG, "Exception during loading store sub", e);
                    Toast.makeText(getContext(), "Unable to load Store Sub", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "Unable to load Store Sub", Toast.LENGTH_LONG).show();
            }

            getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }
}
