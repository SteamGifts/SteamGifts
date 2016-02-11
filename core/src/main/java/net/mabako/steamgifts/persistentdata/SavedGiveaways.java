package net.mabako.steamgifts.persistentdata;

import android.content.Context;

import com.google.gson.Gson;

import net.mabako.steamgifts.data.Giveaway;

public class SavedGiveaways extends SavedElements<Giveaway> {
    static final String DB_TABLE = "giveaways";

    public SavedGiveaways(Context context) {
        super(context, DB_TABLE);
    }

    @Override
    protected Giveaway getElement(Gson gson, String json) {
        Giveaway giveaway = gson.fromJson(json, Giveaway.class);

        giveaway.setEntries(-1);
        giveaway.setEntered(false);

        return giveaway;
    }

    @Override
    public int compare(Giveaway lhs, Giveaway rhs) {
        return lhs.getEndTime().compareTo(rhs.getEndTime());
    }
}
