package net.mabako.steamgifts.fragments.interfaces;

/**
 * Notifications from the {@link net.mabako.steamgifts.activities.MainActivity} to individual fragments.
 */
public interface IActivityTitle {
    /**
     * Resource for the Toolbar title
     * @return resource id
     */
    int getTitleResource();

    String getExtraTitle();
}
