package net.mabako.steamgifts.data;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Poll implements Serializable {
    private static final long serialVersionUID = -2876811085489294457L;

    private String question;
    private List<IEndlessAdaptable> content = new ArrayList<>();

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<IEndlessAdaptable> getContent() {
        return content;
    }

    public void addOption(Option option) {
        content.add(option);
    }

    public class Option implements Serializable, IEndlessAdaptable {
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
    }
}
