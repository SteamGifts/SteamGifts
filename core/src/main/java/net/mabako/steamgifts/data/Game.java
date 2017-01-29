package net.mabako.steamgifts.data;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serializable;

public class Game implements Serializable, IEndlessAdaptable {
    private static final long serialVersionUID = -4047245968975766647L;
    public static final int NO_APP_ID = 0;
    public static final int VIEW_LAYOUT = R.layout.game_item;

    private String name;
    private Type type;
    private int gameId;

    /**
     * Id used (exclusively?) for filtering games.
     */
    private long internalGameId;

    public Game() {
        this(Type.APP, NO_APP_ID);
    }

    public Game(Type type, int gameId) {
        this.type = type;
        this.gameId = gameId;
    }

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

    public long getInternalGameId() {
        return internalGameId;
    }

    public void setInternalGameId(long internalGameId) {
        this.internalGameId = internalGameId;
    }

    @Override
    public int hashCode() {
        return (int) internalGameId;
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
