package net.mabako.steamgifts.persistentdata;

import android.content.Context;

import com.google.gson.Gson;

import net.mabako.steamgifts.data.Discussion;

public class SavedDiscussions extends SavedElements<Discussion> {
    static final String DB_TABLE = "discussions";

    public SavedDiscussions(Context context) {
        super(context, DB_TABLE);
    }

    @Override
    protected Discussion getElement(Gson gson, String json) {
        return gson.fromJson(json, Discussion.class);
    }

    @Override
    public int compare(Discussion lhs, Discussion rhs) {
        return lhs.getTitle().compareTo(rhs.getTitle());
    }
}
