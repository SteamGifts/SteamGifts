package net.mabako.steamgifts.data;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;

public class Game implements Serializable, IEndlessAdaptable {
    private static final long serialVersionUID = -4047245968975766647L;
    public static final int NO_APP_ID = 0;
    public static final int VIEW_LAYOUT = R.layout.hidden_game_item;

    private String name;
    private Type type = Type.APP;
    private int gameId = NO_APP_ID;

    /**
     * Id used (exclusively?) for filtering games.
     */
    private int internalGameId;

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public int getInternalGameId() {
        return internalGameId;
    }

    public void setInternalGameId(int internalGameId) {
        this.internalGameId = internalGameId;
    }

    @Override
    public int hashCode() {
        return internalGameId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Game))
            return false;

        return ((Game) o).internalGameId == internalGameId;
    }

    public enum Type {
        APP, SUB
    }
}
