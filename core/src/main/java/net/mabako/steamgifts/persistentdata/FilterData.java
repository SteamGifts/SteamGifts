package net.mabako.steamgifts.persistentdata;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.Serializable;

/**
 * Giveaway filters.
 */
public class FilterData implements Serializable {
    private static final long serialVersionUID = 924136599147980741L;

    public static final String PREF_FILTER = "giveaway.filter";
    public static final String PREF_KEY_CONFIG = "filter-config";

    private static FilterData current = null;

    private int minEntries = -1, maxEntries = -1, minPoints = -1, maxPoints = -1, minLevel = -1, maxLevel = -1, minCopies = -1, maxCopies = -1;
    private boolean hideEntered, restrictLevelOnlyOnPublicGiveaways, entriesPerCopy, regionRestrictedOnly;

    public static synchronized FilterData getCurrent(Context context) {
        if (current == null) {
            // Load from preferences if possible
            SharedPreferences sp = context.getSharedPreferences(PREF_FILTER, Context.MODE_PRIVATE);

            String config = sp.getString(PREF_KEY_CONFIG, null);
            try {
                current = new Gson().fromJson(config, FilterData.class);
            } catch (JsonSyntaxException e) {
            }

            if (current == null)
                current = new FilterData();
        }
        return current;
    }

    public static void setCurrent(FilterData current) {
        FilterData.current = current;
    }

    public int getMaxEntries() {
        return maxEntries;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

    public int getMaxCopies() {
        return maxCopies;
    }

    public int getMinEntries() {
        return minEntries;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMinPoints() {
        return minPoints;
    }

    public int getMinCopies() {
        return minCopies;
    }

    public void setMaxEntries(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }

    public void setMaxPoints(int maxPoints) {
        this.maxPoints = maxPoints;
    }

    public void setMaxCopies(int maxCopies) {
        this.maxCopies = maxCopies;
    }

    public void setMinEntries(int minEntries) {
        this.minEntries = minEntries;
    }

    public void setMinLevel(int minLevel) {
        this.minLevel = minLevel;
    }

    public void setMinPoints(int minPoints) {
        this.minPoints = minPoints;
    }

    public void setMinCopies(int minCopies) {
        this.minCopies = minCopies;
    }

    public boolean isHideEntered() {
        return hideEntered;
    }

    public void setHideEntered(boolean hideEntered) {
        this.hideEntered = hideEntered;
    }

    public boolean isRestrictLevelOnlyOnPublicGiveaways() {
        return restrictLevelOnlyOnPublicGiveaways;
    }

    public void setRestrictLevelOnlyOnPublicGiveaways(boolean restrictLevelOnlyOnPublicGiveaways) {
        this.restrictLevelOnlyOnPublicGiveaways = restrictLevelOnlyOnPublicGiveaways;
    }

    public boolean isEntriesPerCopy() {
        return entriesPerCopy;
    }

    public void setEntriesPerCopy(boolean entriesPerCopy) {
        this.entriesPerCopy = entriesPerCopy;
    }

    public boolean isRegionRestrictedOnly() {
        return regionRestrictedOnly;
    }

    public void setRegionRestrictedOnly(boolean regionRestrictedOnly) {
        this.regionRestrictedOnly = regionRestrictedOnly;
    }

    public boolean isAnyActive() {
        return minEntries > -1 || maxEntries > -1 || minPoints > -1 || maxPoints > -1 || minLevel > -1 || maxLevel > -1 || minCopies > -1 || maxCopies > -1 || hideEntered || regionRestrictedOnly;
    }
}
