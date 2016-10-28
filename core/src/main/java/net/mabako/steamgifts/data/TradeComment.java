package net.mabako.steamgifts.data;

public class TradeComment extends Comment {
    private static final long serialVersionUID = 7399592725523466062L;

    private int tradeScorePositive, tradeScoreNegative;
    private long steamID64;

    public TradeComment(long id, String author, int depth, String avatar, boolean isOp, Type type) {
        super(id, author, depth, avatar, isOp, type);
    }

    public int getTradeScorePositive() {
        return tradeScorePositive;
    }

    public void setTradeScorePositive(int tradeScorePositive) {
        this.tradeScorePositive = tradeScorePositive;
    }

    public int getTradeScoreNegative() {
        return tradeScoreNegative;
    }

    public void setTradeScoreNegative(int tradeScoreNegative) {
        this.tradeScoreNegative = tradeScoreNegative;
    }

    public void setSteamID64(long steamID64) {
        this.steamID64 = steamID64;
    }

    public long getSteamID64() {
        return steamID64;
    }
}
