package net.mabako.steamgifts.tasks;

import android.net.Uri;

import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.ICommentHolder;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

public final class Utils {
    /**
     * Extract comments recursively.
     *
     * @param commentNode Jsoup-Node of the parent node.
     * @param parent
     */
    public static void loadComments(Element commentNode, ICommentHolder parent) {
        loadComments(commentNode, parent, 0);
    }

    private static void loadComments(Element commentNode, ICommentHolder parent, int depth) {
        for (Element c : commentNode.children()) {
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

            Element timeRemaining = thisComment.select(".comment__actions > div span").first();

            // public Comment(int id, String author, String timeAgo, String timeAgoLong, String content) {
            Comment comment = new Comment(Integer.parseInt(c.attr("data-comment-id")), author, timeRemaining.text(), timeRemaining.attr("title"), content, depth, avatar, isOp);

            // check if the comment is deleted
            comment.setDeleted(thisComment.select(".comment__summary").first().select(".comment__delete-state").size() == 1);

            // add this
            parent.addComment(comment);

            // Add all children
            loadComments(c.select(".comment__children").first(), parent, depth + 1);
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
        String copiesT = hints.first().text();
        String pointsT = hints.last().text();
        int copies = hints.size() == 1 ? 1 : Integer.parseInt(copiesT.replace("(", "").replace(" Copies)", ""));
        int points = Integer.parseInt(pointsT.replace("(", "").replace("P)", ""));

        giveaway.setCopies(copies);
        giveaway.setPoints(points);

        // Steam link
        if (steamUri != null) {
            List<String> pathSegments = steamUri.getPathSegments();
            if (pathSegments.size() >= 2)
                giveaway.setGameId(Integer.parseInt(pathSegments.get(1)));
            giveaway.setType("app".equals(pathSegments.get(0)) ? Giveaway.Type.APP : Giveaway.Type.SUB);
        }

        // Time remaining
        giveaway.setTimeRemaining(element.select("." + cssNode + "__columns > div span").first().text());
        giveaway.setTimeCreated(element.select("." + cssNode + "__columns > div span").last().text());

        // Flags
        giveaway.setWhitelist(!element.select("." + cssNode + "__column--whitelist").isEmpty());
        giveaway.setGroup(!element.select("." + cssNode + "__column--group").isEmpty());

        Element level = element.select("." + cssNode + "__column--contributor-level").first();
        if (level != null)
            giveaway.setLevel(Integer.parseInt(level.text().replace("Level", "").replace("+", "").trim()));
    }
}
