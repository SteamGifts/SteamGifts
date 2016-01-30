package net.mabako.steamgifts.tasks;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

import net.mabako.steamgifts.activities.SyncActivity;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Connection;

/**
 * Task to enter or leave giveaways.
 */
public class EnterLeaveGiveawayTask extends AjaxTask<IHasEnterableGiveaways> {
    private final static String TAG = EnterLeaveGiveawayTask.class.getSimpleName();
    private final String giveawayId;

    public EnterLeaveGiveawayTask(IHasEnterableGiveaways fragment, Context context, String giveawayId, String xsrfToken, String what) {
        super(fragment, context, xsrfToken, what);
        this.giveawayId = giveawayId;
    }

    @Override
    public void addExtraParameters(Connection connection) {
        connection.data("code", giveawayId);
    }

    @Override
    protected void onPostExecute(Connection.Response response) {
        if (response != null && response.statusCode() == 200) {
            try {
                Log.v(TAG, "Response to JSON request: " + response.body());
                JSONObject root = new JSONObject(response.body());

                boolean success = "success".equals(root.getString("type"));
                int points = root.getInt("points");

                IHasEnterableGiveaways fragment = getFragment();
                fragment.onEnterLeaveResult(giveawayId, getWhat(), success, true);

                // Update the points we have.
                SteamGiftsUserData.getCurrent(getContext()).setPoints(points);

                if (fragment instanceof Fragment && "error".equals(root.getString("type")) && "Sync Required".equals(root.getString("msg"))) {
                    ((Fragment) fragment).getActivity().startActivity(new Intent(((Fragment) fragment).getContext(), SyncActivity.class));
                }

                return;
            } catch (JSONException e) {
                Log.e(TAG, "Failed to parse JSON object", e);
            }
        }

        getFragment().onEnterLeaveResult(giveawayId, getWhat(), null, false);
    }
}
