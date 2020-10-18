package com.sserrano.ecoogo.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.sserrano.ecoogo.R;

public class ErrorFragment extends DialogFragment implements DialogInterface.OnClickListener,
        DialogInterface.OnDismissListener {

    public static final String TAG = ErrorFragment.class.getSimpleName();

    public static void showErrorFragment(String title, String message, FragmentActivity caller){
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("message", message);
        ErrorFragment errorFragment = new ErrorFragment();
        errorFragment.setArguments(bundle);
        errorFragment.show(caller.getSupportFragmentManager(), "error_fragment");
    }

    public static void showErrorFragment(FragmentActivity caller){
        new ErrorFragment().show(caller.getSupportFragmentManager(), "error_fragment");
    }

    @Override
    public Dialog onCreateDialog(Bundle bundle) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        if(getArguments() != null){
            builder.setTitle(getArguments().getString("title",
                    getString(R.string.error_title)))
                    .setMessage(getArguments().getString("message",
                            getString(R.string.error_message)))
                    .setPositiveButton(getString(R.string.exit_text),
                            ErrorFragment.this);
        } else {
            builder.setTitle(getString(R.string.error_title))
                    .setMessage(getString(R.string.error_message))
                    .setPositiveButton(getString(R.string.exit_text),
                            ErrorFragment.this);
        }

        builder.setCancelable(false).setOnDismissListener(ErrorFragment.this);
        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        System.exit(0);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        System.exit(0);
    }
}
