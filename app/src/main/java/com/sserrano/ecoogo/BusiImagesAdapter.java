package com.sserrano.ecoogo;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewpager.widget.PagerAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sserrano.ecoogo.model.interfaces.OnPagerItemInstantiatedListener;

public class BusiImagesAdapter extends PagerAdapter {

    public static final String TAG = BusiImagesAdapter.class.getSimpleName();

    private int numOfImages;
    private Context context;
    private OnPagerItemInstantiatedListener busiFragment;

    public BusiImagesAdapter(int numOfImages, Context context,
                             OnPagerItemInstantiatedListener busiFragment){
        this.numOfImages = numOfImages;
        this.context = context;
        this.busiFragment = busiFragment;
    }

    @Override
    public int getCount() {
        return numOfImages;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return o == view;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ConstraintLayout busImgLayout = (ConstraintLayout)
                LayoutInflater.from(context).inflate(R.layout.business_image_layout, null);
        busImgLayout.setTag(position);
        container.addView(busImgLayout, 0);
        busiFragment.onPagerItemInstantiated(position);
        return busImgLayout;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
