package net.mabako.steam.store.data;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.core.R;

import java.io.Serializable;

public class Space implements IEndlessAdaptable, Serializable {
    private static final long serialVersionUID = 7071337769230964912L;
    public static int VIEW_LAYOUT = R.layout.endless_spacer;

    @Override
    public int getLayout() {
        return VIEW_LAYOUT;
    }
}
