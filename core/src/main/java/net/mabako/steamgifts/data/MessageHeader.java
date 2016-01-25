package net.mabako.steamgifts.data;

import android.content.Intent;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.util.ArrayList;
import java.util.List;

public class MessageHeader implements IEndlessAdaptable, ICommentHolder {
    public static final int VIEW_LAYOUT = R.layout.message_header_item;

    private final String title;
    private final Intent intent;
    private List<Comment> comments = new ArrayList<>();

    public MessageHeader(String title, Intent intent) {
        this.title = title;
        this.intent = intent;
    }

    public String getTitle() {
        return title;
    }

    public Intent getIntent() {
        return intent;
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
