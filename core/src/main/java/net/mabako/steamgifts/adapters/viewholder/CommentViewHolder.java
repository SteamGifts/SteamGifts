package net.mabako.steamgifts.adapters.viewholder;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.activities.CommonActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Comment;
import net.mabako.steamgifts.data.TradeComment;
import net.mabako.steamgifts.fragments.DetailFragment;
import net.mabako.steamgifts.fragments.interfaces.ICommentableFragment;
import net.mabako.steamgifts.persistentdata.SteamGiftsUserData;

import java.util.Locale;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class CommentViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {
    private static final int MAX_VISIBLE_DEPTH = 11;

    private final ICommentableFragment fragment;

    private final TextView commentAuthor;
    private final TextView commentTime;
    private final TextView commentRole;
    protected final TextView commentContent;
    private final ImageView commentImage;
    private final View commentIndent;
    private final View commentMarker;
    private final View tradeScoreDivider;
    private final TextView tradeScorePositive;
    private final TextView tradeScoreNegative;

    private final Context context;
    private View.OnClickListener writeCommentListener, editCommentListener, deleteCommentListener;
    private boolean deleted;

    public CommentViewHolder(View v, Context context, ICommentableFragment fragment) {
        super(v);
        this.fragment = fragment;
        this.context = context;

        commentAuthor = (TextView) v.findViewById(R.id.user);
        commentTime = (TextView) v.findViewById(R.id.time);
        commentRole = (TextView) v.findViewById(R.id.role);

        commentContent = (TextView) v.findViewById(R.id.content);
        commentContent.setMovementMethod(LinkMovementMethod.getInstance());

        commentMarker = v.findViewById(R.id.comment_marker);
        commentIndent = v.findViewById(R.id.comment_indent);
        commentImage = (ImageView) v.findViewById(R.id.author_avatar);

        tradeScoreDivider = v.findViewById(R.id.trade_divider);
        tradeScorePositive = (TextView) v.findViewById(R.id.trade_score_positive);
        tradeScoreNegative = (TextView) v.findViewById(R.id.trade_score_negative);

        v.setOnCreateContextMenuListener(this);
    }

    @SuppressWarnings("deprecation")
    public void setFrom(final Comment comment) {
        StringUtils.setBackgroundDrawable(context, itemView, comment.isHighlighted());

        commentAuthor.setText(comment.getAuthor());
        commentAuthor.setTextAppearance(context, comment.isHighlighted() ? R.style.SmallText : comment.isOp() ? R.style.SmallText_HighlightOp : R.style.SmallText_NormalUser);
        StringUtils.setBackgroundDrawable(context, commentAuthor, comment.isOp() && !comment.isHighlighted(), R.attr.colorAccountHeader);

        // TODO this should eventually hold more information, such as white- and blacklisting icons.
        commentRole.setVisibility(comment.getAuthorRole() == null ? View.GONE : View.VISIBLE);
        commentRole.setText("{faw-gavel} " + comment.getAuthorRole());

        commentTime.setText(comment.getRelativeCreatedTime(context));
        commentTime.setTextAppearance(context, comment.isHighlighted() ? R.style.SmallText : R.style.SmallText_Light);

        commentContent.setText(StringUtils.fromHtml(context, comment.getContent(), !comment.isDeleted(), null));

        // Space before the marker
        ViewGroup.LayoutParams params = commentIndent.getLayoutParams();
        params.width = commentMarker.getLayoutParams().width * Math.min(MAX_VISIBLE_DEPTH, comment.getDepth());
        commentIndent.setLayoutParams(params);

        Picasso.with(context).load(comment.getAvatar()).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into(commentImage);
        View.OnClickListener viewProfileListener = comment.isDeleted() ? null : new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragment.showProfile(comment.getAuthor());
            }
        };

        commentImage.setOnClickListener(viewProfileListener);
        commentAuthor.setOnClickListener(viewProfileListener);

        if (fragment.canPostOrModifyComments()) {
            writeCommentListener = comment.getId() == 0 ? null : new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.requestComment(comment);
                }
            };

            // We assume that, if we have an editable state, we can edit the comment. Should we check for username here?
            editCommentListener = comment.getEditableContent() == null || comment.getId() == 0 || (!(fragment instanceof DetailFragment)) ? null : new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((DetailFragment) fragment).requestCommentEdit(comment);
                }
            };

            deleted = comment.isDeleted();
            deleteCommentListener = comment.getId() == 0 || !comment.isDeletable() ? null : new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fragment.deleteComment(comment);
                }
            };
        }

        if (comment instanceof TradeComment && !comment.isDeleted()) {
            tradeScoreDivider.setVisibility(View.VISIBLE);
            tradeScorePositive.setVisibility(View.VISIBLE);
            tradeScoreNegative.setVisibility(View.VISIBLE);

            tradeScorePositive.setText(String.format(Locale.ENGLISH, "+%d", ((TradeComment) comment).getTradeScorePositive()));
            tradeScoreNegative.setText(String.format(Locale.ENGLISH, "-%d", ((TradeComment) comment).getTradeScoreNegative()));
        } else {
            tradeScoreDivider.setVisibility(View.GONE);
            tradeScorePositive.setVisibility(View.GONE);
            tradeScoreNegative.setVisibility(View.GONE);
        }

        AttachedImageUtils.setFrom(itemView, comment, (CommonActivity) (((Fragment) fragment).getActivity()));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (SteamGiftsUserData.getCurrent(null).isLoggedIn()) {
            menu.setHeaderTitle(R.string.actions);

            if (writeCommentListener != null) {
                menu.add(R.string.add_comment).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (writeCommentListener != null)
                            writeCommentListener.onClick(itemView);
                        return true;
                    }
                });
            }

            if (editCommentListener != null) {
                menu.add(R.string.edit_comment).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (editCommentListener != null)
                            editCommentListener.onClick(itemView);
                        return true;
                    }
                });
            }

            if (deleteCommentListener != null) {
                menu.add(deleted ? R.string.undelete_comment : R.string.delete_comment).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (deleteCommentListener != null)
                            deleteCommentListener.onClick(itemView);
                        return true;
                    }
                });
            }
        }
    }
}
