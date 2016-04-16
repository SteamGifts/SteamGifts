package net.mabako.steamgifts.tasks;

import android.support.v4.app.Fragment;

import net.mabako.steamgifts.fragments.GiveawayListFragment;
import net.mabako.steamgifts.fragments.HiddenGamesFragment;
import net.mabako.steamgifts.fragments.interfaces.IHasHideableGiveaways;

import org.jsoup.Connection;

public class UpdateGiveawayFilterTask<FragmentType extends Fragment> extends AjaxTask<FragmentType> {
    public static final String HIDE = "hide_giveaways_by_game_id";

    /**
     * Show a game on the giveaway list again.
     * <p>Consistency ftw?</p>
     */
    public static final String UNHIDE = "remove_filter";

    private final int internalGameId;
    private final String gameTitle;

    public UpdateGiveawayFilterTask(FragmentType fragment, String xsrfToken, String what, int internalGameId, String gameTitle) {
        super(fragment, fragment.getContext(), xsrfToken, what);

        this.internalGameId = internalGameId;
        this.gameTitle = gameTitle;
    }

    @Override
    protected void addExtraParameters(Connection connection) {
        connection.data("game_id", String.valueOf(internalGameId));
    }

    @Override
    protected void onPostExecute(Connection.Response response) {
        if (response == null) {
            // TODO Socket timed out or some stupid shit like that.
            return;
        }

        FragmentType fragment = getFragment();
        if (fragment instanceof IHasHideableGiveaways && HIDE.equals(getWhat())) {
            if (response.statusCode() == 200) {
                ((IHasHideableGiveaways) fragment).onHideGame(internalGameId, true, gameTitle);
                return;
            }
        }

        if (fragment instanceof GiveawayListFragment && UNHIDE.equals(getWhat())) {
            if (response.statusCode() == 200) {
                ((GiveawayListFragment) fragment).onShowGame(internalGameId, true);
            }
        }

        if (fragment instanceof HiddenGamesFragment && UNHIDE.equals(getWhat())) {
            if (response.statusCode() == 200)
                ((HiddenGamesFragment) fragment).onShowGame(internalGameId);
        }
    }
}
