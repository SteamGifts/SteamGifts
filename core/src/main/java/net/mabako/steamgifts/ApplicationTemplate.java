package net.mabako.steamgifts;

import android.app.Application;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public abstract class ApplicationTemplate extends Application {
    protected void setupFabric(String flavor) {
        Log.d("ApplicationTemplate", "current flavor: " + flavor);
        if ("out".equals(flavor))
            Fabric.with(new Fabric.Builder(this).kits(new Crashlytics()).build());
    }
}
