package net.mabako.steamgifts.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.adapters.EndlessAdapter;
import net.mabako.steamgifts.data.BasicTrade;
import net.mabako.steamgifts.data.Trade;
import net.mabako.steamgifts.data.TradeExtras;
import net.mabako.steamgifts.fragments.util.TradeDetailsCard;
import net.mabako.steamgifts.tasks.LoadTradeDetailsTask;

import java.io.Serializable;

public class TradeDetailFragment extends DetailFragment {
    public static final String ARG_TRADE = "trade";

    private static final String TAG = TradeDetailFragment.class.getSimpleName();

    private static final String SAVED_TRADE = ARG_TRADE;
    private static final String SAVED_CARD = "trade-card";

    /**
     * Content to show for the trade details.
     */
    private BasicTrade trade;
    private TradeDetailsCard tradeCard;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            trade = (BasicTrade) getArguments().getSerializable(SAVED_TRADE);
            tradeCard = new TradeDetailsCard();
        } else {
            trade = (BasicTrade) savedInstanceState.getSerializable(SAVED_TRADE);
            tradeCard = (TradeDetailsCard) savedInstanceState.getSerializable(SAVED_CARD);
        }

        adapter.setFragmentValues(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_TRADE, trade);
        outState.putSerializable(SAVED_CARD, tradeCard);
    }

    public static Fragment newInstance(@NonNull BasicTrade trade, @Nullable CommentContextInfo context) {
        TradeDetailFragment d = new TradeDetailFragment();

        Bundle args = new Bundle();
        args.putSerializable(SAVED_TRADE, trade);
        args.putSerializable(SAVED_COMMENT_CONTEXT, context);
        d.setArguments(args);

        return d;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = super.onCreateView(inflater, container, savedInstanceState);

        if (trade instanceof Trade) {
            onPostTradeLoaded((Trade) trade, true);
        } else {
            Log.d(TAG, "Loading activity for basic trade " + trade.getTradeId());
        }

        // Add the cardview for the Giveaway details
        adapter.setStickyItem(tradeCard);

        // To reverse or not to reverse?
        // TODO should this be a separate setting for discussions & trades?
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (prefs.getBoolean("preference_discussion_comments_reversed", false) && getCommentContext() == null) {
            adapter.setViewInReverse();
            fetchItems(EndlessAdapter.LAST_PAGE);
        } else {
            fetchItems(EndlessAdapter.FIRST_PAGE);
        }
        setHasOptionsMenu(true);

        return layout;
    }

    @Override
    protected AsyncTask<Void, Void, ?> getFetchItemsTaskEx(int page) {
        String url = trade.getTradeId();
        if (trade instanceof Trade)
            url += "/" + ((Trade) trade).getName();
        else if (getCommentContext() != null)
            url += "/" + getCommentContext().getDetailName();
        else
            url += "/sgforandroid";

        return new LoadTradeDetailsTask(this, url, page, !(trade instanceof Trade));
    }

    public void onPostTradeLoaded(Trade trade, boolean ignoreExisting) {
        // Called this twice, eh...
        if (this.trade instanceof Trade && !ignoreExisting)
            return;

        this.trade = trade;
        tradeCard.setTrade(trade);

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getTitle());
            }
            activity.supportInvalidateOptionsMenu();
        }
    }

    public void onPostTradeLoaded(Trade trade) {
        onPostTradeLoaded(trade, false);
    }

    public void addItems(TradeExtras extras, int page, boolean lastPage) {
        if (extras == null)
            return;

        if (!(trade instanceof Trade))
            throw new IllegalStateException("#onPostTradeLoaded was probably not called");

        tradeCard.setExtras(extras);
        adapter.setStickyItem(tradeCard);

        adapter.notifyPage(getCommentContext() != null ? 1 : page, lastPage);
        addItems(extras.getComments(), false, extras.getXsrfToken());

        if (getActivity() != null)
            getActivity().supportInvalidateOptionsMenu();
    }

    @NonNull
    @Override
    protected Serializable getDetailObject() {
        return trade;
    }

    @Nullable
    @Override
    protected String getDetailPath() {
        if (trade instanceof Trade)
            return "trade/" + trade.getTradeId() + "/" + ((Trade) trade).getName();

        return null;
    }

    @Override
    protected String getTitle() {
        return trade instanceof Trade ? ((Trade) trade).getTitle() : null;
    }

    @Override
    public void showProfile(String user) {
        Intent intent = new Intent(getActivity(), DetailActivity.class);
        intent.putExtra(UserDetailFragment.ARG_USER, user);
        getActivity().startActivity(intent);
    }
}
