package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

abstract class AjaxTask<FragmentType> extends AsyncTask<Void, Void, Connection.Response> {
    private static final String TAG = AjaxTask.class.getSimpleName();

    private String url = "http://www.steamgifts.com/ajax.php";

    private final String xsrfToken;
    private final String what;

    private final FragmentType fragment;

    AjaxTask(FragmentType fragment, String xsrfToken, String what) {
        this.fragment = fragment;
        this.xsrfToken = xsrfToken;
        this.what = what;

        if (TextUtils.isEmpty(this.xsrfToken))
            Log.w(TAG, "no xsrf token for ajax call");

        if (TextUtils.isEmpty(this.what))
            Log.w(TAG, "no what for ajax call");
    }

    @Override
    protected Connection.Response doInBackground(Void... params) {
        try {
            Log.v(TAG, "Connecting to " + url);
            Connection connection = Jsoup
                    .connect(url)
                    .data("xsrf_token", xsrfToken)
                    .data("do", what)
                    .cookie("PHPSESSID", SteamGiftsUserData.getCurrent().getSessionId())
                    .followRedirects(false);

            addExtraParameters(connection);

            Connection.Response response = connection.method(Connection.Method.POST).execute();

            Log.v(TAG, url + " returned Status Code " + response.statusCode() + " (" + response.statusMessage() + ")");

            return response;
        } catch (IOException e) {
            Log.e(TAG, "Error fetching URL", e);
            return null;
        }
    }

    protected abstract void addExtraParameters(Connection connection);

    FragmentType getFragment() {
        return fragment;
    }

    String getWhat() {
        return what;
    }

    void setUrl(String url) {
        this.url = url;
    }
}
