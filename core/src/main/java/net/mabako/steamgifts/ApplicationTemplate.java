package net.mabako.steamgifts;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public abstract class ApplicationTemplate extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PeriodicTasks.scheduleAllTasks(getBaseContext());
    }

    /**
     * Setup Crashlytics.
     */
    protected void setupFabric() {
        Fabric.with(new Fabric.Builder(this).kits(new Crashlytics()).build());
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
}
