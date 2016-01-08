package net.mabako.steamgifts.tasks;

import android.util.Log;

import net.mabako.steamgifts.fragments.GiveawayListFragment;

import org.jsoup.Connection;

public class UpdateGiveawayFilterTask extends AjaxTask<GiveawayListFragment> {
    public static final String HIDE = "hide_giveaways_by_game_id";

    /**
     * Show a game on the giveaway list again.
     * <p>Consistency ftw?</p>
     */
    public static final String UNHIDE = "remove_filter";

    private final int internalGameId;

    public UpdateGiveawayFilterTask(GiveawayListFragment fragment, String xsrfToken, String what, int internalGameId) {
        super(fragment, xsrfToken, what);

        setUrl("http://www.steamgifts.com/");
        this.internalGameId = internalGameId;
    }

    @Override
    protected void addExtraParameters(Connection connection) {
        connection.data("game_id", String.valueOf(internalGameId));
    }

    @Override
    protected void onPostExecute(Connection.Response response) {
        Log.d(UpdateGiveawayFilterTask.class.getSimpleName(), "Response code: " + response.statusCode());
        getFragment().onHideGame(internalGameId);
    }
}
