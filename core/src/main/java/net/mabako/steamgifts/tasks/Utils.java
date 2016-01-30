package net.mabako.steamgifts.tasks;

import android.net.Uri;

import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.ICommentHolder;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Utils {
    /**
     * Extract comments recursively.
     *
     * @param commentNode Jsoup-Node of the parent node.
     * @param parent
     */
    public static void loadComments(Element commentNode, ICommentHolder parent) {
        loadComments(commentNode, parent, 0, false);
    }

    public static void loadComments(Element commentNode, ICommentHolder parent, int depth, boolean reversed) {
        if (commentNode == null)
            return;

        Elements children = commentNode.children();

        if (reversed)
            Collections.reverse(children);

        for (Element c : children) {
            Element thisComment = c.child(0);

            // Remove "Save Changes" & "Cancel"
            thisComment.select(".comment__edit-state").html("");

            Element authorNode = thisComment.select(".comment__username").first();
            String author = authorNode.text();
            boolean isOp = authorNode.hasClass("comment__username--op");

            String avatar = null;
            Element avatarNode = thisComment.select(".global__image-inner-wrap").first();
            if (avatarNode != null)
                avatar = extractAvatar(avatarNode.attr("style"));

            Element desc = thisComment.select(".comment__description").first();
            desc.select(".comment__toggle-attached").html("");
            String content = desc.html();

            Element timeCreated = thisComment.select(".comment__actions > div span").first();

            Uri permalinkUri = Uri.parse(thisComment.select(".comment__actions a[href^=/go/comment").first().attr("href"));

            int commentId = 0;
            try {
                commentId = Integer.parseInt(c.attr("data-comment-id"));
            } catch (NumberFormatException e) {
            }

            Comment comment = new Comment(commentId, author, timeCreated.text(), timeCreated.attr("title"), content, depth, avatar, isOp);
            comment.setPermalinkId(permalinkUri.getPathSegments().get(2));

            // check if the comment is deleted
            comment.setDeleted(thisComment.select(".comment__summary").first().select(".comment__delete-state").size() == 1);

            comment.setHighlighted(thisComment.select(".comment__parent > .comment__envelope").size() != 0);

            // add this
            parent.addComment(comment);

            // Add all children
            loadComments(c.select(".comment__children").first(), parent, depth + 1, false);
        }
    }

    public static String extractAvatar(String style) {
        return style.replace("background-image:url(", "").replace(");", "").replace("_medium", "_full");
    }

    /**
     * Load some details for the giveaway. Some items must be loaded outside of this.
     *
     * @param giveaway
     * @param element
     * @param cssNode
     * @param headerHintCssNode
     * @param steamUri
     */
    public static void loadGiveaway(Giveaway giveaway, Element element, String cssNode, String headerHintCssNode, Uri steamUri) {
        // Copies & Points. They do not have separate markup classes, it's basically "if one thin markup element exists, it's one copy only"
        Elements hints = element.select("." + headerHintCssNode);
        if (!hints.isEmpty()) {
            String copiesT = hints.first().text();
            String pointsT = hints.last().text();
            int copies = hints.size() == 1 ? 1 : Integer.parseInt(copiesT.replace("(", "").replace(" Copies)", "").replace(",", ""));
            int points = Integer.parseInt(pointsT.replace("(", "").replace("P)", ""));

            giveaway.setCopies(copies);
            giveaway.setPoints(points);
        } else {
            giveaway.setCopies(1);
            giveaway.setPoints(0);
        }

        // Steam link
        if (steamUri != null) {
            List<String> pathSegments = steamUri.getPathSegments();
            if (pathSegments.size() >= 2)
                giveaway.setGameId(Integer.parseInt(pathSegments.get(1)));
            giveaway.setType("app".equals(pathSegments.get(0)) ? Game.Type.APP : Game.Type.SUB);
        }

        // Time remaining
        Element endTime = element.select("." + cssNode + "__columns > div span").first();
        giveaway.setTimeRemaining(endTime.text());
        giveaway.setEndTime(endTime.attr("title"));
        giveaway.setTimeCreated(element.select("." + cssNode + "__columns > div span").last().text());

        // Flags
        giveaway.setWhitelist(!element.select("." + cssNode + "__column--whitelist").isEmpty());
        giveaway.setGroup(!element.select("." + cssNode + "__column--group").isEmpty());
        giveaway.setPrivate(!element.select("." + cssNode + "__column--invite-only").isEmpty());
        giveaway.setRegionRestricted(!element.select("." + cssNode + "__column--region-restricted").isEmpty());

        Element level = element.select("." + cssNode + "__column--contributor-level").first();
        if (level != null)
            giveaway.setLevel(Integer.parseInt(level.text().replace("Level", "").replace("+", "").trim()));

        // Internal ID for blacklisting
        Element popup = element.select(".giveaway__hide.trigger-popup").first();
        if (popup != null)
            giveaway.setInternalGameId(Integer.parseInt(popup.attr("data-game-id")));
    }

    /**
     * Loads giveaways from a list page.
     * <p>This is not suitable for loading individual giveaway instances from the featured list, as the HTML layout differs (see {@link LoadGiveawayDetailsTask#loadGiveaway(Document, Uri)}</p>
     *
     * @param document the loaded document
     * @return list of giveaways
     */
    public static List<Giveaway> loadGiveawaysFromList(Document document) {
        Elements giveaways = document.select(".giveaway__row-inner-wrap");

        List<Giveaway> giveawayList = new ArrayList<>();
        for (Element element : giveaways) {
            // Basic information
            Element link = element.select("h2 a").first();

            Giveaway giveaway = null;
            if (link.hasAttr("href")) {
                Uri linkUri = Uri.parse(link.attr("href"));
                String giveawayLink = linkUri.getPathSegments().get(1);
                String giveawayName = linkUri.getPathSegments().get(2);

                giveaway = new Giveaway(giveawayLink);
                giveaway.setName(giveawayName);
            } else {
                giveaway = new Giveaway(null);
                giveaway.setName(null);
            }

            giveaway.setTitle(link.text());
            giveaway.setCreator(element.select(".giveaway__username").text());

            // Entries, would usually have comment count too... but we don't display that anywhere.
            Elements links = element.select(".giveaway__links a span");
            giveaway.setEntries(Integer.parseInt(links.first().text().split(" ")[0].replace(",", "")));

            giveaway.setEntered(element.hasClass("is-faded"));

            // More details
            Element icon = element.select("h2 a").last();
            Uri uriIcon = icon == link ? null : Uri.parse(icon.attr("href"));

            Utils.loadGiveaway(giveaway, element, "giveaway", "giveaway__heading__thin", uriIcon);
            giveawayList.add(giveaway);
        }

        return giveawayList;
    }
}
