package net.mabako.steamgifts.fragments.interfaces;

import net.mabako.steamgifts.data.Comment;

public interface ICommentableFragment {
    void showProfile(String user);

    void showProfile(long steamID64);

    void requestComment(Comment parentComment);

    void deleteComment(Comment comment);

    boolean canPostOrModifyComments();
}
