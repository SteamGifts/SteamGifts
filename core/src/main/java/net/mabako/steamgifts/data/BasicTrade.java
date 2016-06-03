package net.mabako.steamgifts.data;

import java.io.Serializable;


public class BasicTrade implements Serializable {
    private static final long serialVersionUID = -3232872009471753927L;
    private final String tradeId;

    public BasicTrade(String tradeId) {
        this.tradeId = tradeId;
    }

    public String getTradeId() {
        return tradeId;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof BasicTrade) || tradeId == null)
            return false;

        return tradeId.equals(((BasicTrade) o).tradeId);
    }

    @Override
    public int hashCode() {
        return tradeId == null ? 0 : tradeId.hashCode();
    }
}
