package net.mabako.steamgifts.fragments;

/**
 * Notifications from the {@link net.mabako.steamgifts.activities.MainActivity} to individual fragments.
 */
public interface IFragmentNotifications {
    /**
     * Resource for the Toolbar title
     * @return resource id
     */
    int getTitleResource();

    String getExtraTitle();
}
