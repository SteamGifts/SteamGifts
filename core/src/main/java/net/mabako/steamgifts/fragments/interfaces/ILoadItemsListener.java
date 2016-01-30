package net.mabako.steamgifts.fragments.interfaces;

import net.mabako.steamgifts.adapters.IEndlessAdaptable;

import java.util.List;

public interface ILoadItemsListener {
    void addItems(List<? extends IEndlessAdaptable> items, boolean clearExistingItems, String xsrfToken);
}
