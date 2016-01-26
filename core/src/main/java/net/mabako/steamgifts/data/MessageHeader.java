package net.mabako.steamgifts.data;

import android.content.Intent;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageHeader implements IEndlessAdaptable, ICommentHolder, Serializable {
    private static final long serialVersionUID = -4026179915503324775L;
    public static final int VIEW_LAYOUT = R.layout.message_header_item;

    private final String title;
    private List<Comment> comments = new ArrayList<>();

    public MessageHeader(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public List<Comment> getComments() {
        return comments;
    }

    @Override
    public void addComment(Comment comment) {
        comments.add(comment);
    }

    // TODO if you receive more messages while reading messages, this is gonna have a bad time
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof MessageHeader))
            return false;

        return comments.equals(((MessageHeader) o).comments);
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
