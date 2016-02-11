package net.mabako.steamgifts.persistentdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Any giveaways the user wishes to save for a long(er) time.
 */
public abstract class SavedElements<T> implements Comparator<T> {
    private static final String TAG = SavedElements.class.getSimpleName();

    private final String table;
    private final Context context;

    private static final String KEY_ID = "id";
    private static final String KEY_VALUE = "value";

    protected final GiveawayOpenHelper<T> helper;

    public SavedElements(Context context, String table) {
        helper = new GiveawayOpenHelper<T>(context, this);

        this.context = context;
        this.table = table;
    }

    /**
     * Returns a list of saved elements.
     *
     * @return list of savedelements
     */
    public List<T> all() {
        return helper.all();
    }

    /**
     * Adds a single element to be persisted.
     *
     * @param element   the element to be saved
     * @param elementId the id of the element
     * @return true if the element was saved, false otherwise
     */
    public boolean add(@NonNull T element, @NonNull String elementId) {
        return helper.add(element, elementId);
    }

    /**
     * Returns a single persistent element.
     *
     * @param elementId the id of the element
     * @return the found element, or null if not existant.
     */
    @Nullable
    public T get(@NonNull String elementId) {
        return helper.get(elementId);
    }

    /**
     * Removes an element.
     *
     * @param elementId the id of the element
     * @return true if the element was deleted, false otherwise
     */
    public boolean remove(@NonNull String elementId) {
        return helper.remove(elementId);
    }

    /**
     * Is the element with the given id saved?
     *
     * @param elementId the id of the element
     * @return true if the element is saved, false otherwise
     */
    public boolean exists(@NonNull String elementId) {
        return helper.exists(elementId);
    }

    protected abstract T getElement(Gson gson, String json);

    public void close() {
        helper.close();
    }

    protected Context getContext() {
        return context;
    }

    private static class GiveawayOpenHelper<T> extends SQLiteOpenHelper {
        private final SavedElements<T> parent;

        public GiveawayOpenHelper(Context context, SavedElements<T> parent) {
            super(context, "savedelements", null, 3);
            this.parent = parent;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + SavedGiveaways.DB_TABLE + "(" + KEY_ID + " text primary key, " + KEY_VALUE + " text)");
            db.execSQL("CREATE TABLE " + SavedDiscussions.DB_TABLE + "(" + KEY_ID + " text primary key, " + KEY_VALUE + " text)");
        }

        private boolean add(T element, String elementId) {
            ContentValues values = new ContentValues();
            values.put(KEY_ID, elementId);
            values.put(KEY_VALUE, new Gson().toJson(element));

            SQLiteDatabase db = getWritableDatabase();
            return db.replace(parent.table, null, values) != -1;
        }

        public List<T> all() {
            List<T> elements = new ArrayList<T>();
            Gson gson = new Gson();

            Cursor cursor = getReadableDatabase().query(parent.table, new String[]{KEY_VALUE}, null, null, null, null, null, null);
            try {
                while (cursor.moveToNext()) {
                    elements.add(parent.getElement(gson, cursor.getString(0)));
                }
            } finally {
                cursor.close();
            }

            Collections.sort(elements, parent);

            return elements;
        }

        public T get(String elementId) {
            Cursor cursor = getReadableDatabase().query(parent.table, new String[]{KEY_VALUE}, KEY_ID + " = ?", new String[]{elementId}, null, null, null, null);
            try {
                if (cursor.moveToFirst())
                    return parent.getElement(new Gson(), cursor.getString(0));

                return null;
            } finally {
                cursor.close();
            }
        }

        public boolean exists(String elementId) {
            Cursor cursor = getReadableDatabase().query(parent.table, new String[]{KEY_ID}, KEY_ID + " = ?", new String[]{elementId}, null, null, null, null);
            boolean exists = cursor.getCount() > 0;
            cursor.close();

            return exists;
        }

        public boolean remove(String elementId) {
            return getWritableDatabase().delete(parent.table, KEY_ID + " = ?", new String[]{elementId}) > 0;
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

            // Delete all saved giveawys
            if (oldVersion < 2)
                db.delete(SavedGiveaways.DB_TABLE, null, null);

            // Create a new table for saved discussions
            if (oldVersion < 3)
                db.execSQL("CREATE TABLE " + SavedDiscussions.DB_TABLE + "(" + KEY_ID + " text primary key, " + KEY_VALUE + " text)");
        }
    }
}