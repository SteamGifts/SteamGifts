package net.mabako.steamgifts.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import com.mikepenz.fontawesome_typeface_library.FontAwesome;

import net.mabako.steamgifts.adapters.TradeAdapter;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.fragments.interfaces.IActivityTitle;
import net.mabako.steamgifts.tasks.LoadTradesListTask;

public class TradeListFragment extends SearchableListFragment<TradeAdapter> implements IActivityTitle {
    private static final String SAVED_TYPE = "type";

    /**
     * Type of items to show.
     */
    private Type type = Type.ALL;

    public static TradeListFragment newInstance(Type type, String query) {
        TradeListFragment fragment = new TradeListFragment();

        Bundle args = new Bundle();
        args.putSerializable(SAVED_TYPE, type);
        args.putSerializable(SAVED_QUERY, query);
        fragment.setArguments(args);

        fragment.type = type;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            type = (Type) getArguments().getSerializable(SAVED_TYPE);
        } else {
            type = (Type) savedInstanceState.getSerializable(SAVED_TYPE);
        }

        adapter.setFragmentValues(this, getActivity());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_TYPE, type);
    }

    @NonNull
    @Override
    protected TradeAdapter createAdapter() {
        return new TradeAdapter();
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTask(int page) {
        return new LoadTradesListTask(this, page, type, getSearchQuery());
    }


    @Override
    public int getTitleResource() {
        return type.getTitleResource();
    }

    @Override
    public String getExtraTitle() {
        return null;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public Fragment newSearchingInstance(String query) {
        return newInstance(type, query);
    }

    /**
     * Different types of discussion lists.
     */
    public enum Type {
        ALL(R.string.navigation_trades_all, R.string.navigation_trades_all_title, FontAwesome.Icon.faw_exchange),
        CREATED(R.string.navigation_trades_created, R.string.navigation_trades_created_title, FontAwesome.Icon.faw_plus_circle);


        private final int titleResource;
        private final int navbarResource;
        private final FontAwesome.Icon icon;

        Type(int navbarResource, int titleResource, FontAwesome.Icon icon) {
            this.navbarResource = navbarResource;
            this.titleResource = titleResource;
            this.icon = icon;
        }

        public int getTitleResource() {
            return titleResource;
        }

        public int getNavbarResource() {
            return navbarResource;
        }

        public FontAwesome.Icon getIcon() {
            return icon;
        }
    }
}
