package net.mabako.steamgifts.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import net.mabako.steamgifts.activities.WriteCommentActivity;
import net.mabako.steamgifts.core.R;
import net.mabako.steamgifts.data.Comment;

public class WriteCommentFragment extends DialogFragment {
    private static final String STATE_COMMENT = "comment";

    private EditText edit;

    /**
     * The comment we want to edit.
     */
    private Comment comment;

    public static WriteCommentFragment newInstance(Comment existingComment) {
        WriteCommentFragment fragment = new WriteCommentFragment();

        Bundle args = new Bundle();
        args.putSerializable(STATE_COMMENT, existingComment);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            comment = (Comment) getArguments().getSerializable(STATE_COMMENT);
        } else {
            comment = (Comment) savedInstanceState.getSerializable(STATE_COMMENT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_COMMENT, comment);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_write_comment, container, false);

        setHasOptionsMenu(true);

        edit = (EditText) layout.findViewById(R.id.edit_text);
        edit.requestFocus();

        if (savedInstanceState == null && comment != null)
            edit.setText(comment.getEditableContent());

        return layout;
    }


    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.write_comment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.send_comment) {
            edit.setEnabled(false);
            ((WriteCommentActivity) getActivity()).submit(comment, edit.getText().toString());
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
