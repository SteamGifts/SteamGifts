package net.mabako.steamgifts.tasks;

import android.content.Context;
import android.net.Uri;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.fragments.interfaces.ILoadItemsListener;

import org.jsoup.nodes.Element;

import java.util.List;

public class LoadWonGameListTask extends LoadGameListTask {
    public LoadWonGameListTask(ILoadItemsListener listener, Context context, int page) {
        super(listener, context, "giveaways/won", page, null);
    }

    @Override
    protected IEndlessAdaptable load(Element element) {
        Element firstColumn = element.select(".table__column--width-fill").first();
        Element link = firstColumn.select("a.table__column__heading").first();

        Uri linkUri = Uri.parse(link.attr("href"));
        String giveawayLink = linkUri.getPathSegments().get(1);
        String giveawayName = linkUri.getPathSegments().get(2);

        Giveaway giveaway = new Giveaway(giveawayLink);
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
        giveaway.setEntries(-1);
        Element end = firstColumn.select("span").first();
        giveaway.setEndTime(Integer.valueOf(end.attr("data-timestamp")), end.parent().text().trim());

        // Has any feedback option been picked yet?
        // If so, this would be == 1, 0 hidden items implies both feedback options are currently available to be picked.
        giveaway.setEntered(element.select(".table__gift-feedback-awaiting-reply.is-hidden").size() == 0);

        return giveaway;
    }
}
