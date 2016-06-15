package net.mabako.steamgifts.fragments.util;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Trade;
import net.mabako.steamgifts.data.TradeExtras;

import java.io.Serializable;

public class TradeDetailsCard implements IEndlessAdaptable, Serializable {
    private static final long serialVersionUID = -4139266911462136080L;
    public static final int VIEW_LAYOUT = R.layout.trade_detail_card;

    private Trade trade;
    private TradeExtras extras;

    public TradeDetailsCard() {

    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }

    public Trade getTrade() {
        return trade;
    }

    public void setTrade(Trade trade) {
        this.trade = trade;
    }

    public TradeExtras getExtras() {
        return extras;
    }

    public void setExtras(TradeExtras extras) {
        this.extras = extras;
    }
}
