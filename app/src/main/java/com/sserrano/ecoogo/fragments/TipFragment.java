package com.sserrano.ecoogo.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.sserrano.ecoogo.R;

public class TipFragment extends DialogFragment implements DialogInterface.OnClickListener {

    public static void showTipFragment(String title, String message, FragmentActivity caller){
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("message", message);
        TipFragment tipFragment = new TipFragment();
        tipFragment.setArguments(bundle);
        tipFragment.show(caller.getSupportFragmentManager(), "tip_fragment");
    }

    public static void showTipFragment(FragmentActivity caller){
        new TipFragment().show(caller.getSupportFragmentManager(), "tip_fragment");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        
        if(getArguments() != null){
            builder.setTitle(getArguments().getString("title", getString(R.string.tip_title)))
                    .setMessage(getArguments().getString("message",
                            getString(R.string.tip_message)))
                    .setPositiveButton(getString(R.string.got_it_text), TipFragment.this);
        } else {
            builder.setTitle(getString(R.string.tip_title)).
                    setMessage(getString(R.string.tip_message))
                    .setPositiveButton(getString(R.string.got_it_text), this);
        }

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {

    }
}
