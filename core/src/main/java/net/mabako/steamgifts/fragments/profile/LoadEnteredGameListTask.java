package net.mabako.steamgifts.fragments.profile;

import android.net.Uri;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.ListFragment;
import net.mabako.steamgifts.tasks.LoadGameListTask;
import net.mabako.steamgifts.tasks.Utils;

import org.jsoup.nodes.Element;

import java.util.List;

public class LoadEnteredGameListTask extends LoadGameListTask {
    public static final int ENTRIES_PER_PAGE = 50;

    public LoadEnteredGameListTask(ListFragment listFragment, int page) {
        super(listFragment, listFragment.getContext(), "giveaways/entered", page, null);
    }

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
                giveaway.setGame(new Game("apps".equals(pathSegments.get(1)) ? Game.Type.APP : Game.Type.SUB, Integer.parseInt(pathSegments.get(2))));
            }
        }

        giveaway.setPoints(-1);
        giveaway.setEntries(Utils.parseInt(element.select(".table__column--width-small").first().text()));

        Element end = firstColumn.select("p > span").first();
        if (end != null)
            giveaway.setEndTime(end.attr("title"), end.text());

        giveaway.setEntered(giveaway.isOpen());
        giveaway.setDeleted(!element.select(".table__column__deleted").isEmpty());

        return giveaway;
    }
}
