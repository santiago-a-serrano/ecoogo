package com.sserrano.ecoogo.model.interfaces;

import android.location.Location;

public interface LocationReceiver {
    public void onLocationReceived(Location location);
    public void onLocationDisabled();
    public void onLocationNotAllowed();
    public void onLocationNotAvailable();
    public void onLocationAvailable();
}
