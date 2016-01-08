package net.mabako.steamgifts.data;

import java.io.Serializable;

public class BasicDiscussion implements Serializable {
    private final String discussionId;

    public BasicDiscussion(String discussionId) {
        this.discussionId = discussionId;
    }

    public String getDiscussionId() {
        return discussionId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof BasicDiscussion))
            return false;

        return ((BasicDiscussion) o).discussionId == discussionId;
    }

    @Override
    public int hashCode() {
        return discussionId.hashCode();
    }
}
