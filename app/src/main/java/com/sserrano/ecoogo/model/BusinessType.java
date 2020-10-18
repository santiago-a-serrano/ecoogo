package com.sserrano.ecoogo.model;

import com.sserrano.ecoogo.R;

class BusinessType {
    private String plural;
    private int iconResID;

    BusinessType(String plural, int index){
        this.plural = plural;

        switch(index){
            case 0:
                iconResID = R.drawable.bicon_restaurant;
                break;
            case 1:
                iconResID = R.drawable.bicon_hotel;
                break;
            case 2:
                iconResID = R.drawable.bicon_store;
                break;
            case 3:
                iconResID = R.drawable.bicon_cinema;
                break;
            default:
                iconResID = R.drawable.bicon_null;
                break;
            case 4:
                iconResID = R.drawable.bicon_cafe;
                break;
            case 5:
                iconResID = R.drawable.bicon_bar;
                break;
        }
    }

    String getPlural() {
        return plural;
    }

    int getIconResID() {
        return iconResID;
    }
}
