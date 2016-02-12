package net.mabako.steamgifts.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.mabako.steamgifts.adapters.viewholder.UserViewHolder;
import net.mabako.steamgifts.data.BasicUser;
import net.mabako.steamgifts.data.Winner;
import net.mabako.steamgifts.fragments.ListFragment;

import java.util.List;

public class UserAdapter extends EndlessAdapter {
    private static final long serialVersionUID = -1382099248508481575L;

    /**
     * Users that are shown per page.
     */
    private static final int ITEMS_PER_PAGE = 25;

    private transient ListFragment<?> fragment;

    public UserAdapter() {
        alternativeEnd = true;
    }

    public void setFragmentValues(ListFragment<?> fragment) {
        setLoadListener(fragment);
        this.fragment = fragment;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        if (viewType == BasicUser.VIEW_LAYOUT || viewType == Winner.VIEW_LAYOUT)
            return new UserViewHolder(view, fragment);

        throw new IllegalStateException("view tpye " + viewType + " is unknown");
    }

    @Override
    protected void onBindActualViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((UserViewHolder) holder).setFrom((BasicUser) getItem(position));
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        return items.size() == ITEMS_PER_PAGE;
    }

    public RemovedElement removeUser(int userId) {
        if (userId == 0)
            throw new IllegalStateException();

        for (int position = getItems().size() - 1; position >= 0; --position) {
            BasicUser user = (BasicUser) getItem(position);

            if (user != null && user.getId() == userId) {
                return removeItem(position);
            }
        }

        return null;
    }
}
