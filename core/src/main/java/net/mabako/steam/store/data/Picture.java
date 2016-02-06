package net.mabako.steam.store.data;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;

public class Picture implements IEndlessAdaptable, Serializable {
    private static final long serialVersionUID = 1373131985788155321L;
    public static final int VIEW_LAYOUT = R.layout.store_picture;

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
