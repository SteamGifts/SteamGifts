package net.mabako.store;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.adapters.IEndlessAdaptable;
import net.mabako.steamgifts.adapters.viewholder.GameViewHolder;
import net.mabako.steamgifts.data.Game;

import java.util.ArrayList;
import java.util.List;

public abstract class StoreFragment extends Fragment {
    protected int appId;
    private boolean loaded;

    private LoadStoreTask task;
    private View layout;

    private RecyclerView listView;
    protected Adapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_simple_list, container, false);

        listView = (RecyclerView) layout.findViewById(R.id.list);
        adapter = new Adapter();
        listView.setAdapter(adapter);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (task != null)
            task.cancel(true);
    }

    public abstract LoadStoreTask getTaskToStart();

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !loaded) {
            loaded = true;

            task = getTaskToStart();
            task.execute();
        }
    }

    public class Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private List<IEndlessAdaptable> items = new ArrayList<>();

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getContext()).inflate(viewType, parent, false);
            switch (viewType) {
                case Game.VIEW_LAYOUT:
                    return new GameViewHolder(view, StoreFragment.this);
            }

            throw new IllegalStateException("No View");
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder h, int position) {
            if (h instanceof GameViewHolder) {
                ((GameViewHolder) h).setFrom((Game) getItem(position));
            }
        }

        public IEndlessAdaptable getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).getLayout();
        }

        public void add(IEndlessAdaptable adaptable) {
            items.add(adaptable);
            notifyItemInserted(items.size() - 1);
        }
    }
}
