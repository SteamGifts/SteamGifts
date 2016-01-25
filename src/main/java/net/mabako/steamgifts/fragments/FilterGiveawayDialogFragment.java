package net.mabako.steamgifts.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import net.mabako.steamgifts.R;
import net.mabako.steamgifts.fragments.interfaces.IFilterUpdatedListener;
import net.mabako.steamgifts.persistentdata.FilterData;

public class FilterGiveawayDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private boolean requireReload = false;

    private View view;
    private IFilterUpdatedListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        FilterData f = FilterData.getCurrent();
        view = getActivity().getLayoutInflater().inflate(R.layout.fragment_filter, null, false);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(view)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.reset, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear all filter settings
                        if (FilterData.getCurrent().isAnyActive()) {
                            FilterData.clear();

                            if (listener != null)
                                listener.onFilterUpdated();
                        }
                    }
                }).create();

        setValue(view.findViewById(R.id.filter_entries_max), f.getMaxEntries());
        setValue(view.findViewById(R.id.filter_level_max), f.getMaxLevel());
        setValue(view.findViewById(R.id.filter_points_max), f.getMaxPoints());

        setValue(view.findViewById(R.id.filter_entries_min), f.getMinEntries());
        setValue(view.findViewById(R.id.filter_level_min), f.getMinLevel());
        setValue(view.findViewById(R.id.filter_points_min), f.getMinPoints());
        ((CheckBox) view.findViewById(R.id.filter_entered)).setChecked(f.isHideEntered());

        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        FilterData oldFilterData = FilterData.getCurrent();
        FilterData.clear();
        FilterData newFilterData = FilterData.getCurrent();

        newFilterData.setMaxEntries(getValueAndUpdateFlags(R.id.filter_entries_max, oldFilterData.getMaxEntries()));
        newFilterData.setMaxLevel(getValueAndUpdateFlags(R.id.filter_level_max, oldFilterData.getMaxLevel()));
        newFilterData.setMaxPoints(getValueAndUpdateFlags(R.id.filter_points_max, oldFilterData.getMaxPoints()));
        newFilterData.setMinEntries(getValueAndUpdateFlags(R.id.filter_entries_min, oldFilterData.getMinEntries()));
        newFilterData.setMinLevel(getValueAndUpdateFlags(R.id.filter_level_min, oldFilterData.getMinLevel()));
        newFilterData.setMinPoints(getValueAndUpdateFlags(R.id.filter_points_min, oldFilterData.getMinPoints()));

        boolean hideEntered = ((CheckBox) view.findViewById(R.id.filter_entered)).isChecked();
        newFilterData.setHideEntered(hideEntered);
        if (hideEntered != oldFilterData.isHideEntered())
            requireReload = true;

        if (listener != null && requireReload)
            listener.onFilterUpdated();
    }

    private void setValue(View view, int value) {
        if (value >= 0 && view instanceof EditText) {
            EditText editText = (EditText) view;
            editText.setText(String.valueOf(value));
        }
    }

    private int getValueAndUpdateFlags(int editResource, int oldValue) {
        EditText editText = (EditText) view.findViewById(editResource);

        int newValue;
        try {
            newValue = Integer.parseInt(editText.getText().toString());
        } catch (NumberFormatException e) {
            newValue = -1;
        }

        if (oldValue != newValue)
            requireReload = true;

        return newValue;
    }

    public void setListener(IFilterUpdatedListener listener) {
        this.listener = listener;
    }

}
