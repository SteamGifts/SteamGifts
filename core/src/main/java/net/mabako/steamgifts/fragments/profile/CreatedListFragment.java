package net.mabako.steamgifts.fragments.profile;

import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.GiveawayAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.tasks.LoadGameListTask;
import net.mabako.steamgifts.tasks.Utils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.Serializable;
import java.util.List;

public class CreatedListFragment extends ListFragment<GiveawayAdapter> implements IActivityTitle {
    public CreatedListFragment() {
        loadItemsInitially = false;
    }

    @Override
    public int getTitleResource() {
        return R.string.user_tab_created;
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    @Override
    protected GiveawayAdapter createAdapter() {
        return new GiveawayAdapter(getActivity(), new EndlessAdapter.OnLoadListener() {
            @Override
            public void onLoad(int page) {

            }
        }, null, 50, PreferenceManager.getDefaultSharedPreferences(getContext()));
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadGameListTask(this, "giveaways/created", page, null) {
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


                Elements columns = element.select(".table__column--width-small.text-center");

                giveaway.setPoints(-1);
                giveaway.setEntries(Integer.parseInt(columns.first().text().replace(",", "")));
                giveaway.setTimeRemaining(firstColumn.select("span > span").text());

                giveaway.setEntered("Unsent".equals(columns.get(1).text()));
                giveaway.setDeleted(!element.select(".table__column__deleted").isEmpty());

                return giveaway;
            }
        };
    }

    @Override
    protected Serializable getType() {
        return null;
    }
}
