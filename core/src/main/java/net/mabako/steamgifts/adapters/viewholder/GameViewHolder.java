package net.mabako.steamgifts.adapters.viewholder;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.mabako.steam.store.StoreSubFragment;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Game;
import net.mabako.steamgifts.fragments.HiddenGamesFragment;

import java.util.Locale;

public class GameViewHolder extends RecyclerView.ViewHolder {
    private final TextView gameName;
    private final ImageView image;

    private final Button removeGame;

    private final Fragment fragment;

    private static int measuredHeight = 0;

    public GameViewHolder(View itemView, Fragment fragment) {
        super(itemView);
        this.fragment = fragment;

        gameName = (TextView) itemView.findViewById(R.id.game_name);
        image = (ImageView) itemView.findViewById(R.id.game_image);

        removeGame = (Button) itemView.findViewById(R.id.remove_game);
    }

    public void setFrom(final Game game) {
        gameName.setText(game.getName());

        if (fragment instanceof HiddenGamesFragment && game.getInternalGameId() != Game.NO_APP_ID) {
            removeGame.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((HiddenGamesFragment) fragment).requestShowGame(game.getInternalGameId(), game.getName());
                }
            });
            removeGame.setVisibility(View.VISIBLE);
        } else if (fragment instanceof StoreSubFragment && game.getGameId() != Game.NO_APP_ID) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((StoreSubFragment) fragment).showDetails(game.getGameId());
                }
            });
        }

        // giveaway_image
        if (game.getGameId() != Game.NO_APP_ID) {
            Picasso.with(fragment.getContext()).load("http://cdn.akamai.steamstatic.com/steam/" + game.getType().name().toLowerCase(Locale.ENGLISH) + "s/" + game.getGameId() + "/capsule_184x69.jpg").into(image, new Callback() {
                /**
                 * We manually set the height of this image to fit the container.
                 */
                @Override
                public void onSuccess() {
                    if (measuredHeight <= 0)
                        measuredHeight = itemView.getMeasuredHeight();

                    ViewGroup.LayoutParams params = image.getLayoutParams();
                    params.height = measuredHeight;
                }

                @Override
                public void onError() {

                }
            });
        } else {
            image.setImageResource(android.R.color.transparent);
        }
    }
}
