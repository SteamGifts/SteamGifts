package net.mabako.steam.store.data;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;

public class Text implements IEndlessAdaptable, Serializable {
    private static final long serialVersionUID = 1675033471669858154L;
    public static final int VIEW_LAYOUT = R.layout.text_item;

    private final String text;
    private final boolean html;

    public Text(String text, boolean html) {
        this.text = text;
        this.html = html;
    }

    public String getText() {
        return text;
    }

    public boolean isHtml() {
        return html;
    }

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
