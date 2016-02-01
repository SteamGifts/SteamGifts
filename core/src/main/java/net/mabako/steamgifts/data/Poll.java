package net.mabako.steamgifts.data;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

// TODO polls can be restricted to individual games, right now this only supports 'no game' vs 'each option has a game'
public class Poll implements Serializable {
    private static final long serialVersionUID = -2876811085489294457L;

    private String question;
    private List<IEndlessAdaptable> answers = new ArrayList<>();

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public void addAnswer(Option option) {
        answers.add(option);
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

    public static class Option implements Serializable, IEndlessAdaptable {
        private static final long serialVersionUID = 879317134785161587L;

        private int id;
        private int voteCount;
        private String text;

        private int appId = Game.NO_APP_ID;
        private Game.Type appType = Game.Type.APP;

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

        @Override
        public int getLayout() {
            return 0;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || !(o instanceof Option))
                return false;

            return ((Option) o).id == id;
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
}
