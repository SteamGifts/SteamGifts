package net.mabako.steamgifts.data;

import android.support.annotation.NonNull;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO polls can be restricted to individual games, right now this only supports 'no game' vs 'each option has a game'
public class Poll implements Serializable {
    private static final long serialVersionUID = -2876811085489294457L;

    private Header header;
    private List<IEndlessAdaptable> answers = new ArrayList<>();

    private int totalVotes = 0;
    private int mostVotesOnASingleAnswer = 0;
    private int selectedAnswerId;

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
        mostVotesOnASingleAnswer = Math.max(mostVotesOnASingleAnswer, answer.getVoteCount());
    }

    public int getTotalVotes() {
        return totalVotes;
    }

    public int getMostVotesOnASingleAnswer() {
        return mostVotesOnASingleAnswer;
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

    public static class Answer implements Serializable, IEndlessAdaptable {
        private static final long serialVersionUID = 879317134785161587L;
        public static final int VIEW_LAYOUT = R.layout.poll_answer;

        private int id;
        private int voteCount;
        private String text;

        private int appId = Game.NO_APP_ID;
        private Game.Type appType = Game.Type.APP;
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

        public int getAppId() {
            return appId;
        }

        public void setAppId(int appId) {
            this.appId = appId;
        }

        public Game.Type getAppType() {
            return appType;
        }

        public void setAppType(Game.Type appType) {
            this.appType = appType;
        }

        public boolean isGame() {
            return appId != Game.NO_APP_ID;
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
            return "[" + id + "," + voteCount + "," + text + "," + appId + "]";
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
