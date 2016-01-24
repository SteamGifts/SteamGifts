package net.mabako.steamgifts.persistentdata;

import android.content.Context;

import com.google.gson.Gson;

import net.mabako.steamgifts.data.Giveaway;

public class SavedGiveaways extends SavedElements<Giveaway> {
    public static final String DB_TABLE = "giveaways";

    public SavedGiveaways(Context context) {
        super(context, DB_TABLE);
    }

    @Override
    protected Giveaway getElement(Gson gson, String json) {
        Giveaway giveaway = gson.fromJson(json, Giveaway.class);

        giveaway.setTimeCreated(null);
        giveaway.setTimeRemaining(null);
        giveaway.setEntered(false);
        giveaway.setEntries(-1);

        return giveaway;
    }
}
