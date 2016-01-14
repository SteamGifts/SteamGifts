package net.mabako.steamgifts.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.WriteCommentActivity;
import net.mabako.steamgifts.data.Comment;

import org.w3c.dom.Text;

import jp.wasabeef.picasso.transformations.RoundedCornersTransformation;

public class SingleCommentFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.comment, container, false);

        Comment comment = (Comment) getActivity().getIntent().getSerializableExtra(WriteCommentActivity.PARENT);

        ((TextView) layout.findViewById(R.id.user)).setText(comment.getAuthor());
        ((TextView) layout.findViewById(R.id.time)).setText(comment.getTimeAgo());

        CharSequence desc = Html.fromHtml(comment.getContent());
        desc = desc.subSequence(0, desc.length() - 2);
        ((TextView) layout.findViewById(R.id.content)).setText(desc);

        // Space before the marker
        View commentIndent = layout.findViewById(R.id.comment_indent);
        ViewGroup.LayoutParams params = commentIndent.getLayoutParams();
        params.width = 0;
        commentIndent.setLayoutParams(params);

        Picasso.with(getContext()).load(comment.getAvatar()).placeholder(R.drawable.default_avatar_mask).transform(new RoundedCornersTransformation(20, 0)).into((ImageView) layout.findViewById(R.id.author_avatar));

        return layout;
    }
}
