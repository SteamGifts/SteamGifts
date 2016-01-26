package net.mabako.steamgifts.data;

import java.io.Serializable;

public class BasicDiscussion implements Serializable {
    private static final long serialVersionUID = -7060144750419956364L;
    private final String discussionId;

    public BasicDiscussion(String discussionId) {
        this.discussionId = discussionId;
    }

    public String getDiscussionId() {
        return discussionId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof BasicDiscussion) || discussionId == null)
            return false;

        return discussionId.equals(((BasicDiscussion) o).discussionId);
    }

    @Override
    public int hashCode() {
        return discussionId == null ? 0 : discussionId.hashCode();
    }
}
