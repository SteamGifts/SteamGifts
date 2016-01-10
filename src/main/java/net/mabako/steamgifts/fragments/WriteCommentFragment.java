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

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.activities.WriteCommentActivity;

public class WriteCommentFragment extends DialogFragment {
    private EditText edit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_write_comment, container, false);

        setHasOptionsMenu(true);

        edit = (EditText) layout.findViewById(R.id.edit_text);
        edit.requestFocus();

        return layout;
    }


    @Override
    public void onCreateOptionsMenu(
            Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.write_comment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send_comment:
                edit.setEnabled(false);
                ((WriteCommentActivity) getActivity()).submit(edit.getText().toString());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
