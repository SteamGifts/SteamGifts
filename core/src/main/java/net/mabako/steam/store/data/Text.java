package net.mabako.steam.store.data;

import android.support.annotation.LayoutRes;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serializable;

public class Text implements IEndlessAdaptable, Serializable {
    private static final long serialVersionUID = 1675033471669858154L;
    public static final int VIEW_LAYOUT = R.layout.text_item;

    private final String text;
    private final boolean html, loadRemoteImages;

    @LayoutRes
    private final int layout;

    public Text(String text, boolean html) {
        this(text, html, VIEW_LAYOUT, false);
    }

    public Text(String text, boolean html, boolean loadRemoteImages) {
        this(text, html, VIEW_LAYOUT, loadRemoteImages);
    }

    public Text(String text, boolean html, @LayoutRes int layout, boolean loadRemoteImages) {
        this.text = text;
        this.html = html;
        this.layout = layout;
        this.loadRemoteImages = loadRemoteImages;
    }

    public String getText() {
        return text;
    }

    public boolean isHtml() {
        return html;
    }

    public boolean isLoadRemoteImages() {
        return loadRemoteImages;
    }

    @Override
    public int getLayout() {
        return layout;
    }
}
