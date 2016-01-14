package net.mabako.steamgifts.intro;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class Slide extends Fragment {
    private SubView subview;

    public static Slide newInstance(SubView subview) {
        Bundle args = new Bundle();
        args.putSerializable("subview", subview);

        Slide fragment = new Slide();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        subview = (SubView) getArguments().getSerializable("subview");

        View view = inflater.inflate(subview.getLayout(), container, false);
        onCreateSubView(view.findViewById(R.id.intro_container));
        return view;
    }

    public void onCreateSubView(View view) {
        switch (subview) {
            case MAIN_GIVEAWAY_1:
                // Giveaway
                View giveawayView = view.findViewById(R.id.giveaway);
                for (int id : new int[]{R.id.separator, R.id.giveaway_list_indicator_group, R.id.giveaway_list_indicator_level_negative, R.id.giveaway_list_indicator_level_positive, R.id.giveaway_list_indicator_private, R.id.giveaway_list_indicator_whitelist})
                    giveawayView.findViewById(id).setVisibility(View.GONE);

                // Comment
                View commentView = view.findViewById(R.id.comment);
                Picasso.with(getContext()).load(R.drawable.default_avatar).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into((ImageView) (commentView.findViewById(R.id.author_avatar)));
                commentView.findViewById(R.id.comment_indent).getLayoutParams().width = 0;
                break;

            case MAIN_GIVEAWAY_2:
                view.findViewById(R.id.separator).setVisibility(View.GONE);
                break;
        }
    }
}