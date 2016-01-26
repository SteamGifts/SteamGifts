package net.mabako.steamgifts;

public class Application extends ApplicationTemplate {
    @Override
    public void onCreate() {
        super.onCreate();
        setupFabric(BuildConfig.FLAVOR);
    }
}
