package net.mabako.steamgifts.fragments;

import net.mabako.steamgifts.data.Comment;

public interface ICommentableFragment {
    void requestComment(Comment parentComment);
}
