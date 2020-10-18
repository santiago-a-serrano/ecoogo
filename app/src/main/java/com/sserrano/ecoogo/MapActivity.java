package com.sserrano.ecoogo;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.sserrano.ecoogo.fragments.BusinessFragment;
import com.sserrano.ecoogo.fragments.ErrorFragment;
import com.sserrano.ecoogo.fragments.TipFragment;
import com.sserrano.ecoogo.model.Business;
import com.sserrano.ecoogo.model.Businesses;
import com.sserrano.ecoogo.model.CloudData;
import com.sserrano.ecoogo.model.LocationProvider;
import com.sserrano.ecoogo.model.Utilities;
import com.sserrano.ecoogo.model.interfaces.BusinessFragmentReadyCallback;
import com.sserrano.ecoogo.model.interfaces.LocationReceiver;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity
        implements OnMapReadyCallback, GoogleMap.OnMapLoadedCallback, View.OnClickListener,
        GoogleMap.OnMarkerClickListener, BusinessFragmentReadyCallback,
        LocationReceiver {

    public static final String TAG = MapActivity.class.getSimpleName();

    private MapView mapView;
    private LocationProvider locProvider;
    private GoogleMap mMap;
    private CloudData cloudData;
    private Businesses businesses;
    private List<Marker> busiMarkers;
    private Marker locationMarker = null;
    private List<Integer> visibleBusiMarkers = new ArrayList<>();
    private LatLng savedLocation = null;
    private Button myLocationButton;
    private LinearLayout loadingPopup;
    private BusinessFragment openBusFrag;
    private LinearLayout[] typeSelectors;
    private LinearLayout[] tagSelectors;
    private BitmapDescriptor locationIconBlue, locationIconGray;
    private BitmapDescriptor locationIcon = locationIconGray;

    private volatile boolean mapReady = false;
    private volatile int selectedTag = -1;
    private volatile int selectedType = -1;
    private volatile boolean businessFocusing = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        mapView = findViewById(R.id.map);
        mapView.onCreate(null);
        mapView.getMapAsync(this);
        MapsInitializer.initialize(this);
        cloudData = new CloudData(this);
        myLocationButton = findViewById(R.id.myLocationButton);
        loadingPopup = findViewById(R.id.loadingPopup);
        locationIconBlue = BitmapDescriptorFactory.fromResource(R.drawable.location_marker);
        locationIconGray = BitmapDescriptorFactory.fromResource(R.drawable.location_marker_gray);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        int verticalPadding = Utilities.dpToPx(24, this);
        googleMap.setPadding(0, verticalPadding, 0, verticalPadding/4);

        while(!cloudData.isDataAvailable() && !cloudData.didFailGettingData()){
            waitForData();
        }

        if(cloudData.didFailGettingData()){
            failedGettingDataPopup();
        } else {
            createBusinessMarkers();
            showBusinessMarkers();
            putTypeSelectors();
            putTagSelectors();
            if(savedLocation != null) setLocationMarker(savedLocation);
        }

        mMap.setOnMapLoadedCallback(this);
        mMap.setOnMarkerClickListener(this);
        setupMyLocation();
    }

    private void setupMyLocation(){
        locProvider = new LocationProvider(this, this);
    }

    @Override
    public void onMapLoaded() {
        camFitAll(false);
    }

    @Override
    public void onLocationReceived(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        setLocationMarker(latLng);
    }

    @Override
    public void onLocationDisabled() {
        TipFragment.showTipFragment(getString(R.string.location_disabled_title),
                getString(R.string.location_disabled_message), this);
    }

    public void onLocationNotAllowed(){
        TipFragment.showTipFragment(getString(R.string.no_location_allowed_title),
                getString(R.string.no_location_allowed_message), this);
    }

    @Override
    public void onLocationAvailable() {
        locationIcon = locationIconBlue;
        if(locationMarker != null){
            locationMarker.setIcon(locationIcon);
        }
    }

    @Override
    public void onLocationNotAvailable() {
        locationIcon = locationIconGray;
        if(locationMarker != null){
            locationMarker.setIcon(locationIcon);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            locProvider.permissionAccepted();
        } else {
            onLocationNotAllowed();
        }
    }

    public Activity getActivity(){
        return this;
    }

    public void camFocus(LatLng position){
        camFocus(position, null);
    }

    private void camFocus(LatLng position, final Marker marker){

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15),
                new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                if(marker != null)
                    openBusFrag = BusinessFragment.showCustom(MapActivity.this,
                        businesses.getBusiness(busiMarkers.indexOf(marker)));
                setBusinessFocusing(false);
            }

            @Override
            public void onCancel() {
                if(marker != null)
                    openBusFrag = BusinessFragment.showCustom(MapActivity.this,
                        businesses.getBusiness(busiMarkers.indexOf(marker)));
                setBusinessFocusing(false);
            }
        });
    }

    public LatLng getLocMarkerLoc() {
        if(locationMarker == null) return null;
        return locationMarker.getPosition();
    }

    private void waitForData(){

    }

    private void failedGettingDataPopup(){
        ErrorFragment.showErrorFragment(getString(R.string.failed_gathering_data_title),
                getString(R.string.failed_gathering_data_message), this);
    }

    private void createBusinessMarkers(){

        MarkerOptions markerOptions = new MarkerOptions().visible(false).anchor(0.5f, 0.9f);

        businesses = cloudData.getBusinesses();
        busiMarkers = new ArrayList<>(businesses.length());
        for(int i = 0; i < businesses.length(); i++){
            Business business = businesses.getBusiness(i);
            markerOptions.icon(createMarkerBitmapDescriptor
                    (businesses.getTypeIconID(business.getType()))).
                    position(new LatLng(business.getLatitude(), business.getLongitude()))
                    .title(business.getName());
            busiMarkers.add(mMap.addMarker(markerOptions));
        }
    }

    private BitmapDescriptor createMarkerBitmapDescriptor(int businessTypeIconResID){
        return createMarkerBitmapDescriptor(businessTypeIconResID, false);
    }

    private BitmapDescriptor createMarkerBitmapDescriptor(int businessTypeIconResID,
                                                          boolean highlighted){
        Drawable background = ContextCompat.getDrawable(this, R.drawable.eco_pointer);
        if(highlighted) background = ContextCompat.getDrawable
                (this, R.drawable.eco_pointer_highlighted);


        background.setBounds(0, 0, background.getIntrinsicWidth(),
                background.getIntrinsicHeight());
        Drawable typeIcon = ContextCompat.getDrawable(this, businessTypeIconResID);
        int leftBnd = Utilities.dpToPx(6, this),
            topBnd = Utilities.dpToPx(6, this);
        typeIcon.setBounds(leftBnd, topBnd, typeIcon.getIntrinsicWidth()/2 + leftBnd,
                typeIcon.getIntrinsicHeight()/2 + topBnd);
        Bitmap bitmap = Bitmap.createBitmap(background.getIntrinsicWidth(),
                background.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        typeIcon.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void showBusinessMarkers(){
        int[] allIndexes = new int[busiMarkers.size()];
        for(int i = 0; i < busiMarkers.size(); i++){
            allIndexes[i] = i;
        }
        showBusinessMarkers(allIndexes);
    }

    private void showBusinessMarkers(int[] markerIndexes){
        for(int i = 0; i < markerIndexes.length; i++){
            visibleBusiMarkers.add(markerIndexes[i]);
            busiMarkers.get(markerIndexes[i]).setVisible(true);
        }
    }

    private void hideAllBusinessMarkers(){
        for(int busiMarker : visibleBusiMarkers){
            busiMarkers.get(busiMarker).setVisible(false);
        }
        visibleBusiMarkers.clear();
    }

    private void camFitAll(){
        camFitAll(true);
    }

    private void camFitAll(boolean animate){
        double greatLat = 0, greatLon = 0, smallLat = 0, smallLon = 0;

        for(int i = 0; i < visibleBusiMarkers.size(); i++){
            LatLng markerPos = busiMarkers.get(visibleBusiMarkers.get(i)).getPosition();
            if(i == 0 || markerPos.latitude > greatLat) greatLat = markerPos.latitude;
            if(i == 0 || markerPos.longitude > greatLon) greatLon = markerPos.longitude;
            if(i == 0 || markerPos.latitude < smallLat) smallLat = markerPos.latitude;
            if(i == 0 || markerPos.longitude < smallLon) smallLon = markerPos.longitude;
        }

        if(locationMarker != null){
            LatLng userLocation = locationMarker.getPosition();
            if(userLocation.latitude > greatLat) greatLat = userLocation.latitude;
            if(userLocation.longitude > greatLon) greatLon = userLocation.longitude;
            if(userLocation.latitude < smallLat) smallLat = userLocation.latitude;
            if(userLocation.longitude < smallLon) smallLon = userLocation.longitude;
        }

        if(greatLat == smallLat && greatLon == smallLon){
            camFocus(new LatLng(greatLat, smallLat));
        } else {
            LatLngBounds camBounds = new LatLngBounds
                    (new LatLng(smallLat, smallLon), new LatLng(greatLat, greatLon));
            int padding = Utilities.dpToPx(32, this);
            if(animate) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(camBounds, padding));
            } else {
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(camBounds, padding));
            }
        }
    }

    private void setLocationMarker(LatLng location){
        if (locationMarker == null) {
            MarkerOptions locMarkOptions = new MarkerOptions().position(location).
                    title(getString(R.string.you_are_here)).icon(locationIcon).anchor(0.5f, 0.5f).zIndex(1);
            locationMarker = mMap.addMarker(locMarkOptions);
            myLocationButton.setVisibility(View.VISIBLE);
        } else {
            locationMarker.setPosition(location);
        }
    }

    private void putTypeSelectors(){
        LinearLayout typeSelectorLayout = findViewById(R.id.typeSelectorLayout);
        typeSelectors = new LinearLayout[businesses.typesLength()];
        for(int i = 0; i < businesses.typesLength(); i++) {
            typeSelectors[i] = (LinearLayout)
                    getLayoutInflater().inflate(R.layout.type_button_layout, null);
            TextView text = typeSelectors[i].findViewById(R.id.typeText);
            text.setText(businesses.getPlural(i));
            Button button = typeSelectors[i].findViewById(R.id.iconButton);
            Drawable icon = ContextCompat.getDrawable(this, businesses.getTypeIconID(i));
            button.setCompoundDrawablesWithIntrinsicBounds
                    (null, icon, null, null);
            final int typeIndex = i;
            typeSelectors[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!businessFocusing) {
                        colorTypeSelector(selectedType, true);
                        hideAllBusinessMarkers();
                        showBusinessMarkers(businesses.getBusinessesOfType(typeIndex));
                        colorTypeSelector(typeIndex);
                        camFitAll();
                        selectedType = typeIndex;
                    }
                }
            });
            typeSelectorLayout.addView(typeSelectors[i]);
        }
    }


    private void colorTypeSelector(int type){
        colorTypeSelector(type, false);
    }

    private void colorTypeSelector(int type, boolean grayOut){
        int backDrawable = grayOut ? R.drawable.back_button_round_type_selector :
                R.drawable.back_button_round_type_selector_selected;
        int drawTintColor = grayOut ? R.color.evenDarkerGray : android.R.color.white;
        View selectorView = type != -1 ? typeSelectors[type] : findViewById(R.id.allTypesToggle);
        Button iconButton = selectorView.findViewById(R.id.iconButton);
        iconButton.setBackground(ContextCompat.
                getDrawable(this, backDrawable));
        DrawableCompat.setTint(iconButton.getCompoundDrawables()[1],
                ContextCompat.getColor(this, drawTintColor));
    }

    private void putTagSelectors(){
        final HorizontalScrollView tagsScrollView = findViewById(R.id.tagsScrollView);
        LinearLayout tagsLayout = findViewById(R.id.tagsLayout);
        tagsScrollView.setHorizontalScrollBarEnabled(false);
        tagsScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        tagsLayout.addView(Utilities.horizontalSpace(8, this));

        tagSelectors = new LinearLayout[businesses.tagsLength()];
        for(int i = 0; i < businesses.tagsLength(); i++){
            tagSelectors[i] = (LinearLayout)
                getLayoutInflater().inflate(R.layout.eco_tag_button_header_layout, null);
            Button button = tagSelectors[i].findViewById(R.id.tagButton);
            button.setText(businesses.getTag(i).getName());
            tagsLayout.addView(tagSelectors[i]);
            tagSelectors[i].setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.
                  WRAP_CONTENT, Utilities.dpToPx(44, this)));

            final int tagIndex = i;
            tagSelectors[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!businessFocusing) {

                        if (selectedTag != -1) {
                            highlightTagSelector(selectedTag, true);
                            highlightBusMarkersWithTag(selectedTag, true);
                        }

                        if (selectedTag != tagIndex) {
                            highlightBusMarkersWithTag(tagIndex);
                            highlightTagSelector(tagIndex);
                            selectedTag = tagIndex;
                        } else {
                            selectedTag = -1;
                        }
                    }
                }
            });

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parent = (View) v.getParent();
                    parent.callOnClick();
                }
            });

            tagsLayout.addView(Utilities.horizontalSpace(8, this));
        }
    }

    private void highlightBusMarkersWithTag(int tag){
        highlightBusMarkersWithTag(tag, false);
    }

    private void highlightBusMarkersWithTag(int tag, boolean deHighlight){
        int[] busToHighlight = businesses.getBusinessesWithTag(tag);
        for(int i = 0; i < busToHighlight.length; i++){
            busiMarkers.get(busToHighlight[i]).setIcon(createMarkerBitmapDescriptor
                    (businesses.getTypeIconID(businesses.getBusiness(busToHighlight[i]).getType()),
                            !deHighlight));
        }
    }

    private void highlightTagSelector(int tag) {
        highlightTagSelector(tag, false);
    }

    private void highlightTagSelector(int tag, boolean deHighlight){
        int backDrawable = deHighlight ? R.drawable.back_eco_tag_button_header :
                R.drawable.back_eco_tag_button_header_selected;
        int textColor = deHighlight ? R.color.primaryDarkerColor : R.color.primaryTextColor;
        Button button = tagSelectors[tag].findViewById(R.id.tagButton);
        button.setBackground(ContextCompat.getDrawable(this, backDrawable));
        button.setTextColor(ContextCompat.getColor(this, textColor));
    }

    private void setBusinessFocusing(boolean focusing){
        businessFocusing = focusing;
        mMap.getUiSettings().setAllGesturesEnabled(!focusing);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.myLocationButton:
                if(getLocMarkerLoc() != null && !businessFocusing) camFocus(getLocMarkerLoc());
                break;
            case R.id.go_button:
                if(openBusFrag != null) openBusFrag.goToBusiness();
                break;
            case R.id.allTypesToggle:
                if(!businessFocusing) {
                    colorTypeSelector(selectedType, true);
                    showBusinessMarkers();
                    colorTypeSelector(-1);
                    camFitAll();
                    selectedType = -1;
                }
                break;
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker){
        if(busiMarkers.contains(marker) && !businessFocusing) {
            camFocus(marker.getPosition(), marker);
            setBusinessFocusing(true);
            loadingPopup.setVisibility(View.VISIBLE);
            loadingPopup.setTranslationY(Utilities.dpToPx(40, getActivity()));
            loadingPopup.animate().translationYBy(Utilities.dpToPx(-40, getActivity()));
            return true;
        } else {
            return true;
        }
    }

    @Override
    public void onBusinessFragmentReady() {
        loadingPopup.animate().translationYBy(Utilities.dpToPx(40, getActivity())).
                withEndAction(new Runnable() {
            @Override
            public void run() {
                loadingPopup.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }
    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }
    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
    @Override
    public void onBackPressed() {

    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == LocationProvider.REQUEST_CODE){
            if(resultCode == RESULT_OK){
                locProvider.settingsAccepted();
            } else {
                onLocationNotAllowed();
            }
        }
    }
}
