package net.mabako.steamgifts.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.io.IOException;

public abstract class AjaxTask<FragmentType> extends AsyncTask<Void, Void, Connection.Response> {
    private static final String TAG = AjaxTask.class.getSimpleName();

    private String url = "https://www.steamgifts.com/ajax.php";

    private final String xsrfToken;
    private final String what;

    private final Context context;
    private final FragmentType fragment;

    public AjaxTask(FragmentType fragment, Context context, String xsrfToken, String what) {
        this.fragment = fragment;
        this.context = context;
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
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT)
                    .data("xsrf_token", xsrfToken)
                    .data("do", what)
                    .cookie("PHPSESSID", SteamGiftsUserData.getCurrent(context).getSessionId())
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

    protected FragmentType getFragment() {
        return fragment;
    }

    String getWhat() {
        return what;
    }

    void setUrl(String url) {
        this.url = url;
    }

    public Context getContext() {
        return context;
    }
}
