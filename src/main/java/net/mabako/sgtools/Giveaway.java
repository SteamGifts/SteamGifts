package net.mabako.sgtools;

import net.mabako.steamgifts.data.Game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

class Giveaway implements Serializable {
    private String name;
    private Game.Type type = Game.Type.APP;
    private int gameId = Game.NO_APP_ID;

    private List<String> rules = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Game.Type getType() {
        return type;
    }

    public void setType(Game.Type type) {
        this.type = type;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public void addRule(String rule) {
        rules.add(rule);
    }

    public List<String> getRules() {
        return rules;
    }
}
