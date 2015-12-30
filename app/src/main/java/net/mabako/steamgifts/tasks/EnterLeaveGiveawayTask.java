package net.mabako.steamgifts.tasks;

import android.util.Log;

import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.web.WebUserData;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;

/**
 * Task to enter or leave giveaways.
 */
public class EnterLeaveGiveawayTask extends AjaxTask<GiveawayDetailFragment> {
    private final static String TAG = EnterLeaveGiveawayTask.class.getSimpleName();
    private String giveawayId;

    public EnterLeaveGiveawayTask(GiveawayDetailFragment fragment, String giveawayId, String xsrfToken, String what) {
        super(fragment, giveawayId, xsrfToken, what);
        this.giveawayId = giveawayId;
    }

    @Override
    public void addExtraParameters(Connection connection) {
        connection.data("code", giveawayId);
    }

    @Override
    protected void onPostExecute(Connection.Response response) {
        if(response != null && response.statusCode() == 200) {
            try {
                JSONObject root = new JSONObject(response.body());

                boolean success = "success".equals(root.getString("type"));
                int points = root.getInt("points");

                getFragment().onEnterLeaveResult(getWhat(), success);

                // Update the points we have.
                WebUserData.getCurrent().setPoints(points);
                return;
            } catch(JSONException e) {
                Log.e(TAG, "Failed to parse JSON object", e);
            }
        }

        getFragment().onEnterLeaveResult(getWhat(), null);
    }
}
