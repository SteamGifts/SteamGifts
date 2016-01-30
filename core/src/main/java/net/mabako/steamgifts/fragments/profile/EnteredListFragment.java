package net.mabako.steamgifts.fragments.profile;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.fragments.interfaces.IHasEnterableGiveaways;
import net.mabako.steamgifts.fragments.util.GiveawayListFragmentStack;
import net.mabako.steamgifts.tasks.EnterLeaveGiveawayTask;
import net.mabako.steamgifts.tasks.LoadGameListTask;
import net.mabako.steamgifts.tasks.Utils;

import org.jsoup.nodes.Element;

import java.io.Serializable;
import java.util.List;

public class EnteredListFragment extends ListFragment<GiveawayAdapter> implements IHasEnterableGiveaways, IActivityTitle {
    private final static String TAG = EnteredListFragment.class.getSimpleName();
    private EnterLeaveGiveawayTask enterLeaveTask;

    @Override
    public int getTitleResource() {
        return R.string.user_tab_entered;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setFragmentValues(getActivity(), this, null);
    }

    @Override
    protected GiveawayAdapter createAdapter() {
        return new GiveawayAdapter(50, PreferenceManager.getDefaultSharedPreferences(getContext()));
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadGameListTask(this, "giveaways/entered", page, null) {
            @Override
            protected IEndlessAdaptable load(Element element) {
                Element firstColumn = element.select(".table__column--width-fill").first();
                Element link = firstColumn.select("a.table__column__heading").first();

                Uri linkUri = Uri.parse(link.attr("href"));
                String giveawayLink = linkUri.getPathSegments().get(1);
                String giveawayName = linkUri.getPathSegments().get(2);

                ProfileGiveaway giveaway = new ProfileGiveaway(giveawayLink);
                giveaway.setName(giveawayName);
                giveaway.setTitle(link.text());


                Element image = element.select(".global__image-inner-wrap").first();
                if (image != null) {
                    Uri uri = Uri.parse(Utils.extractAvatar(image.attr("style")));
                    List<String> pathSegments = uri.getPathSegments();
                    if (pathSegments.size() >= 3) {
                        giveaway.setGameId(Integer.parseInt(pathSegments.get(2)));
                        giveaway.setType("apps".equals(pathSegments.get(1)) ? Game.Type.APP : Game.Type.SUB);
                    }
                }

                giveaway.setPoints(-1);
                giveaway.setEntries(Integer.parseInt(element.select(".table__column--width-small").first().text().replace(",", "")));
                giveaway.setTimeRemaining(firstColumn.select("span").text());

                giveaway.setEntered(giveaway.isOpen());
                giveaway.setDeleted(!element.select(".table__column__deleted").isEmpty());

                return giveaway;
            }
        };
    }

    @Override
    protected Serializable getType() {
        return null;
    }

    @Override
    public void requestEnterLeave(String giveawayId, String what, String xsrfToken) {
        // Probably not...
        // if (enterLeaveTask != null)
        // enterLeaveTask.cancel(true);

        enterLeaveTask = new EnterLeaveGiveawayTask(this, giveawayId, xsrfToken, what);
        enterLeaveTask.execute();
    }

    @Override
    public void onEnterLeaveResult(String giveawayId, String what, Boolean success, boolean propagate) {
        if (success == Boolean.TRUE && GiveawayDetailFragment.ENTRY_DELETE.equals(what)) {
            adapter.removeGiveaway(giveawayId);
        } else {
            Log.e(TAG, "Probably an error catching the result...");
        }

        if (propagate)
            GiveawayListFragmentStack.onEnterLeaveResult(giveawayId, what, success);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (enterLeaveTask != null)
            enterLeaveTask.cancel(true);
    }
}
