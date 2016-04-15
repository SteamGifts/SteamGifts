package net.mabako.steamgifts.tasks;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import net.mabako.Constants;
import net.mabako.steamgifts.activities.MainActivity;

import org.jsoup.Jsoup;

import java.io.IOException;

public class LogoutTask extends AsyncTask<Void, Void, Boolean> {
    private static final String TAG = LogoutTask.class.getSimpleName();

    private final Activity activity;
    private ProgressDialog progressDialog;
    private final String sessionId;

    public LogoutTask(MainActivity activity, String sessionId) {
        this.activity = activity;
        this.sessionId = sessionId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("Logging out...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        // Mostly irrelevant since we clear the stored session id...
        try {
            Jsoup.connect("https://www.steamgifts.com/?logout")
                    .userAgent(Constants.JSOUP_USER_AGENT)
                    .timeout(Constants.JSOUP_TIMEOUT)
                    .cookie("PHPSESSID", sessionId)
                    .get();

            Log.i(TAG, "Successfully logged out");
            return true;
        }
        catch(IOException e) {
            Log.e(TAG, "Failed to log out", e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        progressDialog.dismiss();
    }
}
