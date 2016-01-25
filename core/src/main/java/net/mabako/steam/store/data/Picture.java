package net.mabako.steam.store.data;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

public class Picture implements IEndlessAdaptable {
    public static final int VIEW_LAYOUT = R.layout.image_item;

    private final String url;

    public Picture(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
