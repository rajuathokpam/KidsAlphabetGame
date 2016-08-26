package com.itservz.android.mayekid;

import java.io.Serializable;

/**
 * Created by raju.athokpam on 19-08-2016.
 */
public class MayekCard implements Serializable{
    private String title;
    private int res;
    private int picture;


    public MayekCard(String title, int res, int picture) {
        this.title = title;
        this.res = res;
        this.picture = picture;

    }

    public int getRes() {
        return res;
    }

    public String getTitle() {
        return title;
    }

    public int getPicture() {
        return picture;
    }


}