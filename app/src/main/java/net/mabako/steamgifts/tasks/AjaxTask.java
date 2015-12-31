package net.mabako.steamgifts.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.mabako.steamgifts.web.WebUserData;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public abstract class AjaxTask<FragmentType> extends AsyncTask<Void, Void, Connection.Response> {
    private static final String TAG = AjaxTask.class.getSimpleName();

    private String url = "http://www.steamgifts.com/ajax.php";

    private String xsrfToken;
    private String what;

    private FragmentType fragment;

    public AjaxTask(FragmentType fragment, String xsrfToken, String what) {
        this.fragment = fragment;
        this.xsrfToken = xsrfToken;
        this.what = what;
    }

    @Override
    protected Connection.Response doInBackground(Void... params) {
        try {
            Connection connection = Jsoup
                    .connect(url)
                    .data("xsrf_token", xsrfToken)
                    .data("do", what)
                    .cookie("PHPSESSID", WebUserData.getCurrent().getSessionId())
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

    public abstract void addExtraParameters(Connection connection);

    public FragmentType getFragment() {
        return fragment;
    }

    public String getWhat() {
        return what;
    }

    protected void setUrl(String url) {
        this.url = url;
    }
}
