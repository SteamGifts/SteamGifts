package net.mabako.steamgifts.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;

import net.mabako.steamgifts.Application;
import net.mabako.steamgifts.R;

public class BetaNotificationDialogFragment extends DialogFragment {
    @NonNull
    @SuppressWarnings("deprecation")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Spanned message;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            message = Html.fromHtml(getString(R.string.beta_text), Html.FROM_HTML_MODE_LEGACY);
        } else {
            message = Html.fromHtml(getString(R.string.beta_text));
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.beta_dialog_title)
                .setMessage(message)
                .setPositiveButton(R.string.beta_contine, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getContext().getSharedPreferences(Application.PREF_BETA, Context.MODE_PRIVATE).edit().putBoolean(Application.PREF_KEY_NOTIFICATION_SHOWN, true).apply();
                    }
                })
                .create();
    }
}
