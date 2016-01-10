package net.mabako.store;

import android.util.Log;
import android.view.View;
import android.widget.Toast;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.data.Game;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class StoreSubFragment extends StoreFragment {
    public static StoreSubFragment newInstance(int appId) {
        StoreSubFragment fragment = new StoreSubFragment();
        fragment.appId = appId;
        return fragment;
    }

    @Override
    public LoadStoreTask getTaskToStart() {
        return new LoadSubTask();
    }

    private class LoadSubTask extends LoadStoreTask {
        @Override
        protected Connection getConnection() {
            return Jsoup
                    .connect("http://store.steampowered.com/api/packagedetails/")
                    .data("packageids", String.valueOf(appId))
                    .data("l", "en");
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            if (jsonObject != null) {
                try {
                    JSONObject sub = jsonObject.getJSONObject(String.valueOf(appId));

                    // Were we successful in fetching the details?
                    if (sub.getBoolean("success")) {
                        JSONObject data = sub.getJSONObject("data");
                        JSONArray apps = data.getJSONArray("apps");

                        for (int i = 0; i < apps.length(); ++i) {
                            JSONObject app = apps.getJSONObject(i);

                            Game game = new Game();
                            game.setType(Game.Type.APP);
                            game.setGameId(app.getInt("id"));
                            game.setName(app.getString("name"));

                            adapter.add(game);
                        }

                        Log.d(StoreSubFragment.class.getSimpleName(), "--> " + adapter.getItemCount());
                    } else throw new Exception("not successful");
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Unable to load Store App", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), "Unable to load Store Sub", Toast.LENGTH_LONG).show();
            }

            getView().findViewById(R.id.progressBar).setVisibility(View.GONE);
        }
    }
}
