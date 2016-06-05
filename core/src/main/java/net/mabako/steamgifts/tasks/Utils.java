package net.mabako.steamgifts.tasks;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.data.Giveaway;
import net.mabako.steamgifts.data.ICommentHolder;
import net.mabako.steamgifts.data.IImageHolder;
import net.mabako.steamgifts.data.Image;
import net.mabako.steamgifts.data.TradeComment;
import net.mabako.steamgifts.data.User;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    private Utils() {
    }

    /**
     * Extract comments recursively.
     *
     * @param commentNode Jsoup-Node of the parent node.
     * @param parent
     */
    public static void loadComments(Element commentNode, ICommentHolder parent, Comment.Type type) {
        loadComments(commentNode, parent, 0, false, false, type);
    }

    public static void loadComments(Element commentNode, ICommentHolder parent, int depth, boolean reversed, boolean includeTradeScore, Comment.Type type) {
        if (commentNode == null)
            return;

        Elements children = commentNode.children();

        if (reversed)
            Collections.reverse(children);

        for (Element c : children) {
            long commentId = 0;
            try {
                commentId = Integer.parseInt(c.attr("data-comment-id"));
            } catch (NumberFormatException e) {
                /* do nothing */
            }

            Comment comment = loadComment(c.child(0), commentId, depth, includeTradeScore, type);

            // add this
            parent.addComment(comment);

            // Add all children
            loadComments(c.select(".comment__children").first(), parent, depth + 1, false, includeTradeScore, type);
        }
    }

    /**
     * Load a single comment
     *
     * @param element           comment HTML element
     * @param commentId         the id of  the comment to be loaded
     * @param depth             the depth at which to display said comment
     * @param includeTradeScore whether or not to include +/- elements of the trading score, only visible in the trades section
     * @return the new comment
     */
    @NonNull
    public static Comment loadComment(Element element, long commentId, int depth, boolean includeTradeScore, Comment.Type type) {
        // Save the content of the edit state for a bit & remove the edit state from being rendered.
        Element editState = element.select(".comment__edit-state.is-hidden textarea[name=description]").first();
        String editableContent = null;
        if (editState != null)
            editableContent = editState.text();
        element.select(".comment__edit-state").html("");

        Element authorNode = element.select(".comment__username").first();
        String author = authorNode.text();
        boolean isOp = authorNode.hasClass("comment__username--op");

        String avatar = null;
        Element avatarNode = element.select(".global__image-inner-wrap").first();
        if (avatarNode != null)
            avatar = extractAvatar(avatarNode.attr("style"));

        Element timeCreated = element.select(".comment__actions > div span").first();

        Uri permalinkUri = Uri.parse(element.select(".comment__actions a[href^=/go/" + type.getPath() + "]").first().attr("href"));

        Comment comment = includeTradeScore ? new TradeComment(commentId, author, depth, avatar, isOp, type) : new Comment(commentId, author, depth, avatar, isOp, type);
        comment.setPermalinkId(permalinkUri.getPathSegments().get(2));
        comment.setEditableContent(editableContent);
        comment.setCreatedTime(timeCreated.attr("title"));


        Element desc = element.select(".comment__description").first();
        desc.select("blockquote").tagName("custom_quote");
        String content = loadAttachedImages(comment, desc);
        comment.setContent(content);

        // check if the comment is deleted
        comment.setDeleted(element.select(".comment__summary").first().select(".comment__delete-state").size() == 1);

        comment.setHighlighted(element.select(".comment__parent > .comment__envelope").size() != 0);

        Element roleName = element.select(".comment__role-name").first();
        if (roleName != null)
            comment.setAuthorRole(roleName.text().replace("(", "").replace(")", ""));

        // Do we have either a delete or undelete link?
        comment.setDeletable(element.select(".comment__actions__button.js__comment-delete").size() + element.select(".comment__actions__button.js__comment-undelete").size() == 1);

        if (comment instanceof TradeComment && !comment.isDeleted()) {
            try {
                ((TradeComment) comment).setTradeScorePositive(Utils.parseInt(element.select(".trade-feedback--positive").first().text()));
                ((TradeComment) comment).setTradeScoreNegative(-Utils.parseInt(element.select(".trade-feedback--negative").first().text()));
            } catch (Exception e) {
                Log.v(TAG, "Unable to parse feedback", e);
            }
        }

        return comment;
    }

    public static String extractAvatar(String style) {
        return style.replace("background-image:url(", "").replace(");", "").replace("_medium", "_full");
    }

    /**
     * Load some details for the giveaway. Some items must be loaded outside of this.
     */
    public static void loadGiveaway(Giveaway giveaway, Element element, String cssNode, String headerHintCssNode, Uri steamUri) {
        // Copies & Points. They do not have separate markup classes, it's basically "if one thin markup element exists, it's one copy only"
        Elements hints = element.select("." + headerHintCssNode);
        if (!hints.isEmpty()) {
            String copiesT = hints.first().text();
            String pointsT = hints.last().text();
            int copies = hints.size() == 1 ? 1 : parseInt(copiesT.replace("(", "").replace(" Copies)", ""));
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
            if (pathSegments.size() >= 2) {
                giveaway.setGame(new Game("app".equals(pathSegments.get(0)) ? Game.Type.APP : Game.Type.SUB, Integer.parseInt(pathSegments.get(1))));
            }
        }

        // Time remaining
        Element end = element.select("." + cssNode + "__columns > div span").first();
        giveaway.setEndTime(end.attr("title"), end.text());
        giveaway.setCreatedTime(element.select("." + cssNode + "__columns > div span").last().attr("title"));

        // Flags
        giveaway.setWhitelist(!element.select("." + cssNode + "__column--whitelist").isEmpty());
        giveaway.setGroup(!element.select("." + cssNode + "__column--group").isEmpty());
        giveaway.setPrivate(!element.select("." + cssNode + "__column--invite-only").isEmpty());
        giveaway.setRegionRestricted(!element.select("." + cssNode + "__column--region-restricted").isEmpty());

        Element level = element.select("." + cssNode + "__column--contributor-level").first();
        if (level != null)
            giveaway.setLevel(Integer.parseInt(level.text().replace("Level", "").replace("+", "").trim()));

        // Internal ID for blacklisting
        try {
            giveaway.setInternalGameId(Integer.parseInt(element.parent().attr("data-game-id")));
        } catch (NumberFormatException e) {
            // no game ID for us to allow hiding the game.
        }
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
            giveaway.setEntries(parseInt(links.first().text().split(" ")[0]));

            giveaway.setEntered(element.hasClass("is-faded"));

            // More details
            Element icon = element.select("h2 a").last();
            Uri uriIcon = icon == link ? null : Uri.parse(icon.attr("href"));

            Utils.loadGiveaway(giveaway, element, "giveaway", "giveaway__heading__thin", uriIcon);
            giveawayList.add(giveaway);
        }

        return giveawayList;
    }

    /**
     * The document title is in the format "Game Title - Page X" if we're on /giveaways/id/name/search?page=X,
     * so we strip out the page number.
     */
    public static String getPageTitle(Document document) {
        String title = document.title();
        return title.replaceAll(" - Page ([\\d,]+)$", "");
    }

    /**
     * Extracts all images from the description.
     *
     * @param imageHolder item to save this into
     * @param description description of the element
     * @return the description, minus attached images
     */
    public static String loadAttachedImages(IImageHolder imageHolder, Element description) {
        // find all "View attached image" segments
        Elements images = description.select("div > a > img.is-hidden");
        for (Element image : images) {
            // Extract the link.
            String src = image.attr("src");
            if (!TextUtils.isEmpty(src))
                imageHolder.attachImage(new Image(src, image.attr("title")));

            // Remove this image.
            image.parent().parent().html("");
        }

        return description.html();
    }

    /**
     * Loads a user's profile, returns the XSRF token of the page.
     *
     * @param user     existing profile container
     * @param document HTML input document
     * @return XSRF-token
     */
    public static String loadUserProfile(User user, Document document) {
        String foundXsrfToken = null;

        // If this isn't the user we're logged in as, we'd get some user id.
        Element idElement = document.select("input[name=child_user_id]").first();
        if (idElement != null) {
            user.setId(Integer.valueOf(idElement.attr("value")));
        } else {
            Log.v(TAG, "No child_user_id");
        }

        user.setWhitelisted(!document.select(".sidebar__shortcut__whitelist.is-selected").isEmpty());
        user.setBlacklisted(!document.select(".sidebar__shortcut__blacklist.is-selected").isEmpty());

        // Fetch the xsrf token - this, again, is only present if we're on another user's page.
        Element xsrfToken = document.select("input[name=xsrf_token]").first();
        if (xsrfToken != null)
            foundXsrfToken = xsrfToken.attr("value");

        user.setName(document.select(".featured__heading__medium").first().text());
        user.setAvatar(Utils.extractAvatar(document.select(".global__image-inner-wrap").first().attr("style")));
        user.setUrl(document.select(".sidebar a[data-tooltip=\"Visit Steam Profile\"]").first().attr("href"));

        Elements columns = document.select(".featured__table__column");
        user.setRole(columns.first().select("a[href^=/roles/").text());
        user.setComments(parseInt(columns.first().select(".featured__table__row__right").get(3).text()));

        Elements right = columns.last().select(".featured__table__row__right");

        // Both won and created have <a href="...">[amount won]</a> [value of won items],
        // so it's impossible to get the text for the amount directly.
        Element won = right.get(1);
        user.setWon(parseInt(won.select("a").first().text()));
        won.select("a").html("");
        user.setWonAmount(won.text().trim());

        Element created = right.get(2);
        user.setCreated(parseInt(created.select("a").first().text()));
        created.select("a").html("");
        user.setCreatedAmount(created.text().trim());

        // Trade feedback
        Element feedback = right.get(0);
        if (feedback != null) {
            String text = feedback.text();
            if (text.contains(" / ")) {
                try {
                    String[] str = text.split(" / ");
                    int positive = Integer.valueOf(str[0]);
                    int negative = Integer.valueOf(str[1]) * -1;

                    user.setPositiveFeedback(positive);
                    user.setNegativeFeedback(negative);

                    Log.d(TAG, "F:" + user.getPositiveFeedback() + ", " + user.getNegativeFeedback());
                } catch (Exception e) {
                    Log.w(TAG, "Error parsing user feedback score", e);
                }
            }
        }

        user.setLevel((int) Float.parseFloat(right.get(3).select("span").first().attr("title")));
        return foundXsrfToken;
    }

    /**
     * Returns an integer value from HTML for most display purposes.
     *
     * @param str text containing the value, for example "123" or "13,400"
     * @return the parsed integer value
     */
    public static int parseInt(String str) {
        return Integer.parseInt(str.replace(",", ""));
    }
}
