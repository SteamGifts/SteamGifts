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
    private static final String SAVED_COMMENT = "comment";
    private static final String SAVED_GIVEAWAY_ID = "giveaway-id";

    private EditText edit;

    /**
     * The comment we want to edit.
     */
    private Comment comment;

    /**
     * Giveaway id for 'comment and enter'
     */
    private String giveawayId;

    /**
     * Create a new fragment instance.
     *
     * @param existingComment     the parent comment to optionally display
     * @param enterableGiveawayId giveaway id for 'enter and comment'
     */
    public static WriteCommentFragment newInstance(@Nullable Comment existingComment, @Nullable String enterableGiveawayId) {
        WriteCommentFragment fragment = new WriteCommentFragment();

        Bundle args = new Bundle();
        args.putSerializable(SAVED_COMMENT, existingComment);
        args.putString(SAVED_GIVEAWAY_ID, enterableGiveawayId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            comment = (Comment) getArguments().getSerializable(SAVED_COMMENT);
            giveawayId = getArguments().getString(SAVED_GIVEAWAY_ID, null);
        } else {
            comment = (Comment) savedInstanceState.getSerializable(SAVED_COMMENT);
            giveawayId = savedInstanceState.getString(SAVED_GIVEAWAY_ID, null);
        }

        if (giveawayId != null && comment != null)
            throw new IllegalStateException("both parent comment and enter+comment are set");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(SAVED_COMMENT, comment);
        outState.putSerializable(SAVED_GIVEAWAY_ID, giveawayId);
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

        menu.findItem(R.id.send_comment_and_enter).setVisible(giveawayId != null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.send_comment) {
            ((WriteCommentActivity) getActivity()).submit(comment, edit.getText().toString());
            return true;
        } else if (id == R.id.send_comment_and_enter) {
            ((WriteCommentActivity) getActivity()).submitAndEnter(edit.getText().toString(), giveawayId);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void onEntered() {
        giveawayId = null;
        getActivity().supportInvalidateOptionsMenu();
    }
}
