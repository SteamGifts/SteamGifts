package net.mabako.steam.store;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steam.store.data.Picture;
import net.mabako.steam.store.data.Space;
import net.mabako.steam.store.data.Text;
import net.mabako.steam.store.viewholder.PictureViewHolder;
import net.mabako.steam.store.viewholder.TextViewHolder;
import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.adapters.viewholder.GameViewHolder;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.ListFragment;

import java.io.Serializable;
import java.util.List;

public abstract class StoreFragment extends ListFragment<StoreFragment.Adapter> {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter.setFragmentValues(this);
    }

    @Override
    @NonNull
    protected Adapter createAdapter() {
        return new Adapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public static class Adapter extends EndlessAdapter {
        private static final long serialVersionUID = 7169019794513055782L;
        private transient StoreFragment fragment;

        public void setFragmentValues(StoreFragment fragment) {
            this.fragment = fragment;
        }

        @Override
        protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
            if (fragment == null)
                throw new IllegalStateException("no fragment");

            if (viewType == Space.VIEW_LAYOUT) {
                return new RecyclerView.ViewHolder(view) {
                };
            } else if (viewType == Game.VIEW_LAYOUT) {
                return new GameViewHolder(view, fragment);
            } else if (viewType == Picture.VIEW_LAYOUT) {
                return new PictureViewHolder(view, fragment.getContext());
            } else if (viewType == Text.VIEW_LAYOUT || viewType == R.layout.endless_scroll_end) {
                return new TextViewHolder(view, fragment.getContext());
            }

            throw new IllegalStateException("No View");
        }

        @Override
        protected void onBindActualViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof GameViewHolder) {
                ((GameViewHolder) holder).setFrom((Game) getItem(position));
            } else if (holder instanceof PictureViewHolder) {
                ((PictureViewHolder) holder).setFrom((Picture) getItem(position));
            } else if (holder instanceof TextViewHolder) {
                ((TextViewHolder) holder).setFrom((Text) getItem(position));
            }
        }

        @Override
        protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
            return !items.isEmpty();
        }

        @Override
        public void finishLoading(List<IEndlessAdaptable> addedItems) {
            super.finishLoading(addedItems);
            reachedTheEnd(false);
        }
    }

    // FIXME this isn't properly reset on device rotations; while the main giveaway page has an options menu afterwards, the store pages do not unless they're opened again.
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (getActivity() == null)
            return;

        ((CommonActivity) getActivity()).getCurrentFragment().onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (getActivity() == null)
            return super.onOptionsItemSelected(item);

        return ((CommonActivity) getActivity()).getCurrentFragment().onOptionsItemSelected(item);
    }

    @Override
    protected Serializable getType() {
        return null;
    }
}
