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

    /**
     * Not possible with the current policies of the Google Play Store.
     *
     * @return false
     */
    @Override
    public boolean allowGameImages() {
        return false;
    }

    /**
     * Paypal donations aren't approved by Google, usually.
     *
     * @return false
     */
    @Override
    public boolean allowPaypalDonations() {
        return false;
    }
}
