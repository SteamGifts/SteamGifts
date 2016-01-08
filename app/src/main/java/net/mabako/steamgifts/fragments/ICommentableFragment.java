package net.mabako.steamgifts.fragments;

import net.mabako.steamgifts.data.Comment;

public interface ICommentableFragment {
    void showProfile(String user);

    void requestComment(Comment parentComment);
}
