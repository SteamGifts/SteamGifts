package net.mabako.steamgifts.data;

import net.mabako.steamgifts.core.R;

public class Winner extends BasicUser {
    public static final int VIEW_LAYOUT = R.layout.winner_item;
    private static final long serialVersionUID = 5793503158843335275L;

    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
