package net.mabako.steamgifts.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.BasicUser;
import net.mabako.steamgifts.fragments.WhitelistBlacklistFragment;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class UserViewHolder extends RecyclerView.ViewHolder {
    private final WhitelistBlacklistFragment fragment;

    private final View userHolder;

    private final TextView userName;
    private final ImageView userAvatar;

    private final Button removeUser;

    public UserViewHolder(View itemView, WhitelistBlacklistFragment fragment) {
        super(itemView);
        this.fragment = fragment;

        userHolder = itemView.findViewById(R.id.user_holder);

        userName = (TextView) itemView.findViewById(R.id.user);
        userAvatar = (ImageView) itemView.findViewById(R.id.avatar);

        removeUser = (Button) itemView.findViewById(R.id.remove_user);
    }

    public void setFrom(final BasicUser user) {
        userHolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.showProfile(user.getName());
            }
        });

        userName.setText(user.getName());
        Picasso.with(fragment.getContext()).load(user.getAvatar()).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into(userAvatar);

        removeUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.requestUserListed(user, fragment.getType(), false);
            }
        });
    }
}
