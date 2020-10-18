package com.sserrano.ecoogo.model;

import java.util.ArrayList;
import java.util.List;

public class Businesses {
    private Business[] businesses;
    private BusinessType[] businessTypes;
    private Tag[] tags;

    public Businesses(Business[] businesses, BusinessType[] businessTypes, Tag[] tags){
        this.businesses = businesses;
        this.businessTypes = businessTypes;
        this.tags = tags;
    }

    public Business getBusiness(int index){
        if(index >= businesses.length) return null;
        return businesses[index];
    }

    public Tag getTag(int index){
        if(index >= tags.length) return null;
        return tags[index];
    }

    public String getPlural(int typeIndex){
        return businessTypes[typeIndex].getPlural();
    }

    public int getTypeIconID(int typeIndex){
        return businessTypes[typeIndex].getIconResID();
    }

    public int length(){
        return businesses.length;
    }

    public int typesLength(){
        return businessTypes.length;
    }

    public int tagsLength(){
        return tags.length;
    }

    public int[] getBusinessesOfType(int typeIndex){
        List<Integer> resultList = new ArrayList<>();

        for(int i = 0; i < businesses.length; i++){
            if(businesses[i].getType() == typeIndex) resultList.add(i);
        }

        int[] result = new int[resultList.size()];
        for(int i = 0; i < resultList.size(); i++){
            result[i] = resultList.get(i);
        }
        return result;
    }

    public int[] getBusinessesWithTag(int tagIndex){
        List <Integer> resultList = new ArrayList<>();

        for(int i = 0; i < businesses.length; i++){
            for(int tag : businesses[i].getTags()){
                if(tag == tagIndex) resultList.add(i);
            }
        }

        int[] result = new int[resultList.size()];
        for(int i = 0; i < resultList.size(); i++){
            result[i] = resultList.get(i);
        }

        return result;
    }
}
