package net.mabako.steamgifts.tasks;

import android.support.v4.app.Fragment;
import android.util.Log;

import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.HiddenGamesFragment;

import org.jsoup.Connection;

public class UpdateGiveawayFilterTask<FragmentType extends Fragment> extends AjaxTask<FragmentType> {
    public static final String HIDE = "hide_giveaways_by_game_id";

    /**
     * Show a game on the giveaway list again.
     * <p>Consistency ftw?</p>
     */
    public static final String UNHIDE = "remove_filter";

    private final int internalGameId;

    public UpdateGiveawayFilterTask(FragmentType fragment, String xsrfToken, String what, int internalGameId) {
        super(fragment, xsrfToken, what);

        // We only use the normal ajax.php if we remove a game
        // ... like seriously?
        if (HIDE.equals("what"))
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

        FragmentType fragment = getFragment();
        if (fragment instanceof GiveawayListFragment) {
            ((GiveawayListFragment) fragment).onHideGame(internalGameId);
        } else if (fragment instanceof HiddenGamesFragment) {
            ((HiddenGamesFragment) fragment).onShowGame(internalGameId);
        }
    }
}
