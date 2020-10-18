package com.sserrano.ecoogo.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sserrano.ecoogo.BusiImagesAdapter;
import com.sserrano.ecoogo.R;
import com.sserrano.ecoogo.model.Business;
import com.sserrano.ecoogo.model.GetImageFromURL;
import com.sserrano.ecoogo.model.Utilities;
import com.sserrano.ecoogo.model.interfaces.BusinessFragmentReadyCallback;
import com.sserrano.ecoogo.model.interfaces.ImageFromURLReceiver;
import com.sserrano.ecoogo.model.interfaces.OnPagerItemInstantiatedListener;

public class BusinessFragment extends DialogFragment implements ImageFromURLReceiver,
        OnPagerItemInstantiatedListener {

    public static final String TAG = DialogFragment.class.getSimpleName();

    private double latitude, longitude;
    private int imageCount = 0;
    private LinearLayout infoLinearLayout;
    private ViewPager imageViewPager;
    private boolean[] isPItemInstantiated;
    private Drawable[] imagesOnQueue;

    public static BusinessFragment showCustom(FragmentActivity caller, Business business){
        Bundle infoBundle = new Bundle();

        /* Here you should add to the infoBundle all of the attributes or fields of the business,
        even if they are not obligatory and there is a chance they are null. */
        infoBundle.putDouble("latitude", business.getLatitude());
        infoBundle.putDouble("longitude", business.getLongitude());
        infoBundle.putString("name", business.getName());
        infoBundle.putString("type", business.getTypeName());
        infoBundle.putStringArray("tags", business.getTagNames());
        infoBundle.putString("address", business.getAddress());
        infoBundle.putString("hours", business.getHours());
        infoBundle.putString("webpage", business.getWebpage());
        infoBundle.putString("phone", business.getPhone());
        infoBundle.putStringArray("imageURLs", business.getImageURLs());
        infoBundle.putInt("rating", business.getRating());

        BusinessFragment businessFragment = new BusinessFragment();
        businessFragment.setArguments(infoBundle);
        businessFragment.show(caller.getSupportFragmentManager(), "business_fragment");
        return businessFragment;
    }

    public static void instantiateDemo(FragmentActivity caller){
        new BusinessFragment().show(caller.getSupportFragmentManager(), "business_fragment");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.business_fragment, container,
                true);


        if(getArguments() != null) {
            latitude = getArguments().getDouble("latitude");
            longitude = getArguments().getDouble("longitude");

            TextView nameField = fragmentView.findViewById(R.id.businessName);
            nameField.setText(getArguments().getString("name", getString(R.string.no_name_text)));
            TextView typeField = fragmentView.findViewById(R.id.businessType);
            typeField.setText(getArguments().getString("type", ""));
            TextView addressText = fragmentView.findViewById(R.id.address);
            addressText.setText(getArguments().getString("address"));

            //TODO: FIX THIS!
            /*
            TextView ratingNumber = fragmentView.findViewById(R.id.ratingNumber);
            int ratingUnprocessed = getArguments().getInt("rating", 0);
            if(ratingUnprocessed == 0){
                ratingNumber.setText(getText(R.string.no_rating_text));
            } else {
                String ratingProcessed = Integer.toString(ratingUnprocessed);
                ratingNumber.setText(ratingProcessed);
            }
            */


            infoLinearLayout = fragmentView.findViewById(R.id.infoLinearLayout);

            if(getArguments().getStringArray("tags") != null) {
                HorizontalScrollView tagsLayoutScrollView = new HorizontalScrollView(getContext());
                LinearLayout tagsLayout = new LinearLayout(getContext());
                tagsLayoutScrollView.addView(tagsLayout);
                tagsLayoutScrollView.setHorizontalScrollBarEnabled(false);
                tagsLayoutScrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);
                infoLinearLayout.addView(tagsLayoutScrollView);

                tagsLayout.addView(Utilities.horizontalSpace(8, getContext()));
                String[] tagsArray = getArguments().getStringArray("tags");
                for (int i = 0; i < tagsArray.length; i++){
                    Button tagButton = (Button)
                            getLayoutInflater().inflate(R.layout.eco_tag_button_layout, null);
                    tagButton.setText(tagsArray[i]);
                    tagsLayout.addView(tagButton);
                    tagButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.
                            WRAP_CONTENT, Utilities.dpToPx(28, getContext())));

                    tagsLayout.addView(Utilities.horizontalSpace(8, getContext()));
                }
            }

            infoLinearLayout.addView(Utilities.verticalSpace(12, getContext()));

            inflateIfExists("hours", R.layout.bpam_schedule_layout, R.id.scheduleText,
                    infoLinearLayout);
            inflateIfExists("webpage", R.layout.bpam_webpage_layout, R.id.webpage,
                    infoLinearLayout);
            inflateIfExists("phone", R.layout.bpam_phone_layout, R.id.phoneText,
                    infoLinearLayout);

            //Set the images of the business
            String[] imageURLs = getArguments().getStringArray("imageURLs");
            imageViewPager = infoLinearLayout.findViewById(R.id.imageViewPager);
            if(imageURLs != null && imageURLs.length > 0) {
                isPItemInstantiated = new boolean[imageURLs.length];
                imagesOnQueue = new Drawable[imageURLs.length];
                BusiImagesAdapter busiImagesAdapter =
                        new BusiImagesAdapter(imageURLs.length, getContext(), this);
                imageViewPager.setOffscreenPageLimit(imageURLs.length - 1);
                imageViewPager.setPageMargin(Utilities.dpToPx(8, getContext()));
                imageViewPager.setAdapter(busiImagesAdapter);
                for (int i = 0; i < imageURLs.length; i++) {
                    GetImageFromURL imageGetter = new GetImageFromURL();
                    imageGetter.setReceiver(this);
                    imageGetter.execute(imageURLs[i]);
                }
            } else {
                isPItemInstantiated = new boolean[1];
                BusiImagesAdapter busiImagesAdapter =
                        new BusiImagesAdapter(1, getContext(), this);
                imageViewPager.setAdapter(busiImagesAdapter);
                onImageReceived(null);
            }
        }

        return fragmentView;
    }


    private void inflateIfExists(String parameter, int layoutResID, int textResID,
                                 LinearLayout infoLinearLayout){
        if(getArguments() != null && getArguments().getString(parameter) != null){
            ConstraintLayout inflatable =
                    (ConstraintLayout) getLayoutInflater().inflate(layoutResID, null);
            TextView text = inflatable.findViewById(textResID);
            text.setText(getArguments().getString(parameter));
            infoLinearLayout.addView(inflatable);
        }
    }

    @Override
    public void onImageReceived(Drawable image) {
        if(isPItemInstantiated[imageCount]) {
            putImageInPager(image, imageCount);
        } else if (imagesOnQueue != null){
            imagesOnQueue[imageCount] = image;
        }
        imageCount++;
    }

    @Override
    public void onPagerItemInstantiated(int position) {
        isPItemInstantiated[position] = true;
        if(imagesOnQueue == null){
            putImageInPager(null, position);
        } else if(imagesOnQueue[position] != null){
            putImageInPager(imagesOnQueue[position], position);
        }
    }

    private void putImageInPager(Drawable image, int position){
        ConstraintLayout imageLayout = infoLinearLayout.findViewById(R.id.imageViewPager).
                findViewWithTag(position);
        imageLayout.findViewById(R.id.imageLoading).setVisibility(View.GONE);
        ImageView imageView = imageLayout.findViewById(R.id.businessImage);
        if (image == null && getContext() != null) {
            imageView.setImageDrawable
                    (ContextCompat.getDrawable(getContext(), R.color.primaryLightColor));
            imageLayout.findViewById(R.id.brokenImageIcon).setVisibility(View.VISIBLE);
        } else {
            imageView.setImageDrawable(image);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        View businessFragment = getView();
        if(businessFragment != null && getActivity() != null) {
            int margin = Utilities.dpToPx(32, getActivity());
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams
                    (getActivity().findViewById(R.id.mapLayout).getWidth() - margin,
                            getActivity().findViewById(R.id.mapLayout).getHeight() - margin);
            businessFragment.setLayoutParams(layoutParams);
        } else {
            Log.w(TAG, "The business fragment that was intended to be modified or the " +
                    "activity the fragment is currently associated with were null");
        }

        if(getActivity() instanceof BusinessFragmentReadyCallback){
            ((BusinessFragmentReadyCallback) getActivity()).onBusinessFragmentReady();
        }
    }

    public void goToBusiness(){
        PackageManager pm = null;
        if(getActivity() != null) pm = getActivity().getPackageManager();
        Uri intentURI = Uri.parse("google.navigation:q="+latitude+","+longitude);
        Intent intent = new Intent(Intent.ACTION_VIEW, intentURI);
        intent.setPackage("com.google.android.apps.maps");

        try {
            if (pm != null && pm.getPackageInfo("com.waze", 0) != null) {
                intentURI = Uri.parse("geo: " + latitude + "," + longitude);
                intent = new Intent(Intent.ACTION_VIEW, intentURI);
                intent.setPackage("com.waze");
            }
        } catch (PackageManager.NameNotFoundException e){}

        startActivity(intent);
    }
}
