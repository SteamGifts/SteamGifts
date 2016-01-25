package net.mabako.steamgifts.persistentdata;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
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

    private static final String KEY_ID = "id";
    private static final String KEY_VALUE = "value";

    protected final GiveawayOpenHelper<T> helper;

    public SavedElements(Context context, String table) {
        helper = new GiveawayOpenHelper<T>(context, this);

        this.table = table;
    }

    /**
     * Returns a list of saved elements.
     *
     * @return list of savedelements
     */
    public List<T> getGiveaways() {
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
    public boolean isSaved(@NonNull String elementId) {
        return helper.isSaved(elementId);
    }

    protected abstract T getElement(Gson gson, String json);

    private static class GiveawayOpenHelper<T> extends SQLiteOpenHelper {
        private final SavedElements<T> parent;

        public GiveawayOpenHelper(Context context, SavedElements<T> parent) {
            super(context, "savedelements", null, 1);
            this.parent = parent;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + SavedGiveaways.DB_TABLE + "(" + KEY_ID + " text primary key, " + KEY_VALUE + " text)");
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

            Cursor cursor = getReadableDatabase().query(parent.table, new String[]{KEY_VALUE}, null, null, null, null, null);
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

        public boolean isSaved(String elementId) {
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
        }
    }
}