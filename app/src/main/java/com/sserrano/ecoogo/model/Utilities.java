package com.sserrano.ecoogo.model;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.widget.Space;

import java.util.ArrayList;


public class Utilities {
    public static int dpToPx(int dp, Context context){
        return (int) TypedValue.applyDimension
                (TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int scrWidthPx(Context context){
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int scrHeight(Context context){
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static Space verticalSpace(int dpSize, Context context){
        Space space = new Space(context);
        space.setMinimumHeight(Utilities.dpToPx(dpSize, context));
        return space;
    }

    public static Space horizontalSpace(int dpSize, Context context){
        Space space = new Space(context);
        space.setMinimumWidth(Utilities.dpToPx(dpSize, context));
        return space;
    }
}
