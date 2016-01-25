package net.mabako.steam.store;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;
import org.jsoup.Connection;

abstract class LoadStoreTask extends AsyncTask<Void, Void, JSONObject> {
    @Override
    protected JSONObject doInBackground(Void... params) {
        try {
            Connection.Response response = getConnection().ignoreContentType(true).method(Connection.Method.GET).execute();
            if (response != null && response.statusCode() == 200)
                return new JSONObject(response.body());

            return null;
        } catch (Exception e) {
            Log.e(LoadStoreTask.class.getSimpleName(), "Error loading Url", e);
            return null;
        }
    }

    protected abstract Connection getConnection();
}
