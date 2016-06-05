package net.mabako.steamgifts;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;

public abstract class ApplicationTemplate extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PeriodicTasks.scheduleAllTasks(getBaseContext());
    }

    /**
     * Is this a beta build?
     *
     * @return true if this is a beta build, false otherwise
     */
    public boolean isBetaBuild() {
        return false;
    }

    /**
     * Show a beta notification if this is the first time using this app.
     *
     * @param parentActivity    the activity this is called from
     * @param onlyOnFirstLaunch if set to true, this dialog will not pop up on subsequent launches
     * @see #isBetaBuild()
     */
    public void showBetaNotification(AppCompatActivity parentActivity, boolean onlyOnFirstLaunch) {
        // Probably not a beta build.
    }

    /**
     * Current Version of this application.
     *
     * @return current application version
     */
    public abstract String getAppVersionName();

    /**
     * Current version number of this application.
     *
     * @return current application version code
     */
    public abstract int getAppVersionCode();

    /**
     * Current version flavor.
     */
    public abstract String getFlavor();

    /**
     * Whether or not game images may be shown.
     * <p/>
     * Since game images for giveaways by necessity contain "copyrighted images of video games",
     * these will be hidden on Google Play builds.
     *
     * @return true if game images should be shown
     */
    public boolean allowGameImages() {
        return true;
    }

    /**
     * Whether or not a paypal donation link is shown in the "About" section.
     *
     * @return true if paypal donation link should be shown
     */
    public boolean allowPaypalDonations() {
        return true;
    }
}
