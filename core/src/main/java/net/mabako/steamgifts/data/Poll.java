package net.mabako.steamgifts.data;

import android.support.annotation.NonNull;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO polls can show individual games, right now this is only shown as text.
public class Poll implements Serializable {
    private static final long serialVersionUID = -2876811085489294457L;

    private Header header;
    private List<IEndlessAdaptable> answers = new ArrayList<>();

    private int totalVotes = 0;
    private int selectedAnswerId;
    private boolean closed;

    public Header getHeader() {
        return header;
    }

    public void setQuestion(String question) {
        header = new Header();
        header.setText(question);
    }

    public List<IEndlessAdaptable> getAnswers() {
        return answers;
    }

    public void addAnswer(@NonNull Answer answer, boolean selected) {
        answers.add(answer);

        if (selected)
            selectedAnswerId = answer.getId();

        answer.setPoll(this);
        totalVotes += answer.getVoteCount();
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Poll[");

        for (int i = 0; i < answers.size(); ++i) {
            if (i > 0)
                sb.append(",");
            sb.append(answers.get(i).toString());
        }

        sb.append("]");
        return sb.toString();
    }

    public int getSelectedAnswerId() {
        return selectedAnswerId;
    }

    public void setSelectedAnswerId(int selectedAnswerId) {
        this.selectedAnswerId = selectedAnswerId;
    }

    public static class Answer implements Serializable, IEndlessAdaptable {
        private static final long serialVersionUID = 879317134785161587L;
        public static final int VIEW_LAYOUT = R.layout.poll_answer;

        private int id;
        private int voteCount;
        private String text;

        private Poll poll;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getVoteCount() {
            return voteCount;
        }

        public void setVoteCount(int voteCount) {
            this.voteCount = voteCount;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Poll getPoll() {
            return poll;
        }

        public void setPoll(Poll poll) {
            this.poll = poll;
        }

        public boolean isSelected() {
            return poll.selectedAnswerId == id;
        }

        public boolean isVoteable() {
            return !poll.closed;
        }

        @Override
        public int getLayout() {
            return VIEW_LAYOUT;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Answer))
                return false;

            return ((Answer) o).id == id;
        }

        @Override
        public int hashCode() {
            return id;
        }

        @Override
        public String toString() {
            return "[" + id + "," + voteCount + "," + text + "]";
        }
    }

    public static class CommentSeparator implements IEndlessAdaptable, Serializable {
        public static final int VIEW_LAYOUT = R.layout.comment_separator;
        private static final long serialVersionUID = -8237738700191365276L;

        @Override
        public int getLayout() {
            return VIEW_LAYOUT;
        }
    }

    public static class Header implements IEndlessAdaptable, Serializable {
        public static final int VIEW_LAYOUT = R.layout.poll_header;
        private static final long serialVersionUID = 6397402142913281497L;

        private String text;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public int getLayout() {
            return VIEW_LAYOUT;
        }
    }
}
