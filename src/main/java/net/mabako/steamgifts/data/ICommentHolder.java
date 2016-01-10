package net.mabako.steamgifts.data;

import java.util.List;

public interface ICommentHolder {
    /**
     * Returns a list of all child comments.
     *
     * @return List of child comments
     */
    List<Comment> getComments();

    /**
     * Adds a child comment.
     *
     * @param comment the child to add
     */
    void addComment(Comment comment);
}
