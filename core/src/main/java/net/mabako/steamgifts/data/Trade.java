package net.mabako.steamgifts.data;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.io.Serializable;

public class Trade implements Serializable, IEndlessAdaptable {
    private static final long serialVersionUID = -4828592066348698719L;

    @Override
    public int getLayout() {
        return 0;
    }
}
