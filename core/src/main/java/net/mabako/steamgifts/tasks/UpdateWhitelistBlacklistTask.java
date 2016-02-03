package net.mabako.steamgifts.tasks;

import android.content.Context;
import android.widget.Toast;

import net.mabako.steamgifts.data.BasicUser;
import net.mabako.steamgifts.fragments.interfaces.IHasWhitelistAndBlacklist;

import org.jsoup.Connection;

import java.util.Locale;

/**
 * Add or remove a user from a white- or blacklist.
 */
public class UpdateWhitelistBlacklistTask extends AjaxTask<IHasWhitelistAndBlacklist> {
    // too lazy to parse JSON
    private static final String STATUS_SUCCESS = "{\"type\":\"success\"}";

    private final boolean adding;
    private final BasicUser user;

    public UpdateWhitelistBlacklistTask(IHasWhitelistAndBlacklist fragment, Context context, String xsrfToken, IHasWhitelistAndBlacklist.What what, BasicUser user, boolean adding) {
        super(fragment, context, xsrfToken, what.name().toLowerCase(Locale.ENGLISH));
        this.adding = adding;
        this.user = user;
    }

    @Override
    protected void addExtraParameters(Connection connection) {
        connection.data("action", adding ? "insert" : "delete");
        connection.data("child_user_id", String.valueOf(user.getId()));
    }

    @Override
    protected void onPostExecute(Connection.Response response) {
        if (response != null && response.statusCode() == 200 && STATUS_SUCCESS.equals(response.body())) {
            getFragment().onUserWhitelistOrBlacklistUpdated(user, IHasWhitelistAndBlacklist.What.valueOf(getWhat().toUpperCase(Locale.ENGLISH)), adding);
        } else {
            Toast.makeText(getContext(), "Unable to update " + getWhat() + ".", Toast.LENGTH_SHORT).show();
        }
    }
}
