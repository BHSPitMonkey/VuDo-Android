package com.stepheneisenhauer.vudo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;

import java.util.ArrayList;

/**
 * Generates the Dialog used to show discovered servers and allow the user to select one
 */
public class DiscoveryDialogFragment extends DialogFragment {
    ArrayList<NsdServiceInfo> mServices;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        CharSequence[] items = {};
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_scanning)
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setDiscoveredServices(ArrayList<NsdServiceInfo> services) {
        mServices = services;
        CharSequence[] items = new CharSequence[services.size()];
        for (int i=0; i<services.size(); i++) {
            items[i] = services.get(i).getServiceName();
        }
        /*
        getDialog().setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        */
    }
}