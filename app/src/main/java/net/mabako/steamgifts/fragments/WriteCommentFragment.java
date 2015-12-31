package net.mabako.steamgifts.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.fragments.util.WriteCommentListener;

public class WriteCommentFragment extends DialogFragment {
    private EditText edit;

    public static WriteCommentFragment newInstance(String giveawayId, String giveawayName, String xsrfToken) {
        Bundle args = new Bundle();
        args.putString("giveaway-id", giveawayId + "/" + giveawayName);
        args.putString("xsrf-token", xsrfToken);

        WriteCommentFragment fragment = new WriteCommentFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_write_comment, null);

        builder.setView(view);
        builder.setTitle("Write Comment");
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle args = getArguments();
                ((WriteCommentListener) getActivity()).submit(args.getString("giveaway-id"), args.getString("xsrf-token"), edit.getText().toString());
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        Dialog dialog = builder.create();

        edit = (EditText) view.findViewById(R.id.edit_text);
        edit.requestFocus();


        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        return dialog;
    }
}
