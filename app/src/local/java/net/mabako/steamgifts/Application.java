package net.mabako.steamgifts;

public class Application extends ApplicationTemplate {
    @Override
    public String getAppVersionName() {
        return BuildConfig.VERSION_NAME;
    }

    @Override
    public int getAppVersionCode() {
        return BuildConfig.VERSION_CODE;
    }

    @Override
    public String getFlavor() {
        return BuildConfig.FLAVOR;
    }
}