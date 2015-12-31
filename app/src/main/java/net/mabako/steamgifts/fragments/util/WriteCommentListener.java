package net.mabako.steamgifts.fragments.util;

public interface WriteCommentListener {
    void submit(String giveawayId, String xsrfToken, String message);
}
