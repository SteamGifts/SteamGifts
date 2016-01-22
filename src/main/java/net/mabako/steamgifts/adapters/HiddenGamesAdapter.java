package net.mabako.steamgifts.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import net.mabako.steamgifts.adapters.viewholder.GameViewHolder;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.HiddenGamesFragment;

import java.util.List;

public class HiddenGamesAdapter extends EndlessAdapter {
    /**
     * Discussions that are shown per page.
     */
    private static final int ITEMS_PER_PAGE = 25;

    private String xsrfToken;
    private final HiddenGamesFragment fragment;

    public HiddenGamesAdapter(HiddenGamesFragment fragment, @NonNull OnLoadListener listener) {
        super(listener);
        this.alternativeEnd = true;
        this.fragment = fragment;
    }

    @Override
    protected RecyclerView.ViewHolder onCreateActualViewHolder(View view, int viewType) {
        return new GameViewHolder(view, fragment);
    }

    @Override
    protected void onBindActualViewHolder(RecyclerView.ViewHolder h, int position) {
        if (h instanceof GameViewHolder) {
            Game game = (Game) getItem(position);
            GameViewHolder holder = (GameViewHolder) h;

            holder.setFrom(game);
        }
    }

    @Override
    protected boolean hasEnoughItems(List<IEndlessAdaptable> items) {
        return items.size() == ITEMS_PER_PAGE;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public void setXsrfToken(String xsrfToken) {
        this.xsrfToken = xsrfToken;
    }

    public void removeShownGame(int internalGameId) {
        if (internalGameId == Game.NO_APP_ID)
            throw new IllegalStateException();

        for (int position = getItems().size() - 1; position >= 0; --position) {
            Game game = (Game) getItem(position);

            if (game != null && game.getInternalGameId() == internalGameId) {
                removeItem(position);
            }
        }
    }
}
