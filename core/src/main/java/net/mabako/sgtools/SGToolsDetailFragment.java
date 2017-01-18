package net.mabako.sgtools;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.activities.DetailActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicGiveaway;
import net.mabako.steamgifts.fragments.GiveawayDetailFragment;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * View a giveaway on <a href="http://www.sgtools.info">SGTools</a>.
 */
public class SGToolsDetailFragment extends Fragment implements View.OnClickListener {
    public static final String ARG_UUID = "uuid";
    private static final String SAVED_UUID = "uuid";
    private static final String SAVED_GIVEAWAY = "giveaway";

    private UUID uuid;
    private Giveaway giveaway;

    private CollapsingToolbarLayout appBarLayout;
    private View layout;

    private AsyncTask<Void, Void, ?> task = null;

    public static SGToolsDetailFragment newInstance(UUID uuid) {
        SGToolsDetailFragment fragment = new SGToolsDetailFragment();

        Bundle args = new Bundle();
        args.putSerializable(SAVED_UUID, uuid);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            uuid = (UUID) getArguments().getSerializable(SAVED_UUID);
        } else {
            uuid = (UUID) savedInstanceState.getSerializable(SAVED_UUID);
            giveaway = (Giveaway) savedInstanceState.getSerializable(SAVED_GIVEAWAY);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(SAVED_UUID, uuid);
        outState.putSerializable(SAVED_GIVEAWAY, giveaway);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.fragment_sgtools, container, false);

        appBarLayout = (CollapsingToolbarLayout) getActivity().findViewById(R.id.toolbar_layout);
        appBarLayout.setTitle("Loading SGTools...");

        if (giveaway != null) {
            onGiveawayLoaded(giveaway);
        } else {
            task = new LoadGiveawayTask(this, uuid);
            task.execute();
        }

        return layout;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (task != null) {
            task.cancel(true);
            task = null;
        }
    }

    public void onGiveawayLoaded(Giveaway giveaway) {
        this.giveaway = giveaway;
        appBarLayout.setTitle(giveaway.getName());

        ImageView toolbarImage = (ImageView) getActivity().findViewById(R.id.toolbar_image);
        Picasso.with(getContext()).load("http://cdn.akamai.steamstatic.com/steam/" + giveaway.getType().name().toLowerCase(Locale.ENGLISH) + "s/" + giveaway.getGameId() + "/header.jpg").into(toolbarImage, new Callback() {
            @Override
            public void onSuccess() {
                appBarLayout.setExpandedTitleTextAppearance(R.style.TransparentText);
            }

            @Override
            public void onError() {

            }
        });

        layout.findViewById(R.id.progressBar).setVisibility(View.GONE);

        Button checkButton = (Button) layout.findViewById(R.id.sgtools_check);
        checkButton.setVisibility(View.VISIBLE);
        checkButton.setOnClickListener(this);

        RecyclerView listView = (RecyclerView) layout.findViewById(R.id.list);
        listView.setVisibility(View.VISIBLE);
        listView.setLayoutManager(new LinearLayoutManager(getContext()));
        listView.setAdapter(new ListAdapter(giveaway.getRules()));
    }

    public void requestLogin() {
        getActivity().startActivityForResult(new Intent(getActivity(), SGToolsLoginActivity.class), CommonActivity.REQUEST_LOGIN_SGTOOLS);
    }

    /**
     * Clicked the "Check" button
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        v.setEnabled(false);

        task = new LoadGiveawayLinkTask(this, uuid);
        task.execute();
    }

    public void onCheckSuccessful(String giveawayUrl) {
        Uri uri = Uri.parse(giveawayUrl);

        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(GiveawayDetailFragment.ARG_GIVEAWAY, new BasicGiveaway(uri.getPathSegments().get(1)));

        getActivity().startActivityForResult(intent, CommonActivity.REQUEST_LOGIN);
        getActivity().finish();
    }

    public void onCheckFailed(String errorMessage) {
        layout.findViewById(R.id.sgtools_check).setEnabled(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage(errorMessage);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.show();
    }

    private static class ListAdapter extends RecyclerView.Adapter<ListViewHolder> {
        private final List<String> list;

        public ListAdapter(List<String> list) {
            this.list = list;
        }

        @Override
        public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sgtools_rule_item, parent, false);
            return new ListViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ListViewHolder holder, int position) {
            holder.textView.setText(list.get(position));
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private static class ListViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        public ListViewHolder(View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.text);
        }
    }
}
