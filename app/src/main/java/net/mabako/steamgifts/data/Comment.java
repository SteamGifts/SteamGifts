package net.mabako.steamgifts.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Comment on a giveaway or discussion.
 */
public class Comment implements Serializable {
    private int id;
    private String author;
    private String timeAgo;
    private String timeAgoLong;
    private String content;
    private int depth;

    public Comment(int id, String author, String timeAgo, String timeAgoLong, String content, int depth) {
        this.id = id;
        this.author = author;
        this.timeAgo = timeAgo;
        this.timeAgoLong = timeAgoLong;
        this.content = content;
        this.depth = depth;
    }

    public String getAuthor() {
        return author;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public String getTimeAgoLong() {
        return timeAgoLong;
    }

    public String getContent() {
        return content;
    }

    public int getDepth() {
        return depth;
    }
}
