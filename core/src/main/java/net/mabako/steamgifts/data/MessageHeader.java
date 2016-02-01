package net.mabako.steamgifts.data;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MessageHeader implements IEndlessAdaptable, ICommentHolder, Serializable {
    private static final long serialVersionUID = -4026179915503324775L;
    public static final int VIEW_LAYOUT = R.layout.message_header_item;

    private final String title;
    private final String url;
    private List<Comment> comments = new ArrayList<>();

    public MessageHeader(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public List<Comment> getComments() {
        return comments;
    }

    @Override
    public void addComment(Comment comment) {
        comments.add(comment);
    }

    // TODO if you receive more messages while reading messages and refresh, this is gonna have a bad time
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
